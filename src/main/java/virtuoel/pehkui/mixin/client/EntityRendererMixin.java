package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity>
{
	@Inject(method = "renderLeash", at = @At(value = "HEAD"))
	private <E extends Entity> void pehkui$renderLeash$head(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, E leashHolder, CallbackInfo info)
	{
		final float widthScale = ScaleUtils.getModelWidthScale(entity, tickDelta);
		final float heightScale = ScaleUtils.getModelHeightScale(entity, tickDelta);
		
		final float inverseWidthScale = 1.0F / widthScale;
		final float inverseHeightScale = 1.0F / heightScale;
		
		matrices.push();
		matrices.scale(inverseWidthScale, inverseHeightScale, inverseWidthScale);
		matrices.push();
	}
	
	@Inject(method = "renderLeash", at = @At(value = "RETURN"))
	private <E extends Entity> void pehkui$renderLeash$return(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, E leashHolder, CallbackInfo info)
	{
		matrices.pop();
		matrices.pop();
	}
	
	@ModifyExpressionValue(method = "renderLabelIfPresent", at = @At(value = "CONSTANT", args = "doubleValue=0.5D"))
	private double pehkui$renderLabelIfPresent$offset(double value, @Local(argsOnly = true) T entity)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale(entity);
		
		return scale != 1.0F ? value + (entity.getHeight() * ((1.0F / scale) - 1.0F)) : value;
	}
}
