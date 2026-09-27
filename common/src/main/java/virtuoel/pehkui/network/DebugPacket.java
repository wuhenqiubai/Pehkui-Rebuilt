package virtuoel.pehkui.network;

import net.minecraft.network.FriendlyByteBuf;
import virtuoel.pehkui.server.command.DebugCommand;

public class DebugPacket
{
	public final DebugCommand.PacketType type;
	
	public DebugPacket(final DebugCommand.PacketType type)
	{
		this.type = type;
	}
	
	public DebugPacket(final FriendlyByteBuf buf)
	{
		DebugCommand.PacketType read;
		
		try
		{
			read = buf.readEnum(DebugCommand.PacketType.class);
		}
		catch (Exception e)
		{
			read = null;
		}
		
		this.type = read;
	}
	
	public void write(final FriendlyByteBuf buf)
	{
		buf.writeEnum(this.type);
	}
}
