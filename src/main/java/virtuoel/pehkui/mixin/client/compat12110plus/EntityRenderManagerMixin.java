package virtuoel.pehkui.mixin.client.compat12110plus;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;
import virtuoel.pehkui.util.ScaleRenderUtils;

@Mixin(EntityRenderManager.class)
public class EntityRenderManagerMixin
{
	@WrapOperation(method = "render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/render/state/CameraRenderState;DDDLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"))
	private <S extends EntityRenderState> void pehkui$render(EntityRenderer<?, ? super S> renderer, S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, Operation<Void> original)
	{
		final PehkuiEntityRenderStateExtensions pehkuiState = (PehkuiEntityRenderStateExtensions) state;
		final float widthScale = pehkuiState.pehkui$getModelWidthScale();
		final float heightScale = pehkuiState.pehkui$getModelHeightScale();
		
		ScaleRenderUtils.logIfEntityRenderCancelled();
		
		matrices.push();
		matrices.scale(widthScale, heightScale, widthScale);
		matrices.push();
		
		ScaleRenderUtils.saveLastRenderedEntity(state.entityType);
		
		try
		{
			original.call(renderer, state, matrices, queue, cameraState);
		}
		finally
		{
			ScaleRenderUtils.clearLastRenderedEntity();
			
			matrices.pop();
			matrices.pop();
		}
	}
}
