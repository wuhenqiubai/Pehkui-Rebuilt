package virtuoel.pehkui.neoforge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractBoat.class)
public abstract class ChestBoatEntityMixin
{
	@ModifyReturnValue(method = "getSinglePassengerXOffset", at = @At("RETURN"))
	private float pehkui$getPassengerHorizontalOffset(float original)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
		
		return scale != 1.0F ? original * scale : original;
	}
}
