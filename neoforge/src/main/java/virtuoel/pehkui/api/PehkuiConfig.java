package virtuoel.pehkui.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.util.ClampingScaleModifier;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * NeoForge 侧的配置，形态<b>照抄上游 Pehkui 的 neoforge 分支</b>（{@code ModConfigSpec} / TOML）。
 *
 * <p>上游 Pehkui 不是 Architectury 项目 —— 每个 loader 一条 git 分支、各持一份完整源码树，
 * 所以 {@code PehkuiConfig} 在两个平台上<b>本就是两套实现</b>：Fabric 是 KanosConfig（JSON，
 * 字段类型 {@code Supplier<T>}），NeoForge 是 {@code ModConfigSpec}（TOML，字段类型
 * {@code ModConfigSpec$*Value}）。JVM 的 {@code getfield} 要求描述符精确匹配，两者无法共存，
 * 故本文件与 {@code fabric/.../api/PehkuiConfig.java} 各存一份、各自对齐各自的上游。
 *
 * <p>结构差异（与上游一致，非疏漏）：scale 上下限在 Fabric 上属于 {@code Common}，
 * 在 NeoForge 上属于 {@code Server} 的 {@code scale_limits} 段。
 *
 * <p>Rebuilt 相对上游新增的 7 个配置项（scale rules 与 vanilla scale 兼容）追加在
 * {@code Common} 末尾 —— 上游没有，但 Rebuilt 自己的代码要读，缺了会 {@code NoSuchFieldError}。
 */
public class PehkuiConfig
{
	@ApiStatus.Internal
	public static final ModConfigSpec clientSpec;
	public static final Client CLIENT;
	@ApiStatus.Internal
	public static final ModConfigSpec commonSpec;
	public static final Common COMMON;
	@ApiStatus.Internal
	public static final ModConfigSpec serverSpec;
	public static final Server SERVER;

	@ApiStatus.Internal
	@SubscribeEvent
	public static void onLoad(ModConfigEvent.Loading configEvent)
	{
		Pehkui.LOGGER.debug(
			"Loaded Pehkui config file {}", configEvent.getConfig().getFileName()
		);
	}

	@ApiStatus.Internal
	@SubscribeEvent
	public static void onFileChange(ModConfigEvent.Reloading configEvent)
	{
		Pehkui.LOGGER.debug(
			"Pehkui config just got changed on the file system!"
		);
	}

	static
	{
		final ModConfigSpec.Builder client, common, server;
		client = new ModConfigSpec.Builder();
		common = new ModConfigSpec.Builder();
		server = new ModConfigSpec.Builder();

		CLIENT = new Client(client);
		COMMON = new Common(common);
		SERVER = new Server(server);

		clientSpec = client.build();
		commonSpec = common.build();
		serverSpec = server.build();
	}

	public static class Client
	{
		public final ModConfigSpec.DoubleValue minimumCameraDepth;

		Client(ModConfigSpec.Builder builder)
		{
			builder
				.comment("Client only settings, mostly things related to rendering")
				.push("client");
			this.minimumCameraDepth = builder
				.translation("pehkui.configgui.minimumCameraDepth")
				.defineInRange("minimumCameraDepth", 1.0D / 32767.0D, 0.0D, 0.05D);
			builder.pop();
		}
	}

	public static class Common
	{
		public final ModConfigSpec.BooleanValue scaledFallDamage;
		public final ModConfigSpec.BooleanValue scaledMotion;
		public final ModConfigSpec.BooleanValue scaledReach;
		public final ModConfigSpec.BooleanValue scaledAttack;
		public final ModConfigSpec.BooleanValue scaledDefense;
		public final ModConfigSpec.BooleanValue scaledHealth;
		public final ModConfigSpec.BooleanValue scaledItemDrops;
		public final ModConfigSpec.BooleanValue scaledProjectiles;
		public final ModConfigSpec.BooleanValue scaledExplosions;
		public final ModConfigSpec.BooleanValue keepAllScalesOnRespawn;
		public final ModConfigSpec.ConfigValue<List<? extends String>> scalesKeptOnRespawn;
		public final ModConfigSpec.BooleanValue accurateNetherPortals;
		public final ModConfigSpec.BooleanValue enableCommands;
		public final ModConfigSpec.BooleanValue enableDebugCommands;
		// ===== 以下 7 项为 Rebuilt 扩展（上游没有）=====
		public final ModConfigSpec.BooleanValue enableScaleRules;
		public final ModConfigSpec.IntValue scaleRuleCheckInterval;
		public final ModConfigSpec.DoubleValue scaleRuleMaxScale;
		public final ModConfigSpec.BooleanValue scaleRulesAffectPlayers;
		public final ModConfigSpec.BooleanValue applyVanillaScale;
		public final ModConfigSpec.BooleanValue vanillaScaleAffectsGameplay;
		public final ModConfigSpec.BooleanValue vanillaScaleSyncBack;

