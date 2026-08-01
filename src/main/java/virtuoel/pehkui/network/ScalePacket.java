package virtuoel.pehkui.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import io.netty.buffer.ByteBuf;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.util.ScaleUtils;

public class ScalePacket
{
	public final int entityId;
	public final Collection<ScaleData> scales = new ArrayList<>();
	public final Map<Identifier, CompoundTag> syncedScales = new HashMap<>();
	
	public ScalePacket(final Entity entity, final Collection<ScaleData> scales)
	{
		entityId = entity.getId();
		this.scales.addAll(scales);
	}
	
	public ScalePacket(final FriendlyByteBuf buf)
	{
		entityId = buf.readVarInt();
		
		for (int i = buf.readInt(); i > 0; i--)
		{
			final Identifier typeId = buf.readIdentifier();
			
			final CompoundTag scaleData = ScaleUtils.buildScaleNbtFromPacketByteBuf(buf);
			
			syncedScales.put(typeId, scaleData);
		}
	}
	
	public void write(final FriendlyByteBuf buf)
	{
		buf.writeVarInt(entityId);
		((ByteBuf) buf).writeInt(scales.size());
		
		for (final ScaleData s : scales)
		{
			buf.writeIdentifier(ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, s.getScaleType()));
			s.toPacket(buf);
		}
	}
}
