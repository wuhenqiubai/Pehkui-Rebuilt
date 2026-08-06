package virtuoel.pehkui.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;
import virtuoel.pehkui.util.ScaleRenderUtils;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin
{
	@WrapOperation(method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
	private <E extends Entity, S extends EntityRenderState> void pehkui$render(EntityRenderer<?, ? super S> renderer, S state, PoseStack matrices, MultiBufferSource provider, int light, Operation<Void> original)
	{
		final PehkuiEntityRenderStateExtensions pehkuiState = (PehkuiEntityRenderStateExtensions) state;
		final float widthScale = pehkuiState.getModelWidthScale();
		final float heightScale = pehkuiState.getModelHeightScale();

		ScaleRenderUtils.logIfEntityRenderCancelled();

		matrices.pushPose();
		matrices.scale(widthScale, heightScale, widthScale);
		matrices.pushPose();

		ScaleRenderUtils.saveLastRenderedEntity(pehkuiState.getEntityType());

		try
		{
			original.call(renderer, state, matrices, provider, light);
		}
		finally
		{
			ScaleRenderUtils.clearLastRenderedEntity();

			matrices.popPose();
			matrices.popPose();
		}
	}
}
