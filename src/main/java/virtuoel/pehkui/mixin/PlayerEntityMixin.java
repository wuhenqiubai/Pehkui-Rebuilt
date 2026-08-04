package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Player.class)
public abstract class PlayerEntityMixin
{
	@Inject(at = @At("RETURN"), method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;")
	private void pehkui$dropItem(ItemStack stack, boolean spread, CallbackInfoReturnable<ItemEntity> info)
	{
		final ItemEntity entity = info.getReturnValue();
		
		if (entity != null)
		{
			ScaleUtils.setScaleOfDrop(entity, (Entity) (Object) this);
			
			final float scale = ScaleUtils.getEyeHeightScale((Entity) (Object) this);
			
			if (scale != 1.0F)
			{
				final Vec3 pos = entity.position();
				
				entity.setPos(pos.x, pos.y + ((1.0F - scale) * 0.3D), pos.z);
			}
		}
	}
	
	@WrapOperation(method = "aiStep()V", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
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
	
	@ModifyExpressionValue(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = { @At(value = "CONSTANT", args = "floatValue=0.5F", ordinal = 1), @At(value = "CONSTANT", args = "floatValue=0.5F", ordinal = 2), @At(value = "CONSTANT", args = "floatValue=0.5F", ordinal = 3) })
	private float pehkui$attack$knockback(float value)
	{
		final float scale = ScaleUtils.getKnockbackScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
	
	@ModifyExpressionValue(method = "getCurrentItemAttackStrengthDelay", at = @At(value = "CONSTANT", args = "doubleValue=20.0D"))
	private double pehkui$getAttackCooldownProgressPerTick$multiplier(double value)
	{
		final float scale = ScaleUtils.getAttackSpeedScale((Entity) (Object) this);
		
		return scale != 1.0F ? value / scale : value;
	}
	
	@ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
	private float pehkui$getBlockBreakingSpeed(float original)
	{
		final float scale = ScaleUtils.getMiningSpeedScale((Entity) (Object) this);
		
		return scale != 1.0F ? original * scale : original;
	}
	
	@ModifyExpressionValue(method = "moveCloak", at = { @At(value = "CONSTANT", args = "doubleValue=10.0D"), @At(value = "CONSTANT", args = "doubleValue=-10.0D") })
	private double pehkui$updateCapeAngles$limits(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
	
	@WrapOperation(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$attack$expand(AABB obj, double x, double y, double z, Operation<AABB> original, @Local(argsOnly = true) Entity target)
	{
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale(target);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale(target);
		
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

	@ModifyExpressionValue(method = "attack(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "CONSTANT", args = "doubleValue=0.4000000059604645D"))
	private double pehkui$attack$knockback(double value)
	{
		final float scale = ScaleUtils.getKnockbackScale((Entity) (Object) this);

		return scale != 1.0F ? scale * value : value;
	}

	@ModifyReturnValue(method = "getFlyingSpeed", at = @At(value = "RETURN", ordinal = 0))
	private float pehkui$getOffGroundSpeed(float original)
	{
		final float scale = ScaleUtils.getFlightScale((Player) (Object) this);

		return scale != 1.0F ? original * scale : original;
	}

	@ModifyReturnValue(method = "blockInteractionRange", at = @At("RETURN"))
	private double pehkui$getBlockInteractionRange(double original)
	{
		final float scale = ScaleUtils.getBlockReachScale((Entity) (Object) this);
		return scale != 1.0F ? scale * original : original;
	}

	@ModifyReturnValue(method = "entityInteractionRange", at = @At("RETURN"))
	private double pehkui$getEntityInteractionRange(double original)
	{
		final float scale = ScaleUtils.getEntityReachScale((Entity) (Object) this);
		return scale != 1.0F ? scale * original : original;
	}
}
