package virtuoel.pehkui.neoforge;

import java.nio.file.Path;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
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
		return !FMLEnvironment.isProduction();
	}

	@Override
	public String getMinecraftVersion()
	{
		return FMLLoader.getCurrent().getVersionInfo().mcVersion();
	}

	@Override
	public Packet<?> createClientboundPacket(CustomPacketPayload payload)
	{
		return new ClientboundCustomPayloadPacket(payload);
	}
}
