package virtuoel.pehkui.util;

import java.nio.file.Path;
import java.util.List;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import virtuoel.kanos_config.api.JsonConfigHandler;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleEventCallback;
import virtuoel.pehkui.api.ScaleType;

/**
 * 平台抽象：把 fabric / neoforge 的平台差异隔离于此，使 {@code common} 模块不直接引用任何
 * 平台 API（net.fabricmc.* / net.neoforged.*），只依赖本抽象类。
 *
 * 各平台在入口装配时通过 {@link #setInstance} 注入自己的实现。
 */
public abstract class Platform
{
	public static volatile Platform INSTANCE = new Unsupported();

	/**
	 * 判断某个 mod 是否已安装。
	 */
	public abstract boolean isModLoaded(String modId);

	/**
	 * config 根目录（fabric: FabricLoader.getConfigDir；neoforge: FMLPaths.CONFIGDIR）。
	 */
	public abstract Path getConfigDir();

	/**
	 * 是否运行在开发环境（fabric: isDevelopmentEnvironment；neoforge: 非生产）。
	 */
	public abstract boolean isDevelopmentEnvironment();

	/**
	 * 当前 Minecraft 版本字符串（如 "1.21.11"）。
	 */
	public abstract String getMinecraftVersion();

	/**
	 * 把一个自定义 payload 包装成服务端发放的 packet
	 * （fabric: ServerPlayNetworking.createClientboundPacket；neoforge: new ClientboundCustomPayloadPacket）。
	 */
	public abstract Packet<?> createClientboundPacket(CustomPacketPayload payload);

	/**
	 * 触发某 scale type 的 scaleChanged 事件。
	 *
	 * <p>之所以要这一层：{@code ScaleType} 的三个事件成员在两个平台上<b>类型不同</b> ——
	 * Fabric 是 {@code net.fabricmc.fabric.api.event.Event<ScaleEventCallback>}（用
	 * {@code .invoker().onEvent(...)}），NeoForge 是 {@code java.util.Collection<ScaleEventCallback>}
	 * （用 {@code .forEach(...)}）。{@code common} 不能引用任一平台类型，只能经此转发。
	 */
	public abstract void invokeScaleChanged(ScaleType type, ScaleData data);

	/** @see #invokeScaleChanged(ScaleType, ScaleData) */
	public abstract void invokePreTick(ScaleType type, ScaleData data);

	/** @see #invokeScaleChanged(ScaleType, ScaleData) */
	public abstract void invokePostTick(ScaleType type, ScaleData data);

	/**
	 * 向某 scale type 的 scaleChanged 事件注册回调。
	 *
	 * <p>平台差异同 {@link #invokeScaleChanged}：Fabric 用 {@code .register(...)}，
	 * NeoForge 用 {@code .add(...)}。
	 */
	public abstract void registerScaleChanged(ScaleType type, ScaleEventCallback callback);

	/**
	 * 配置项访问 {@code PehkuiConfig.COMMON.*}。
	 *
	 * <p>之所以要这一层：两份 {@code PehkuiConfig} 的<b>字段描述符不同</b> ——
	 * Fabric 是 {@code java.util.function.Supplier<Boolean>}（KanosConfig），
	 * NeoForge 是 {@code net.neoforged.neoforge.common.ModConfigSpec$BooleanValue}。
	 * JVM 解析 {@code getfield} 要求描述符精确匹配，所以 {@code common} 不能直读任一方的字段，
	 * 只能经此转发。平台实现里写 {@code PehkuiConfig.COMMON.x.get()} —— 两侧源码逐字相同，
	 * 各自编译时解析到自己那份字段类型（两者都实现 {@code Supplier}，故 {@code .get()} 均成立）。
	 */
	public abstract boolean getKeepAllScalesOnRespawn();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract List<String> getScalesKeptOnRespawn();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getAccurateNetherPortals();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getEnableCommands();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getEnableDebugCommands();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getEnableScaleRules();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract int getScaleRuleCheckInterval();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract double getScaleRuleMaxScale();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaleRulesAffectPlayers();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledFallDamage();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledMotion();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledReach();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledAttack();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledDefense();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledHealth();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledItemDrops();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledProjectiles();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getScaledExplosions();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getApplyVanillaScale();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getVanillaScaleAffectsGameplay();

	/** @see #getKeepAllScalesOnRespawn() */
	public abstract boolean getVanillaScaleSyncBack();

