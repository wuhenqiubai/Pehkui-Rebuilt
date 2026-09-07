package virtuoel.pehkui.network;

import java.util.Collection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.util.ConfigSyncUtils.SyncableConfigEntry;

public class ConfigSyncPayload extends ConfigSyncPacket implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ConfigSyncPayload> ID = new CustomPacketPayload.Type<>(Pehkui.CONFIG_SYNC_PACKET);
	public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> CODEC = codec(ID);
	
	public ConfigSyncPayload(final Collection<SyncableConfigEntry<?>> configEntries)
	{
		super(configEntries);
	}
	
	public ConfigSyncPayload(final FriendlyByteBuf buf)
	{
		super(buf);
	}
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
	
	private static StreamCodec<FriendlyByteBuf, ConfigSyncPayload> codec(final CustomPacketPayload.Type<ConfigSyncPayload> id)
	{
		return CustomPacketPayload.codec(ConfigSyncPayload::write, ConfigSyncPayload::new);
	}
}
