package virtuoel.pehkui.neoforge.config;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import virtuoel.pehkui.Pehkui;

/**
 * Client-only entry point that registers the Pehkui config screen with the
 * NeoForge mod list (Mods screen -> Pehkui -> config button).
 *
 * <p>NeoForge 侧的配置是原生的 {@code ModConfigSpec}（TOML），所以直接用 NeoForge 自带的
 * {@link ConfigurationScreen} 渲染 —— 它按 spec 结构逐项生成界面，翻译键取自
 * {@code PehkuiConfig} 里那些 {@code .translation("pehkui.configgui.*")}。
 * 不需要（也不该）复用 common 里那个读 KanosConfig 的 {@code PehkuiConfigScreen}：
 * 那个依赖 {@code PehkuiConfig.BUILDER}，而 NeoForge 那份没有该字段。
 */
@Mod(value = Pehkui.MOD_ID, dist = Dist.CLIENT)
public class ConfigIntegration
{
	public ConfigIntegration(final ModContainer container)
	{
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
}
