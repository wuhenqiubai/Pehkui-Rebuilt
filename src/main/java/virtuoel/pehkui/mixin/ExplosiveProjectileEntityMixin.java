package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ExplosiveProjectileEntity.class)
public abstract class ExplosiveProjectileEntityMixin
{
	@Shadow
	abstract void setVelocityWithAcceleration(Vec3d velocity, double accelerationPower);
	
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/World;)V")
	private void pehkui$construct(EntityType<? extends ExplosiveProjectileEntity> type, LivingEntity owner, Vec3d velocity, World world, CallbackInfo info)
	{
		final ExplosiveProjectileEntity self = (ExplosiveProjectileEntity) (Object) this;
		final float scale = ScaleUtils.setScaleOfProjectile(self, owner);
		
		if (scale != 1.0F)
		{
			self.accelerationPower *= scale;
			setVelocityWithAcceleration(velocity, self.accelerationPower);
		}
	}
}
