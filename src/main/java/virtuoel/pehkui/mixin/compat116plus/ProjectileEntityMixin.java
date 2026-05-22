package virtuoel.pehkui.mixin.compat116plus;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin
{
	@WrapOperation(method = "hasLeftOwner", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;expand(D)Lnet/minecraft/util/math/Box;"))
	private Box pehkui$hasLeftOwner$expand(Box obj, double value, Operation<Box> original)
	{
		final float width = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		final float height = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);
		
		if (width != 1.0F || height != 1.0F)
		{
			return obj.expand(value * width, value * height, value * width);
		}
		
		return original.call(obj, value);
	}
	
	@ModifyVariable(method = "setVelocity(DDDFF)V", ordinal = 0, argsOnly = true, at = @At("HEAD"))
	private float pehkui$setVelocity$power(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		
		return scale != 1.0F ? value * scale : value;
	}
	
	@Inject(at = @At("HEAD"), method = "setOwner(Lnet/minecraft/entity/Entity;)V")
	private void pehkui$setOwner(@Nullable Entity entity, CallbackInfo info)
	{
		if (entity != null)
		{
			final Entity self = (Entity) (Object) this;
			
			if (self instanceof ThrownEntity)
			{
				final float heightScale = ScaleUtils.getEyeHeightScale(entity);
				if (heightScale != 1.0F)
				{
					final Vec3d pos = self.getEntityPos();
					self.setPosition(pos.x, pos.y + ((1.0F - heightScale) * 0.1D), pos.z);
				}
			}
			
			ScaleUtils.setScaleOfProjectile((Entity) (Object) this, entity);
		}
	}
}
