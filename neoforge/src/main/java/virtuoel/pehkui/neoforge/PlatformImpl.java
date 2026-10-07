package virtuoel.pehkui.neoforge;

import java.nio.file.Path;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
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
}
