package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ThrownPotion.class)
public class PotionEntityMixin
{
	@WrapOperation(method = "applyWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$applyWater$expand(AABB obj, double x, double y, double z, Operation<AABB> original)
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
	
	@ModifyExpressionValue(method = "applyWater", at = @At(value = "CONSTANT", args = "doubleValue=16.0D"))
	private double pehkui$applyWater$maxDist(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);

		return scale != 1.0F ? scale * scale * value : value;
	}

	@WrapOperation(method = "applySplash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$applySplashPotion$expand(AABB obj, double x, double y, double z, Operation<AABB> original)
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

	@ModifyExpressionValue(method = "applySplash", at = @At(value = "CONSTANT", args = "doubleValue=16.0D"))
	private double pehkui$applySplashPotion$maxSquaredDist(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);

		return scale != 1.0F ? scale * scale * value : value;
	}

	@ModifyExpressionValue(method = "applySplash", at = @At(value = "CONSTANT", args = "doubleValue=4.0F", ordinal = 2))
	private double pehkui$applySplashPotion$maxDist(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);

		return scale != 1.0F ? scale * value : value;
	}

	@ModifyArg(method = "makeAreaOfEffectCloud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AreaEffectCloud;setRadius(F)V"))
	private float pehkui$applyLingeringPotion$setRadius(float value)
	{
		return value * ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
	}

	@ModifyArg(method = "makeAreaOfEffectCloud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AreaEffectCloud;setRadiusOnUse(F)V"))
	private float pehkui$applyLingeringPotion$setRadiusOnUse(float value)
	{
		return value * ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
	}

	@ModifyArg(method = "makeAreaOfEffectCloud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private Entity pehkui$applyLingeringPotion$entity(Entity entity)
	{
		ScaleUtils.loadScale(entity, (Entity) (Object) this);

		return entity;
	}
}
