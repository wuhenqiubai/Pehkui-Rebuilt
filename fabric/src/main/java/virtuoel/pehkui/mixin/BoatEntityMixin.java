package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractBoat.class)
public abstract class BoatEntityMixin
{
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$tick$expand(AABB obj, double x, double y, double z, Operation<AABB> original)
	{
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);
		
		if (widthScale != 1.0F)
		{
			x *= widthScale;
			z *= widthScale;
		}
		
		if (heightScale != 1.0F)
		{
			y *= heightScale;
		}
		
		return original.call(obj, x, y, z);
	}
	
	@ModifyArg(method = "checkInWater", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/util/Mth;ceil(D)I"))
	private double pehkui$checkBoatInWater$offset(double value)
	{
		final Entity self = (Entity) (Object) this;
		final float scale = ScaleUtils.getBoundingBoxHeightScale(self);
		
		if (scale != 1.0F)
		{
			final double minY = self.getBoundingBox().minY;
			
			return minY + (scale * (value - minY));
		}
		
		return value;
	}
	
	@ModifyVariable(method = "isUnderwater", at = @At(value = "STORE"))
	private double pehkui$getUnderWaterLocation$offset(double value)
	{
		final Entity self = (Entity) (Object) this;
		final float scale = ScaleUtils.getBoundingBoxHeightScale(self);
		
		if (scale > 1.0F)
		{
			final double maxY = self.getBoundingBox().maxY;
			
			return maxY + (scale * (value - maxY));
		}
		
		return value;
	}
	
	@ModifyExpressionValue(method = "floatBoat", at = @At(value = "CONSTANT", args = "doubleValue=-7.0E-4D"))
	private double pehkui$floatBoat$sinking(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale != 1.0F ? scale * value : value;
	}

	@ModifyExpressionValue(method = "floatBoat", at = @At(value = "CONSTANT", args = "doubleValue=0.65D"))
	private double pehkui$floatBoat$multiplier(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale != 1.0F ? value / scale : value;
	}
}
