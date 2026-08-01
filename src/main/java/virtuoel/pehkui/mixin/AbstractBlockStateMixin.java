package virtuoel.pehkui.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import virtuoel.pehkui.util.PehkuiBlockStateExtensions;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin implements PehkuiBlockStateExtensions
{
	@Shadow
	abstract VoxelShape getShape(BlockGetter world, BlockPos pos);
	
	@Override
	public VoxelShape pehkui_getOutlineShape(BlockGetter world, BlockPos pos)
	{
		return getShape(world, pos);
	}
	
	@Shadow
	abstract Block getBlock();
	
	@Override
	public Block pehkui_getBlock()
	{
		return getBlock();
	}
}
