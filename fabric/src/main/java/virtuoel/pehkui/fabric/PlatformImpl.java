package virtuoel.pehkui.fabric;

import java.nio.file.Path;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
}
