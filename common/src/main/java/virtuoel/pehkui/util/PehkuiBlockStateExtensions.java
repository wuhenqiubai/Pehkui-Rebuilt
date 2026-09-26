package virtuoel.pehkui.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface PehkuiBlockStateExtensions
{
	VoxelShape pehkui_getOutlineShape(BlockGetter world, BlockPos pos);
	
	Block pehkui_getBlock();
}
