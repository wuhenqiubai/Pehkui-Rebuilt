package virtuoel.pehkui.mixin.client.compat12110plus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.network.ClientPlayerLikeState;
import virtuoel.pehkui.util.PehkuiClientPlayerLikeStateScale;

@Mixin(ClientPlayerLikeState.class)
public class ClientPlayerLikeStateMixin
{
	@ModifyExpressionValue(method = "setPos", at = { @At(value = "CONSTANT", args = "doubleValue=10.0D"), @At(value = "CONSTANT", args = "doubleValue=-10.0D") })
	private double pehkui$setPos$limits(double value)
	{
		final float scale = PehkuiClientPlayerLikeStateScale.getMotionScale();
		
		return scale != 1.0F ? scale * value : value;
	}
}
