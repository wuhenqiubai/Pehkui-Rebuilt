package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(LivingEntity.class)
public abstract class StepHeightMixin
{
	@ModifyReturnValue(method = "maxUpStep()F", at = @At("RETURN"))
	private float pehkui$maxUpStep(float original)
	{
		final float scale = ScaleUtils.getStepHeightScale((Entity) (Object) this);

		return scale != 1.0F ? original * scale : original;
	}
}
