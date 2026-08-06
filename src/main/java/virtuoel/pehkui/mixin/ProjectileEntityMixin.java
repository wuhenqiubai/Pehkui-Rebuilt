package virtuoel.pehkui.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Projectile.class)
public abstract class ProjectileEntityMixin
{
	@ModifyArg(method = "checkLeftOwner", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
	private double pehkui$shouldLeaveOwner$expand(double value)
	{
		final float width = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		final float height = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);

		if (width != 1.0F || height != 1.0F)
		{
			return Math.max(width, height) * value;
		}

		return value;
	}
	
	@ModifyVariable(method = "shoot(DDDFF)V", ordinal = 0, argsOnly = true, at = @At("HEAD"))
	private float pehkui$setVelocity$power(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		
		return scale != 1.0F ? value * scale : value;
	}
	
	@Inject(at = @At("HEAD"), method = "setOwner")
	private void pehkui$setOwner(@Nullable Entity entity, CallbackInfo info)
	{
		if (entity != null)
		{
			final Entity self = (Entity) (Object) this;

			if (self instanceof ThrowableProjectile)
			{
				final float heightScale = ScaleUtils.getEyeHeightScale(entity);
				if (heightScale != 1.0F)
				{
					final Vec3 pos = self.position();
					self.setPos(pos.x, pos.y + ((1.0F - heightScale) * 0.1D), pos.z);
				}
			}

			ScaleUtils.setScaleOfProjectile(self, entity);
		}
	}
}
