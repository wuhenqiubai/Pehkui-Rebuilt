package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(DragonFireball.class)
public abstract class DragonFireballEntityMixin
{
	@ModifyExpressionValue(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At(value = "CONSTANT", args = "doubleValue=4.0D"))
	private double pehkui$onCollision$width(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
	
	@ModifyExpressionValue(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At(value = "CONSTANT", args = "doubleValue=2.0D"))
	private double pehkui$onCollision$height(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);
		
		return scale != 1.0F ? scale * value : value;
	}
}
