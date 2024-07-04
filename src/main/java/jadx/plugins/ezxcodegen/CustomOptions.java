package jadx.plugins.ezxcodegen;

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;

public class CustomOptions extends BasePluginOptionsBuilder {

	private boolean enable;
	private boolean methodAccessEnable;
	private boolean methodReturnEnable;
	private boolean classOptimizationEnable;
	private boolean debugEnable;

	@Override
	public void registerOptions() {
		boolOption(EzxCodegenPlugin.PLUGIN_ID + ".enable")
				.description("启用上下文菜单 / enable pop menu")
				.defaultValue(true)
				.setter(v -> enable = v);
		boolOption(EzxCodegenPlugin.PLUGIN_ID + ".method.access.enable")
				.description("强制生成过滤访问修饰符代码 / force generation of filter access modifier code")
				.defaultValue(false)
				.setter(v -> methodAccessEnable = v);
		boolOption(EzxCodegenPlugin.PLUGIN_ID + ".method.return.enable")
				.description("强制生成过滤返回值类型代码 / force generation of filter return value type code")
				.defaultValue(false)
				.setter(v -> methodReturnEnable = v);
		boolOption(EzxCodegenPlugin.PLUGIN_ID + ".class.optimization.enable")
				.description("尝试优化类对象代码的生成 / try to optimize the generation of class object code")
				.defaultValue(true)
				.setter(v -> classOptimizationEnable = v);
		boolOption(EzxCodegenPlugin.PLUGIN_ID + ".debug.enable")
				.description("调试模式 / debug mode")
				.defaultValue(false)
				.setter(v -> debugEnable = v);
	}

	public boolean isEnable() {
		return enable;
	}

	public boolean isEnableMethodAccess() {
		return methodAccessEnable;
	}

	public boolean isEnableMethodReturnType() {
		return methodReturnEnable;
	}

	public boolean isDebugEnable() {
		return debugEnable;
	}

	public boolean isClassOptimizationEnable() {
		return classOptimizationEnable;
	}

	@Override
	public String toString() {
		return "CustomOptions{" +
				"enable=" + enable +
				", methodAccessEnable=" + methodAccessEnable +
				", methodReturnEnable=" + methodReturnEnable +
				", classOptimizationEnable=" + classOptimizationEnable +
				", debugEnable=" + debugEnable +
				'}';
	}
}
