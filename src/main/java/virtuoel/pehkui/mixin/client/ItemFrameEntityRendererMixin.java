package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin
{
	@ModifyVariable(method = "render", at = @At(value = "STORE"))
	private Vec3d pehkui$render(Vec3d value, ItemFrameEntityRenderState state, MatrixStack matrixStack, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState)
	{
		final PehkuiEntityRenderStateExtensions pehkuiState = (PehkuiEntityRenderStateExtensions) state;

		final float widthScale = pehkuiState.pehkui$getModelWidthScale();
		final float heightScale = pehkuiState.pehkui$getModelHeightScale();

		if (widthScale != 1.0F || heightScale != 1.0F)
		{
			value = value.multiply(1.0F / widthScale, 1.0F / heightScale, 1.0F / widthScale);

			final Direction facing = state.facing;

			final double widthOffset = ((0.0625D - (0.03125D * widthScale)) - 0.03125D) / widthScale;
			final double heightOffset = ((0.0625D - (0.03125D * heightScale)) - 0.03125D) / heightScale;

			value = value.add(widthOffset * facing.getOffsetX(), heightOffset * facing.getOffsetY(), widthOffset * facing.getOffsetZ());
		}

		return value;
	}
}
