package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.decoration.BlockAttachedEntity;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin
{
	@Shadow
	protected abstract void updateAttachmentPosition();
	
	@Inject(at = @At("RETURN"), method = "calculateDimensions")
	private void pehkui$calculateDimensions(CallbackInfo info)
	{
		updateAttachmentPosition();
	}
}
