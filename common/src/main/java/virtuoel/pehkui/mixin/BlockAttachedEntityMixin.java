package virtuoel.pehkui.mixin;

import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin
{
	@Shadow
	protected abstract void recalculateBoundingBox();
	
	@Inject(at = @At("RETURN"), method = "refreshDimensions")
	private void pehkui$calculateDimensions(CallbackInfo info)
	{
		recalculateBoundingBox();
	}
}
