package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.math.Vec3d;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin
{
	@ModifyReturnValue(method = "getPositionOffset", at = @At("RETURN"))
	private Vec3d pehkui$getPositionOffset(Vec3d original, PlayerEntityRenderState state)
	{
		if (original != Vec3d.ZERO)
		{
			final float scale = ((PehkuiEntityRenderStateExtensions) state).pehkui$getModelHeightScale();
			return original.multiply(scale);
		}

		return original;
	}
}
