package virtuoel.pehkui.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState>
{
	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void pehkui$updateRenderState(T entity, S state, float tickDelta, CallbackInfo info)
	{
		final PehkuiEntityRenderStateExtensions pehkuiState = (PehkuiEntityRenderStateExtensions) state;
		
		pehkuiState.pehkui$setModelWidthScale(ScaleUtils.getModelWidthScale(entity, tickDelta));
		pehkuiState.pehkui$setModelHeightScale(ScaleUtils.getModelHeightScale(entity, tickDelta));
		pehkuiState.pehkui$setVanillaScale(ScaleUtils.getVanillaScale(entity));
	}
}
