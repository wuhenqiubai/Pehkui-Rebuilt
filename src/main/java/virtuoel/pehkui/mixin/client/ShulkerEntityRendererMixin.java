package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.ShulkerEntityRenderer;
import net.minecraft.client.render.entity.state.ShulkerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(ShulkerEntityRenderer.class)
public class ShulkerEntityRendererMixin
{
	@Inject(at = @At("RETURN"), method = "setupTransforms")
	private void pehkui$setupTransforms(ShulkerEntityRenderState state, MatrixStack matrices, float bodyYaw, float tickDelta, CallbackInfo info)
	{
		final PehkuiEntityRenderStateExtensions pehkuiState = (PehkuiEntityRenderStateExtensions) state;

		final Direction face = state.facing;
		if (face != Direction.DOWN)
		{
			final float h = pehkuiState.pehkui$getModelHeightScale();
			if (face != Direction.UP)
			{
				final float w = pehkuiState.pehkui$getModelWidthScale();
				if (w != 1.0F || h != 1.0F)
				{
					matrices.translate(0.0, -((1.0F - w) * 0.5F) / w, -((1.0F - h) * 0.5F) / h);
				}
			}
			else if (h != 1.0F)
			{
				matrices.translate(0.0, -(1.0F - h) / h, 0.0);
			}
		}
	}
}
