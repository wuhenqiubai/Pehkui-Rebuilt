package virtuoel.pehkui.mixin.compat1205plus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin
{
	@ModifyExpressionValue(method = "updateVelocity", at = @At(value = "CONSTANT", args = "doubleValue=0.65D"))
	private double pehkui$updateVelocity$multiplier(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		
		return scale != 1.0F ? value / scale : value;
	}
}