	/**
	 * 配置项访问 {@code PehkuiConfig.COMMON.dragonMineZCompat}。
	 *
	 * <p>DragonMine Z 会在 NeoForge 的 {@code EntityEvent.Size} 里把玩家尺寸换成
	 * {@code EntityDimensions.fixed(...)}，而 vanilla 的 {@code EntityDimensions.scale()}
	 * 对 fixed 尺寸原样返回 —— Pehkui 的缩放会静默失效。开启此项后
	 * {@code ScaleUtils.getScaledDimensions} 对 fixed 尺寸改为显式构造。
	 *
	 * <p>默认开启，但只在装了 {@code dragonminez} 时才生效（见 ScaleUtils 里的 isModLoaded 判断），
	 * 以保证未装该 mod 时行为零变化。
	 *
	 * @see #getKeepAllScalesOnRespawn()
	 */
	public abstract boolean getDragonMineZCompat();

	/**
	 * 配置项访问 {@code PehkuiConfig.CLIENT.minimumCameraDepth}。
	 *
	 * @see #getKeepAllScalesOnRespawn()
	 */
	public abstract double getMinimumCameraDepth();

	/**
	 * KanosConfig 的 JSON 配置文件句柄（Fabric 用）。
	 *
	 * <p>NeoForge 侧配置走原生的 {@code ModConfigSpec}（TOML），<b>没有</b>这个句柄，返回 {@code null}。
	 * 调用方必须容忍 {@code null}（{@code ConfigSyncUtils} 的 config 命令会据此跳过注册）。
	 */
	public abstract JsonConfigHandler getConfigHandler();

	public static void setInstance(Platform platform)
	{
		INSTANCE = platform;
	}

	/** 默认占位实现（未设置时兜底，避免 NPE）。 */
	public static final class Unsupported extends Platform
	{
		@Override
		public boolean isModLoaded(String modId)
		{
			return false;
		}

		@Override
		public Path getConfigDir()
		{
			return null;
		}

		@Override
		public boolean isDevelopmentEnvironment()
		{
			return false;
		}

		@Override
		public String getMinecraftVersion()
		{
			return "";
		}

		@Override
		public Packet<?> createClientboundPacket(CustomPacketPayload payload)
		{
			return null;
		}

		@Override
		public void invokeScaleChanged(ScaleType type, ScaleData data)
		{

		}

		@Override
		public void invokePreTick(ScaleType type, ScaleData data)
		{

		}

		@Override
		public void invokePostTick(ScaleType type, ScaleData data)
		{

		}

		@Override
		public void registerScaleChanged(ScaleType type, ScaleEventCallback callback)
		{

		}

		// 配置访问：未装配时一律回落到「与配置默认值一致」的取值，
		// 使未初始化场景（如数据生成）不因读配置而行为异常。

		@Override
		public boolean getKeepAllScalesOnRespawn()
		{
			return false;
		}

		@Override
		public List<String> getScalesKeptOnRespawn()
		{
			return List.of();
		}

		@Override
		public boolean getAccurateNetherPortals()
		{
			return true;
		}

		@Override
		public boolean getEnableCommands()
		{
			return true;
		}

		@Override
		public boolean getEnableDebugCommands()
		{
			return false;
		}

		@Override
		public boolean getEnableScaleRules()
		{
			return true;
		}

		@Override
		public int getScaleRuleCheckInterval()
		{
			return 10;
		}

		@Override
		public double getScaleRuleMaxScale()
		{
			return 256;
		}

		@Override
		public boolean getScaleRulesAffectPlayers()
		{
			return false;
		}

		@Override
		public boolean getScaledFallDamage()
		{
			return true;
		}

		@Override
		public boolean getScaledMotion()
		{
			return true;
		}

		@Override
		public boolean getScaledReach()
		{
			return true;
		}

		@Override
		public boolean getScaledAttack()
		{
			return true;
		}

		@Override
		public boolean getScaledDefense()
		{
			return true;
		}

		@Override
		public boolean getScaledHealth()
		{
			return true;
		}

		@Override
		public boolean getScaledItemDrops()
		{
			return true;
		}

		@Override
		public boolean getScaledProjectiles()
		{
			return true;
		}

		@Override
		public boolean getScaledExplosions()
		{
			return true;
		}

		@Override
		public boolean getApplyVanillaScale()
		{
			return false;
		}

		@Override
		public boolean getVanillaScaleAffectsGameplay()
		{
			return false;
		}

		@Override
		public boolean getVanillaScaleSyncBack()
		{
			return false;
		}

		@Override
		public boolean getDragonMineZCompat()
		{
			return true;
		}

		@Override
		public double getMinimumCameraDepth()
		{
			return 1.0D / 32767.0D;
		}

		@Override
		public JsonConfigHandler getConfigHandler()
		{
			return null;
		}
	}
}
