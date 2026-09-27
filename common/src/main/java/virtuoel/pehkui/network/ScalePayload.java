package virtuoel.pehkui.network;

import java.util.Collection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleData;

public class ScalePayload extends ScalePacket implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ScalePayload> ID = new CustomPacketPayload.Type<>(Pehkui.SCALE_PACKET);
	public static final StreamCodec<FriendlyByteBuf, ScalePayload> CODEC = codec(ID);
	
	public ScalePayload(final Entity entity, final Collection<ScaleData> scales)
	{
		super(entity, scales);
	}
	
	public ScalePayload(final FriendlyByteBuf buf)
	{
		super(buf);
	}
	
	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
	
	private static StreamCodec<FriendlyByteBuf, ScalePayload> codec(final CustomPacketPayload.Type<ScalePayload> id)
	{
		return CustomPacketPayload.codec(ScalePayload::write, ScalePayload::new);
	}
}
