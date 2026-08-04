package virtuoel.pehkui.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameEntityRendererMixin
{
	@ModifyVariable(method = "submit", at = @At(value = "STORE"))
	private Vec3 pehkui$render(Vec3 value, ItemFrameRenderState state, PoseStack matrixStack, SubmitNodeCollector queue, CameraRenderState cameraRenderState)
	{
		final PehkuiEntityRenderStateExtensions pehkuiState = (PehkuiEntityRenderStateExtensions) state;

		final float widthScale = pehkuiState.pehkui$getModelWidthScale();
		final float heightScale = pehkuiState.pehkui$getModelHeightScale();

		if (widthScale != 1.0F || heightScale != 1.0F)
		{
			value = value.multiply(1.0F / widthScale, 1.0F / heightScale, 1.0F / widthScale);

			final Direction facing = state.direction;

			final double widthOffset = ((0.0625D - (0.03125D * widthScale)) - 0.03125D) / widthScale;
			final double heightOffset = ((0.0625D - (0.03125D * heightScale)) - 0.03125D) / heightScale;

			value = value.add(widthOffset * facing.getStepX(), heightOffset * facing.getStepY(), widthOffset * facing.getStepZ());
		}

		return value;
	}
}
