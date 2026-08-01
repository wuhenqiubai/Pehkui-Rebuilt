package virtuoel.pehkui.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EvokerFangs.class)
public class EvokerFangsEntityMixin
{
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/level/Level;DDDFILnet/minecraft/world/entity/LivingEntity;)V")
	private void pehkui$construct(Level world, double x, double y, double z, float yaw, int warmup, LivingEntity owner, CallbackInfo info)
	{
		ScaleUtils.setScaleOfProjectile((Entity) (Object) this, owner);
	}
}
