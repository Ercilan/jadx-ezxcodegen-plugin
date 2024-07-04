package jadx.plugins.ezxcodegen;

import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.plugins.gui.JadxGuiContext;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.exceptions.JadxRuntimeException;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CodeGenerator implements Consumer<ICodeNodeRef> {
	private final JadxGuiContext guiContext;
	private final JadxDecompiler decompiler;
	private final CustomOptions options;
	/**
	 * 每一行已生成的 ParamTypes 代码长度
	 */
	private int paramTypesGenLength;

	private static final Map<String, String> PRIMITIVE_TYPE_MAPPING = Map.of(
			"int", "Int",
			"byte", "Byte",
			"short", "Short",
			"long", "Long",
			"float", "Float",
			"double", "Double",
			"char", "Char",
			"boolean", "Boolean",
			"void", "Void");

	public CodeGenerator(JadxGuiContext guiContext, JadxDecompiler decompiler, CustomOptions options) {
		this.guiContext = guiContext;
		this.decompiler = decompiler;
		this.options = options;
	}

	@Override
	public void accept(ICodeNodeRef iCodeNodeRef) {
		this.paramTypesGenLength = 0;
		JavaNode node = decompiler.getJavaNodeByRef(iCodeNodeRef);
		String snippet = generateXposedSnippet(node);
		guiContext.copyToClipboard(snippet);
	}

	private String generateXposedSnippet(JavaNode node) {
		if (node instanceof JavaMethod) {
			return generateMethodSnippet((JavaMethod) node);
		}
		if (node instanceof JavaClass) {
			return generateClassSnippet((JavaClass) node);
		}
		if (node instanceof JavaField) {
			return generateFieldSnippet((JavaField) node);
		}
		throw new JadxRuntimeException("Unsupported node type: " + (node != null ? node.getClass() : "null"));
	}

	private String generateClassSnippet(JavaClass javaClass) {
		String rawClassName = javaClass.getRawName();
		String shortClassName = Utils.lowerCaseFirst(javaClass.getName());

		String javaXposedFormatStr =
				"ClassLoader classLoader = lpparam.classLoader;\n"
						+ "Class<?> %sClass = classLoader.loadClass(\"%s\");";
		String kotlinXposedFormatStr = "val %sClass = ClassUtils.loadClass(\"%s\")";

		/*XposedCodegenLanguage language = getLanguage();
		String xposedFormatStr;
		switch (language) {
			case JAVA:
				xposedFormatStr = javaXposedFormatStr;
				break;
			case KOTLIN:
				xposedFormatStr = kotlinXposedFormatStr;
				break;
			default:
				throw new JadxRuntimeException("Invalid Xposed code generation language: " + language);
		}*/

		return String.format(kotlinXposedFormatStr, shortClassName, rawClassName);
	}

	private String generateFieldSnippet(JavaField javaField) {
		String isStatic = javaField.getAccessFlags().isStatic() ? "Static" : "";
		String type = PRIMITIVE_TYPE_MAPPING.getOrDefault(javaField.getFieldNode().getType().toString(), "Object");
		String xposedMethod = "XposedHelpers.get" + isStatic + type + "Field";
		// Ezxhelper是获取field
		// FieldFinder.fromClass(vipResponseClz).filterByType(Any::class.java).first()

		String javaXposedFormatStr =
				"%s(/*runtimeObject*/, \"%s\");";
		String kotlinXposedFormatStr =
				"%s(/*runtimeObject*/, \"%s\")";

		/*XposedCodegenLanguage language = getLanguage();
		String xposedFormatStr;
		switch (language) {
			case JAVA:
				xposedFormatStr = javaXposedFormatStr;
				break;
			case KOTLIN:
				xposedFormatStr = kotlinXposedFormatStr;
				break;
			default:
				throw new JadxRuntimeException("Invalid Xposed code generation language: " + language);
		}*/

		return String.format(kotlinXposedFormatStr, xposedMethod, javaField.getFieldNode().getFieldInfo().getName());
	}

	private String generateMethodSnippet(JavaMethod jMth) {
		MethodNode mth = jMth.getMethodNode();
		String methodPart;
		String xposedMethod;
		if (mth.isConstructor()) {
			xposedMethod = "ConstructorFinder";
			methodPart = "";
		} else {
			xposedMethod = "MethodFinder";
			methodPart = generateMethodPart(mth);
		}

		// kotlin need to escape $ (should $+num be changed? )
		String rawClassName = jMth.getDeclaringClass().getRawName().replaceAll("\\$", "\\\\\\$");
		String javaXposedFormatStr = "";
		String kotlinXposedFormatStr = "%s.fromClass(\"%s\")%s%s.first().createHook {\n" +
				"        before {\n" +
				"            \n" +
				"        }\n" +
				"    }";

		// todo support java
		String xposedFormatStr = kotlinXposedFormatStr;

		/*XposedCodegenLanguage language = getLanguage();
		String xposedFormatStr;
		switch (language) {
			case JAVA:
				xposedFormatStr = javaXposedFormatStr;
				break;
			case KOTLIN:
				xposedFormatStr = kotlinXposedFormatStr;
				break;
			default:
				throw new JadxRuntimeException("Invalid Xposed code generation language: " + language);
		}*/

		List<ArgType> mthArgs = mth.getArgTypes();
		if (mthArgs.isEmpty()) {
			// 无参方法，若直接不筛选参数，因为条件较松，若存在同名方法，可能会使xposed使用到其他方法。
			// 所以此处增加限制筛选无参数
			if (Utils.anyOtherSameNameMethod(mth)) {
				return String.format(xposedFormatStr, xposedMethod, rawClassName, methodPart, "\n    .filterByParamCount(0)\n");
			}
			return String.format(xposedFormatStr, xposedMethod, rawClassName, methodPart, "");
		}

		String params = genParamsType(mth);
		if (params.endsWith("\n")) {
			params = params.substring(0, params.length() - 1) + genReturnTypeFilter(mth) + "\n    ";
		} else {
			params += genReturnTypeFilter(mth);
		}
		return String.format(xposedFormatStr, xposedMethod, rawClassName, methodPart, params);
	}


	private String genAllAccessFilter(MethodNode mth) {
		if (options.isEnableMethodAccess()) {
			return genAccessModifierCode(mth);
		}
		return "";
	}

	private String genParamsType(MethodNode mth) {
		List<ArgType> mthArgs = mth.getArgTypes();
		String params = mthArgs.stream()
				.map(this::genParam)
				.collect(Collectors.joining());
		params = params.substring(0, params.length() - 2);
		if (params.contains("\n")) {
			params = "\n    .filterByParamTypes(\n        " + params.trim() + "\n    )";
		} else {
			params = "\n    .filterByParamTypes(" + params + ")\n";
		}
		return params;
	}

	private String genReturnTypeFilter(MethodNode mth) {
		if (options.isEnableMethodReturnType()) {
			return genReturnTypeCode(mth, options.isClassOptimizationEnable());
		}
		return "";
	}

	private String genParam(ArgType type) {
		int limit = paramTypesGenLength == 0 ? 45 : 75;
		String s = genClassObject(type, options.isClassOptimizationEnable()) + ", ";
		paramTypesGenLength += s.length();
		if (paramTypesGenLength >= limit) {
			s = "\n        " + s;
			paramTypesGenLength = s.length();
		}

		return s;
	}

	public static Predicate<MethodNode> isConfusedMethodWith(MethodNode mth) {
		MethodInfo methodInfo = mth.getMethodInfo();
		return otherMth -> {
			MethodInfo otherMethodInfo = otherMth.getMethodInfo();
			return otherMth != mth && otherMethodInfo.getName().equals(methodInfo.getName()) &&
					otherMethodInfo.getArgumentsTypes().equals(methodInfo.getArgumentsTypes());
		};
	}

	private String generateMethodPart(MethodNode mth) {
		StringBuilder methodPart = new StringBuilder();
		if (options.isDebugEnable()) {
			methodPart.append("\n    // ").append(Utils.getMethodString(mth));
		}
		methodPart.append(genAllAccessFilter(mth));
		methodPart.append("\n    .filterByName(\"").append(mth.getMethodInfo().getName()).append("\")");

		ClassNode classNode = mth.getParentClass();
		List<MethodNode> sameMethodNodes = classNode.getMethods().stream()
				.filter(isConfusedMethodWith(mth))
				.collect(Collectors.toList());

		if (sameMethodNodes.isEmpty()) {
			return methodPart.toString();
		}

		if (options.isDebugEnable()) {
			methodPart.append("\n    // Found methods with duplicate names and parameter types\n    // 1. Try checking access flag");
		}
		// 检查访问修饰符
		Comparator comparator = new Comparator(mth, sameMethodNodes, options);
		String result = comparator.searchUniqueModifier();
		if (Utils.isNotEmpty(result)) {
			// 可唯一，直接添加全部强制过滤代码
			if (options.isEnableMethodAccess()) {
				return methodPart.append(genReturnTypeFilter(mth)).toString();
			}
			methodPart.append(result);
			return methodPart.toString();
		}
		// 检查返回值类型
		if (options.isDebugEnable()) {
			methodPart.append("\n    // 2. Try checking the return type");
		}
		String uniqueReturnType = comparator.searchUniqueReturnType();
		if (Utils.isNotEmpty(uniqueReturnType)) {
			if (options.isEnableMethodReturnType()) {
				return methodPart.toString();
			}
			return methodPart.append(uniqueReturnType).toString();
		}

		methodPart.append("\n    // Warning: Failed to retrieve unique condition for this method.\n");
		return methodPart.toString();
	}

	public static String genAccessModifierCode(MethodNode mth) {
		AccessInfo accessFlags = mth.getAccessFlags();
		StringBuilder sb = new StringBuilder();
		if (accessFlags.isPublic()) {
			sb.append("\n    .filterPublic()");
		} else if (accessFlags.isPrivate()) {
			sb.append("\n    .filterPrivate()");
		} else if (accessFlags.isProtected()) {
			sb.append("\n    .filterProtected()");
		} else if (accessFlags.isPackagePrivate()) {
			sb.append("\n    .filterPackagePrivate()");
		}

		if (accessFlags.isAbstract()) {
			sb.append("\n    .filterAbstract()");
		}
		if (accessFlags.isNative()) {
			sb.append("\n    .filterNative()");
		}
		if (accessFlags.isStatic()) {
			sb.append("\n    .filterStatic()");
		}
		if (accessFlags.isFinal()) {
			sb.append("\n    .filterFinal()");
		}
		if (accessFlags.isVarArgs()) {
			sb.append("\n    .filterVarargs()");
		}
		return sb.toString();
	}

	public static String genReturnTypeCode(MethodNode mth, boolean optimize) {
		return "\n    .filterByReturnType(" + genClassObject(mth.getReturnType(), optimize) + ")";
	}

	public static String fixTypeContent(ArgType type) {
		if (type.isGeneric()) {
			return type.getObject();
		} else if (type.isPrimitive()) { // ::class.javaPrimitiveType
			return PRIMITIVE_TYPE_MAPPING.getOrDefault(type.getPrimitiveType().getLongName(), "Object");
		} else if (type.isObject() && type.getObject().startsWith("java.lang.")) {
			return type.getObject().substring(10);
		} else if (type.isGenericType() && type.isObject() && type.isTypeKnown()) {
			return "Object";
		} else if (type.isArray()) {
			// jvmMain/kotlin/Arrays.kt
			return PRIMITIVE_TYPE_MAPPING.getOrDefault(type.getArrayElement().toString(), "") + "Array";
		}
		// kotlin replace for inner class
		return type.toString().replace("$", ".");
	}

	public static String genClassObject(ArgType type, boolean optimize) {
		String baseClassName = fixTypeContent(type);
		String classRepresentation = baseClassName + "::class.java";

		if (baseClassName.contains(".")) {
			if (optimize && !isSystemClassName(baseClassName)) {
				classRepresentation = "ClassUtils.loadClass(\"" + baseClassName + "\")";
			}
		}

		return classRepresentation;
	}


	private static boolean isSystemClassName(String className) {
		return className.startsWith("android.") || className.startsWith("androidx.") ||
				className.startsWith("jdk.") || className.startsWith("dalvik.") ||
				className.startsWith("java.") || className.startsWith("javax.");
	}
}
