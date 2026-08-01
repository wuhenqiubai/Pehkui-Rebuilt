package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ThrownEntity.class)
public abstract class ThrownEntityMixin
{
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V")
	private void pehkui$construct(EntityType<? extends ThrownEntity> type, LivingEntity owner, World world, CallbackInfo info)
	{
		final float heightScale = ScaleUtils.getEyeHeightScale(owner);
		if (heightScale != 1.0F)
		{
			final Entity self = ((Entity) (Object) this);
			
			final Vec3d pos = self.getPos();
			
			self.setPosition(pos.x, pos.y + ((1.0F - heightScale) * 0.1D), pos.z);
		}
		
		ScaleUtils.setScaleOfProjectile((Entity) (Object) this, owner);
	}
}
