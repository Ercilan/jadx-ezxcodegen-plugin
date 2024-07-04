package jadx.plugins.ezxcodegen;

import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Comparator {

	private static final Logger LOG = LoggerFactory.getLogger(Comparator.class);

	private final MethodNode mth;
	private final List<MethodNode> otherMthNodes;
	private final CustomOptions options;

	private static final Map<String, Function<MethodNode, Object>> ACCESS_FUNC_MAP = new HashMap<>();
	/**
	 * 可以组合的其他关键字
	 */
	private static final Map<String, Function<MethodNode, Object>> OTHER_FUNC_MAP = new HashMap<>();

	static {
		ACCESS_FUNC_MAP.put("filterPublic", m -> m.getAccessFlags().isPublic());
		ACCESS_FUNC_MAP.put("filterPrivate", m -> m.getAccessFlags().isPrivate());
		ACCESS_FUNC_MAP.put("filterProtected", m -> m.getAccessFlags().isProtected());
		ACCESS_FUNC_MAP.put("filterPackagePrivate", m -> m.getAccessFlags().isPackagePrivate());
	}

	static {
		OTHER_FUNC_MAP.put("filterStatic", m -> m.getAccessFlags().isStatic());
		OTHER_FUNC_MAP.put("filterFinal", m -> m.getAccessFlags().isFinal());
		OTHER_FUNC_MAP.put("filterAbstract", m -> m.getAccessFlags().isAbstract());
		OTHER_FUNC_MAP.put("filterVarargs", m -> m.getAccessFlags().isVarArgs());
		OTHER_FUNC_MAP.put("filterNative", m -> m.getAccessFlags().isNative());
	}


	public Comparator(MethodNode mth, List<MethodNode> otherMthNodes, CustomOptions options) {
		this.mth = mth;
		this.otherMthNodes = otherMthNodes;
		this.options = options;
	}

	/**
	 * 搜索判断符合某单一条件的方法是否唯一
	 *
	 * @param retrieveFunc 检索“某单一条件”的方法引用
	 * @return 是否唯一
	 */
	public boolean isUnique(Function<MethodNode, Object> retrieveFunc) {
		Object targetValue = retrieveFunc.apply(mth);
		return otherMthNodes.stream().noneMatch(m -> retrieveFunc.apply(m) == targetValue);
	}

	/**
	 * 搜索判断符合多种并列条件的方法是否唯一
	 *
	 * @param retrieveFuncs 检索“多种并列条件”的方法引用
	 * @return 是否唯一
	 */
	public boolean isUnique(List<Function<MethodNode, Object>> retrieveFuncs) {
		List<Object> targetValues = retrieveFuncs.stream().map(f -> f.apply(mth)).collect(Collectors.toList());
		return otherMthNodes.stream().noneMatch(m -> {
			List<Object> values = retrieveFuncs.stream().map(f -> f.apply(m)).collect(Collectors.toList());

			return IntStream.range(0, values.size())
					.allMatch(i -> values.get(i).equals(targetValues.get(i)));
		});
	}

	public String searchUniqueModifier() {
		// standalone
		for (Map.Entry<String, Function<MethodNode, Object>> entry : ACCESS_FUNC_MAP.entrySet()) {
			LOG.info("Check: {}", entry.getKey());
			if (isUnique(entry.getValue())) {
				LOG.info("Got unique: {}", entry.getKey());
				return "\n    ." + entry.getKey() + "()";
			}
		}

		// will combine
		for (int i = 1; i <= OTHER_FUNC_MAP.size(); i++) {
			LOG.info("Searching for modifier {} of {}", i, otherMthNodes.size());
			List<List<Map.Entry<String, Function<MethodNode, Object>>>> allCombinations = Utils.getAllCombinations(new ArrayList<>(OTHER_FUNC_MAP.entrySet()), i);
			LOG.info("Got {} combinations", allCombinations.size());
			for (List<Map.Entry<String, Function<MethodNode, Object>>> allCombination : allCombinations) {
				// 检查每个组合是否唯一
				List<Function<MethodNode, Object>> collect = allCombination.stream().map(Map.Entry::getValue).collect(Collectors.toList());
				LOG.info("Check: {}", allCombination.stream().map(e -> "\n    ." + e.getKey() + "()").collect(Collectors.joining()));
				if (isUnique(collect)) {
					LOG.info("Got unique: 获取到唯一标识符");
					return allCombination.stream().map(e -> "\n    ." + e.getKey() + "()").collect(Collectors.joining());
				}
			}

		}

		return "";
	}

	public String searchUniqueReturnType() {
		ArgType mthReturnType = mth.getReturnType();
		if (isUnique(m -> m.getReturnType().equals(mthReturnType))) {
			return CodeGenerator.genReturnTypeCode(mth, options.isClassOptimizationEnable());
		}
		return "";
	}
}
