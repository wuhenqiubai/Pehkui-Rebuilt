package virtuoel.pehkui.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractHurtingProjectile.class)
public abstract class ExplosiveProjectileEntityMixin
{
	@Shadow
	protected abstract void assignDirectionalMovement(Vec3 velocity, double accelerationPower);
	
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/Level;)V")
	private void pehkui$construct(EntityType<? extends AbstractHurtingProjectile> type, LivingEntity owner, Vec3 velocity, Level world, CallbackInfo info)
	{
		final AbstractHurtingProjectile self = (AbstractHurtingProjectile) (Object) this;
		final float scale = ScaleUtils.setScaleOfProjectile(self, owner);
		
		if (scale != 1.0F)
		{
			self.accelerationPower *= scale;
			assignDirectionalMovement(velocity, self.accelerationPower);
		}
	}
}
