package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Mob.class)
public abstract class MobEntityMixin
{
	@ModifyExpressionValue(method = "doHurtTarget(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
	private float pehkui$tryAttack$knockback(float value)
	{
		final float scale = ScaleUtils.getKnockbackScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
	
	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$tickMovement$expand(AABB obj, double x, double y, double z, Operation<AABB> original)
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

	@Inject(at = @At("RETURN"), method = "convertTo")
	private <T extends Mob> void pehkui$convertTo(EntityType<T> entityType, ConversionParams context, EntitySpawnReason reason, ConversionParams.AfterConversion<T> finalizer, CallbackInfoReturnable<T> info)
	{
		final Mob e = info.getReturnValue();

		if (e != null)
		{
			ScaleUtils.loadScale(e, (Entity) (Object) this);
		}
	}

	@WrapOperation(method = "getAttackBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$getAttackBox$expand(AABB obj, double x, double y, double z, Operation<AABB> original)
	{
		final float scale = ScaleUtils.getEntityReachScale((Entity) (Object) this);

		if (scale != 1.0F)
		{
			x *= scale;
			z *= scale;
		}

		return original.call(obj, x, y, z);
	}
}