		Common(ModConfigSpec.Builder builder)
		{
			builder
				.comment("General configuration settings")
				.push("general");
			this.scaledFallDamage = builder
				.translation("pehkui.configgui.scaledFallDamage")
				.define("scaledFallDamage", true);
			this.scaledMotion = builder
				.translation("pehkui.configgui.scaledMotion")
				.define("scaledMotion", true);
			this.scaledReach = builder
				.translation("pehkui.configgui.scaledReach")
				.define("scaledReach", true);
			this.scaledAttack = builder
				.translation("pehkui.configgui.scaledAttack")
				.define("scaledAttack", true);
			this.scaledDefense = builder
				.translation("pehkui.configgui.scaledDefense")
				.define("scaledDefense", true);
			this.scaledHealth = builder
				.translation("pehkui.configgui.scaledHealth")
				.define("scaledHealth", true);
			this.scaledItemDrops = builder
				.translation("pehkui.configgui.scaledItemDrops")
				.define("scaledItemDrops", true);
			this.scaledProjectiles = builder
				.translation("pehkui.configgui.scaledProjectiles")
				.define("scaledProjectiles", true);
			this.scaledExplosions = builder
				.translation("pehkui.configgui.scaledExplosions")
				.define("scaledExplosions", true);
			this.keepAllScalesOnRespawn = builder
				.translation("pehkui.configgui.keepAllScalesOnRespawn")
				.define("keepAllScalesOnRespawn", false);
			this.accurateNetherPortals = builder
				.translation("pehkui.configgui.accurateNetherPortals")
				.define("accurateNetherPortals", true);
			this.scalesKeptOnRespawn = builder
				.translation("pehkui.configgui.scalesKeptOnRespawn")
				.defineListAllowEmpty(
					path("scalesKeptOnRespawn"),
					ArrayList::new,
					s ->
					{
						final Identifier id = Identifier.tryParse(String.valueOf(s));

						if (id == null)
						{
							return false;
						}

						final ScaleType scaleType = ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, id);

						final Identifier defaultId = ScaleRegistries.getDefaultId(ScaleRegistries.SCALE_TYPES);
						final ScaleType defaultType = ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, defaultId);

						return scaleType != null && (scaleType != defaultType || id.equals(defaultId));
					}
				);
			this.enableCommands = builder
				.translation("pehkui.configgui.enableCommands")
				.define("enableCommands", true);
			this.enableDebugCommands = builder
				.translation("pehkui.configgui.enableDebugCommands")
				.define("enableDebugCommands", false);

			// ===== Rebuilt 扩展 =====
			this.enableScaleRules = builder
				.translation("pehkui.configgui.enableScaleRules")
				.define("enableScaleRules", true);
			this.scaleRuleCheckInterval = builder
				.translation("pehkui.configgui.scaleRuleCheckInterval")
				.defineInRange("scaleRuleCheckInterval", 10, 1, Integer.MAX_VALUE);
			this.scaleRuleMaxScale = builder
				.translation("pehkui.configgui.scaleRuleMaxScale")
				.defineInRange("scaleRuleMaxScale", 256.0D, 0.0D, Float.MAX_VALUE);
			this.scaleRulesAffectPlayers = builder
				.translation("pehkui.configgui.scaleRulesAffectPlayers")
				.define("scaleRulesAffectPlayers", false);
			this.applyVanillaScale = builder
				.translation("pehkui.configgui.applyVanillaScale")
				.define("applyVanillaScale", false);
			this.vanillaScaleAffectsGameplay = builder
				.translation("pehkui.configgui.vanillaScaleAffectsGameplay")
				.define("vanillaScaleAffectsGameplay", false);
			this.vanillaScaleSyncBack = builder
				.translation("pehkui.configgui.vanillaScaleSyncBack")
				.define("vanillaScaleSyncBack", false);

			builder.pop();
		}
	}

	public static class Server
	{
		Server(ModConfigSpec.Builder builder)
		{
			builder
				.comment("Server configuration settings")
				.push("server");

			builder.push("scale_limits");

			Identifier id;
			String namespace, path;
			ScaleType type;
			double defaultMin, defaultMax;
			ModConfigSpec.DoubleValue min, max;
			for (final Map.Entry<Identifier, ScaleType> entry : ScaleRegistries.SCALE_TYPES.entrySet())
			{
				id = entry.getKey();
				namespace = id.getNamespace();

				if (namespace.equals(Pehkui.MOD_ID))
				{
					type = entry.getValue();

					if (type == ScaleTypes.INVALID)
					{
						continue;
					}

					path = id.getPath();

					defaultMin = type.getAffectsDimensions() ? ScaleUtils.DEFAULT_MINIMUM_POSITIVE_SCALE : Float.MIN_VALUE;
					// pehkui:base 的默认上限单列：倍数太大时下游（实体查询、渲染、物理）都会出问题，
					// 50 倍是实测可用的上限。其余类型仍用 Float.MAX_VALUE。
					defaultMax = type == ScaleTypes.BASE ? 50.0D : Float.MAX_VALUE;

					builder.push(path);
					min = builder
						.translation("pehkui.configgui.scale_limits." + path + ".minimum")
						.defineInRange("minimum", defaultMin, Float.MIN_VALUE, Float.MAX_VALUE);
					max = builder
						.translation("pehkui.configgui.scale_limits." + path + ".maximum")
						.defineInRange("maximum", defaultMax, Float.MIN_VALUE, Float.MAX_VALUE);

					type.getDefaultBaseValueModifiers().add(
						ScaleRegistries.register(
							ScaleRegistries.SCALE_MODIFIERS,
							Pehkui.id("clamping", path),
							new ClampingScaleModifier(min::get, max::get, 0.0F)
						)
					);

					builder.pop();
				}
			}
			builder.pop();

			builder.pop();
		}
	}

	@SafeVarargs
	private static <E> ArrayList<E> path(final E... elements)
	{
		final ArrayList<E> list = new ArrayList<E>();
		Collections.addAll(list, elements);
		return list;
	}
}
