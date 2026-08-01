package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.arguments.OperationArgument;

@Mixin(OperationArgument.class)
public interface OperationArgumentTypeAccessor
{
	@Accessor("ERROR_INVALID_OPERATION")
	static SimpleCommandExceptionType getInvalidOperationException()
	{
		throw new UnsupportedOperationException();
	}
	
	@Accessor("ERROR_DIVIDE_BY_ZERO")
	static SimpleCommandExceptionType getDivisionZeroException()
	{
		throw new UnsupportedOperationException();
	}
}
