package virtuoel.pehkui.neoforge;

import java.nio.file.Path;
import java.util.List;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import virtuoel.kanos_config.api.JsonConfigHandler;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleEventCallback;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.util.Platform;

public class PlatformImpl extends Platform
{
	@Override
	public boolean isModLoaded(String modId)
	{
		return ModList.get().isLoaded(modId);
	}

	@Override
	public Path getConfigDir()
	{
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public boolean isDevelopmentEnvironment()
	{
		return !FMLEnvironment.production;
	}

	@Override
	public String getMinecraftVersion()
	{
		// NeoForge 21.1：FMLLoader.getCurrent().getVersionInfo() 尚不存在，用静态 versionInfo()
		return FMLLoader.versionInfo().mcVersion();
	}

	@Override
	public Packet<?> createClientboundPacket(CustomPacketPayload payload)
	{
		return new ClientboundCustomPayloadPacket(payload);
	}

	// NeoForge 侧的事件是裸集合（上游 NeoForge 分支把 Fabric 的 Event 降级成了 Collection）：
	// 注册用 .add(...)、触发用 .forEach(...)

	@Override
	public void invokeScaleChanged(ScaleType type, ScaleData data)
	{
		type.getScaleChangedEvent().forEach(callback -> callback.onEvent(data));
	}

	@Override
	public void invokePreTick(ScaleType type, ScaleData data)
	{
		type.getPreTickEvent().forEach(callback -> callback.onEvent(data));
	}

	@Override
	public void invokePostTick(ScaleType type, ScaleData data)
	{
		type.getPostTickEvent().forEach(callback -> callback.onEvent(data));
	}

	@Override
	public void registerScaleChanged(ScaleType type, ScaleEventCallback callback)
	{
		type.getScaleChangedEvent().add(callback);
	}

	// 配置访问：直读本平台的 PehkuiConfig。
	// ⚠️ 这一整段与 fabric 的 PlatformImpl 逐字相同 —— 不是笔误。两侧字段类型不同
	// （Fabric: Supplier，NeoForge: ModConfigSpec$BooleanValue），但都实现 Supplier，
	// 所以 .get() 在两侧源码上都成立，各自编译时解析到自己那份。

	@Override
	public boolean getKeepAllScalesOnRespawn()
	{
		return PehkuiConfig.COMMON.keepAllScalesOnRespawn.get();
	}

	@Override
	public List<String> getScalesKeptOnRespawn()
	{
		// NeoForge 侧字段类型是 ConfigValue<List<? extends String>>，get() 回来的是通配型，
		// List.copyOf 收敛成 List<String>（本次调用只在重生时发生，开销无所谓）。
		return List.copyOf(PehkuiConfig.COMMON.scalesKeptOnRespawn.get());
	}

	@Override
	public boolean getAccurateNetherPortals()
	{
		return PehkuiConfig.COMMON.accurateNetherPortals.get();
	}

	@Override
	public boolean getEnableCommands()
	{
		return PehkuiConfig.COMMON.enableCommands.get();
	}

	@Override
	public boolean getEnableDebugCommands()
	{
		return PehkuiConfig.COMMON.enableDebugCommands.get();
	}

	@Override
	public boolean getEnableScaleRules()
	{
		return PehkuiConfig.COMMON.enableScaleRules.get();
	}

	@Override
	public int getScaleRuleCheckInterval()
	{
		return PehkuiConfig.COMMON.scaleRuleCheckInterval.get();
	}

	@Override
	public double getScaleRuleMaxScale()
	{
		return PehkuiConfig.COMMON.scaleRuleMaxScale.get();
	}

	@Override
	public boolean getScaleRulesAffectPlayers()
	{
		return PehkuiConfig.COMMON.scaleRulesAffectPlayers.get();
	}

	@Override
	public boolean getScaledFallDamage()
	{
		return PehkuiConfig.COMMON.scaledFallDamage.get();
	}

	@Override
	public boolean getScaledMotion()
	{
		return PehkuiConfig.COMMON.scaledMotion.get();
	}

	@Override
	public boolean getScaledReach()
	{
		return PehkuiConfig.COMMON.scaledReach.get();
	}

	@Override
	public boolean getScaledAttack()
	{
		return PehkuiConfig.COMMON.scaledAttack.get();
	}

	@Override
	public boolean getScaledDefense()
	{
		return PehkuiConfig.COMMON.scaledDefense.get();
	}

	@Override
	public boolean getScaledHealth()
	{
		return PehkuiConfig.COMMON.scaledHealth.get();
	}

	@Override
	public boolean getScaledItemDrops()
	{
		return PehkuiConfig.COMMON.scaledItemDrops.get();
	}

	@Override
	public boolean getScaledProjectiles()
	{
		return PehkuiConfig.COMMON.scaledProjectiles.get();
	}

	@Override
	public boolean getScaledExplosions()
	{
		return PehkuiConfig.COMMON.scaledExplosions.get();
	}

	@Override
	public boolean getApplyVanillaScale()
	{
		return PehkuiConfig.COMMON.applyVanillaScale.get();
	}

	@Override
	public boolean getVanillaScaleAffectsGameplay()
	{
		return PehkuiConfig.COMMON.vanillaScaleAffectsGameplay.get();
	}

	@Override
	public boolean getVanillaScaleSyncBack()
	{
		return PehkuiConfig.COMMON.vanillaScaleSyncBack.get();
	}

	@Override
	public boolean getDragonMineZCompat()
	{
		return PehkuiConfig.COMMON.dragonMineZCompat.get();
	}

	@Override
	public double getMinimumCameraDepth()
	{
		return PehkuiConfig.CLIENT.minimumCameraDepth.get();
	}

	@Override
	public JsonConfigHandler getConfigHandler()
	{
		// NeoForge 侧配置走原生的 ModConfigSpec（TOML），没有 KanosConfig 的 JSON 句柄。
		// 依赖它的 config 命令会据此跳过注册（见 ConfigSyncUtils.registerConfigFileCommands）。
		return null;
	}
}
