package virtuoel.pehkui.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;
import virtuoel.pehkui.util.ScaleRenderUtils;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderManagerMixin
{
	@WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V"))
	private <S extends EntityRenderState> void pehkui$render(EntityRenderer<?, ? super S> renderer, S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState, Operation<Void> original)
	{
		final PehkuiEntityRenderStateExtensions pehkuiState = (PehkuiEntityRenderStateExtensions) state;
		final float widthScale = pehkuiState.pehkui$getModelWidthScale();
		final float heightScale = pehkuiState.pehkui$getModelHeightScale();
		
		ScaleRenderUtils.logIfEntityRenderCancelled();
		
		matrices.pushPose();
		matrices.scale(widthScale, heightScale, widthScale);
		matrices.pushPose();
		
		ScaleRenderUtils.saveLastRenderedEntity(state.entityType);
		
		try
		{
			original.call(renderer, state, matrices, queue, cameraState);
		}
		finally
		{
			ScaleRenderUtils.clearLastRenderedEntity();
			
			matrices.popPose();
			matrices.popPose();
		}
	}
}
