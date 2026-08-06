package virtuoel.pehkui.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(ShulkerRenderer.class)
public class ShulkerEntityRendererMixin
{
	@Inject(at = @At("RETURN"), method = "setupRotations")
	private void pehkui$setupTransforms(ShulkerRenderState state, PoseStack matrices, float bodyYaw, float tickDelta, CallbackInfo info)
	{
		final PehkuiEntityRenderStateExtensions pehkuiState = (PehkuiEntityRenderStateExtensions) state;

		final Direction face = state.attachFace;
		if (face != Direction.DOWN)
		{
			final float h = pehkuiState.getModelHeightScale();
			if (face != Direction.UP)
			{
				final float w = pehkuiState.getModelWidthScale();
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
