package jadx.plugins.ezxcodegen;

import jadx.api.JadxDecompiler;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.gui.JadxGuiContext;

public class EzxCodegenPlugin implements JadxPlugin {
	public static final String PLUGIN_ID = "ezx-codegen-plugin";

	private final CustomOptions options = new CustomOptions();

	@Override
	public JadxPluginInfo getPluginInfo() {
		return new JadxPluginInfo(PLUGIN_ID, "Ezxhelper Codegen", "Ezxhelper hook 代码生成器。\nEzxhelper hook code generator.");
	}

	public Boolean canGen(ICodeNodeRef nodeRef) {
		// todo 检查是否可以生成代码，貌似不必判断
		return true;
	}

	@Override
	public void init(JadxPluginContext context) {
		context.registerOptions(options);
		if (options.isEnable()) {
			JadxDecompiler decompiler = context.getDecompiler();
			JadxGuiContext guiContext = context.getGuiContext();
			if (guiContext != null) {
				CodeGenerator generator = new CodeGenerator(guiContext, decompiler, options);
				guiContext.addPopupMenuAction("生成 EzxHelper 代码", this::canGen, null, generator);
			}
		}
	}
}
