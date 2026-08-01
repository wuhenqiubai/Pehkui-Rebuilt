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
	interface Get
	{
		@Invoker
		static int callGetData(CommandSourceStack source, DataAccessor object)
		{
			throw new NoSuchMethodError();
		}
	}
	
	@Mixin(DataCommands.class)
	interface Path
	{
		@Invoker
		static int callGetData(CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path)
		{
			throw new NoSuchMethodError();
		}
	}
	
	@Mixin(DataCommands.class)
	interface Scaled
	{
		@Invoker
		static int callGetNumeric(CommandSourceStack source, DataAccessor object, NbtPathArgument.NbtPath path, double scale)
		{
			throw new NoSuchMethodError();
		}
	}
}
