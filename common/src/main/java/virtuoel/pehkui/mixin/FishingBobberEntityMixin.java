package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(FishingHook.class)
public abstract class FishingBobberEntityMixin
{
	@Shadow
	public abstract Player getPlayerOwner();
	
	@ModifyExpressionValue(method = "shouldStopFishing", at = @At(value = "CONSTANT", args = "doubleValue=1024.0D"))
	private double pehkui$removeIfInvalid$distance(double value)
	{
		final float scale = ScaleUtils.getProjectileScale(getPlayerOwner());
		
		if (scale != 1.0F)
		{
			return value * scale * scale;
		}
		
		return value;
	}
}
