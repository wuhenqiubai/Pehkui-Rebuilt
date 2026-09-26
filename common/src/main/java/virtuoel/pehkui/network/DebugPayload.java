package virtuoel.pehkui.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.server.command.DebugCommand;

public class DebugPayload extends DebugPacket implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<DebugPayload> ID = new CustomPacketPayload.Type<>(Pehkui.DEBUG_PACKET);
	public static final StreamCodec<FriendlyByteBuf, DebugPayload> CODEC = codec(ID);
	
	public DebugPayload(final DebugCommand.PacketType type)
	{
		super(type);
	}
	
	public DebugPayload(final FriendlyByteBuf buf)
	{
		super(buf);
	}
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
	
	private static StreamCodec<FriendlyByteBuf, DebugPayload> codec(final CustomPacketPayload.Type<DebugPayload> id)
	{
		return CustomPacketPayload.codec(DebugPayload::write, DebugPayload::new);
	}
}
