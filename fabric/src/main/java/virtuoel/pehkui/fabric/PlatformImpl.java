package virtuoel.pehkui.fabric;

import java.nio.file.Path;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public Path getConfigDir()
	{
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	public boolean isDevelopmentEnvironment()
	{
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public String getMinecraftVersion()
	{
		return FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString();
	}

	@Override
	public Packet<?> createClientboundPacket(CustomPacketPayload payload)
	{
		return ServerPlayNetworking.createS2CPacket(payload);
	}

	// Fabric 的事件是 net.fabricmc.fabric.api.event.Event：注册用 .register(...)、触发用 .invoker().onEvent(...)

	@Override
	public void invokeScaleChanged(ScaleType type, ScaleData data)
	{
		type.getScaleChangedEvent().invoker().onEvent(data);
	}

	@Override
	public void invokePreTick(ScaleType type, ScaleData data)
	{
		type.getPreTickEvent().invoker().onEvent(data);
	}

	@Override
	public void invokePostTick(ScaleType type, ScaleData data)
	{
		type.getPostTickEvent().invoker().onEvent(data);
	}

	@Override
	public void registerScaleChanged(ScaleType type, ScaleEventCallback callback)
	{
		type.getScaleChangedEvent().register(callback);
	}

	// 配置访问：直读本平台的 PehkuiConfig。
	// ⚠️ 这一整段与 neoforge 的 PlatformImpl 逐字相同 —— 不是笔误。两侧字段类型不同
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
		return PehkuiConfig.COMMON.scalesKeptOnRespawn.get();
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
		return PehkuiConfig.BUILDER.config;
	}
}
