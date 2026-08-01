package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin
{
	@ModifyExpressionValue(method = "tryAttack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getAttackKnockbackAgainst(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;)F"))
	private float pehkui$tryAttack$knockback(float value)
	{
		final float scale = ScaleUtils.getKnockbackScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
	
	@WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(DDD)Lnet/minecraft/util/math/Box;"))
	private Box pehkui$tickMovement$expand(Box obj, double x, double y, double z, Operation<Box> original)
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
	private <T extends MobEntity> void pehkui$convertTo(EntityType<T> entityType, EntityConversionContext context, SpawnReason reason, EntityConversionContext.Finalizer<T> finalizer, CallbackInfoReturnable<T> info)
	{
		final MobEntity e = info.getReturnValue();

		if (e != null)
		{
			ScaleUtils.loadScale(e, (Entity) (Object) this);
		}
	}

	@WrapOperation(method = "getAttackBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(DDD)Lnet/minecraft/util/math/Box;"))
	private Box pehkui$getAttackBox$expand(Box obj, double x, double y, double z, Operation<Box> original)
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
