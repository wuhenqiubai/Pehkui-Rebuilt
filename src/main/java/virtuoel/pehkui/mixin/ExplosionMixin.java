package virtuoel.pehkui.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ServerExplosion.class)
public abstract class ExplosionMixin
{
	@ModifyVariable(method = "<init>", ordinal = 0, argsOnly = true, at = @At("HEAD"))
	private static float pehkui$construct$radius(float value, ServerLevel level, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator calculator, Vec3 pos, float power, boolean fire, Explosion.BlockInteraction interaction)
	{
		if (entity != null)
		{
			final float scale = ScaleUtils.getExplosionScale(entity);

			if (scale != 1.0F)
			{
				return value * scale;
			}
		}

		return value;
	}
}
