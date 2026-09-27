package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity>
{
	// 包住整个 renderLeash，而不是原来的 HEAD push / RETURN pop 配对 —— 后者在方法抛异常时
	// 会漏掉 pop，把 LevelRenderer.renderLevel 的局部 PoseStack 撑成非空（Pose stack not empty）。
	@WrapMethod(method = "renderLeash(Lnet/minecraft/world/entity/Entity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;)V")
	private <E extends Entity> void pehkui$renderLeash(T entity, float tickDelta, PoseStack matrices, MultiBufferSource provider, E leashHolder, Operation<Void> original)
	{
		final float widthScale = ScaleUtils.getModelWidthScale(entity, tickDelta);
		final float heightScale = ScaleUtils.getModelHeightScale(entity, tickDelta);
		
		final float inverseWidthScale = 1.0F / widthScale;
		final float inverseHeightScale = 1.0F / heightScale;
		
		matrices.pushPose();
		
		try
		{
			matrices.scale(inverseWidthScale, inverseHeightScale, inverseWidthScale);
			matrices.pushPose();
			
			original.call(entity, tickDelta, matrices, provider, leashHolder);
		}
		finally
		{
			matrices.popPose();
			matrices.popPose();
		}
	}
	
	@ModifyExpressionValue(method = "renderNameTag", at = @At(value = "CONSTANT", args = "doubleValue=0.5D"))
	private double pehkui$renderLabelIfPresent$offset(double value, @Local(argsOnly = true) T entity)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale(entity);
		
		return scale != 1.0F ? value + (entity.getBbHeight() * ((1.0F / scale) - 1.0F)) : value;
	}
}
