package virtuoel.pehkui.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.util.PehkuiBlockStateExtensions;

@Mixin(NetherPortalBlock.class)
public abstract class PortalBlockMixin
{
	@Inject(at = @At("HEAD"), method = "entityInside", cancellable = true)
	private void pehkui$onEntityCollision(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo info)
	{
		if (PehkuiConfig.COMMON.accurateNetherPortals.get())
		{
			if (!entity.getBoundingBox().intersects(((PehkuiBlockStateExtensions) state).pehkui_getOutlineShape(world, pos).bounds().move(pos)))
			{
				info.cancel();
			}
		}
	}
}
