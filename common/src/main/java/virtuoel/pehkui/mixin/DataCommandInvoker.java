package virtuoel.pehkui.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

public interface DataCommandInvoker
{
	@Mixin(DataCommands.class)
	public interface Get
	{
		@Invoker
		public static int callGetData(CommandSourceStack source, DataAccessor object)
		{
			throw new NoSuchMethodError();
		}
	}
	
	@Mixin(DataCommands.class)
	public interface Path
	{
		@Invoker
		public static int callGetData(CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path)
		{
			throw new NoSuchMethodError();
		}
	}
	
	@Mixin(DataCommands.class)
	public interface Scaled
	{
		@Invoker
		public static int callGetNumeric(CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path, double scale)
		{
			throw new NoSuchMethodError();
		}
	}
}
