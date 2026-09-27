package virtuoel.pehkui.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin
{
	// 包住 EntityRenderer.render 这一次调用，push/pop 用 try/finally 兜底。
	// 原先用 @Inject(BEFORE) push ×2 + @Inject(AFTER) pop ×2 配对：目标调用抛异常时 AFTER 不执行，
	// pushPose 漏配对会把 LevelRenderer.renderLevel 的局部 PoseStack 撑成非空
	// （原版收尾抛 IllegalStateException: Pose stack not empty）。
	// 本写法与 1.21.11 的 EntityRenderManagerMixin 同构。
	@WrapOperation(method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
	private <E extends Entity> void pehkui$render(EntityRenderer<? super E> renderer, E entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, Operation<Void> original)
	{
		ScaleRenderUtils.logIfEntityRenderCancelled();
		
		final float widthScale = ScaleUtils.getModelWidthScale(entity, tickDelta);
		final float heightScale = ScaleUtils.getModelHeightScale(entity, tickDelta);
		final float vanillaScale = ScaleUtils.getVanillaScale(entity);
		
		matrices.pushPose();
		matrices.pushPose();
		
		try
		{
			if (vanillaScale != 1.0F && PehkuiConfig.COMMON.applyVanillaScale.get())
			{
				// vanilla 在 LivingEntityRenderer.render 里又按 getScale()（= 原版 scale）缩放了一次，
				// 而 Pehkui 的 MODEL scale 已折入原版 scale，这里抵消避免双重
				matrices.scale(widthScale / vanillaScale, heightScale / vanillaScale, widthScale / vanillaScale);
			}
			else
			{
				matrices.scale(widthScale, heightScale, widthScale);
			}
			
			ScaleRenderUtils.saveLastRenderedEntity(entity.getType());
			
			original.call(renderer, entity, yaw, tickDelta, matrices, vertexConsumers, light);
		}
		finally
		{
			ScaleRenderUtils.clearLastRenderedEntity();
			
			matrices.popPose();
			matrices.popPose();
		}
	}

	@ModifyArg(method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 6, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V"))
	private float pehkui$render$shadowSize(PoseStack matrices, MultiBufferSource vertexConsumers, Entity entity, float darkness, float tickDelta, LevelReader world, float size)
	{
		return size * ScaleUtils.getModelWidthScale(entity, tickDelta);
	}

	@Inject(method = "renderHitbox", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLineBox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/AABB;FFFF)V", ordinal = 0))
	private static void pehkui$renderHitbox(PoseStack matrices, VertexConsumer vertices, Entity entity, float tickDelta, float red, float green, float blue, CallbackInfo ci)
	{
		final float interactionWidth = ScaleUtils.getInteractionBoxWidthScale(entity);
		final float interactionHeight = ScaleUtils.getInteractionBoxHeightScale(entity);
		final float margin = entity.getPickRadius();

		if (interactionWidth != 1.0F || interactionHeight != 1.0F || margin != 0.0F)
		{
			AABB bounds = entity.getBoundingBox();

			final double scaledXLength = bounds.getXsize() * 0.5D * (interactionWidth - 1.0F);
			final double scaledYLength = bounds.getYsize() * 0.5D * (interactionHeight - 1.0F);
			final double scaledZLength = bounds.getZsize() * 0.5D * (interactionWidth - 1.0F);
			final double scaledMarginWidth = margin * interactionWidth;
			final double scaledMarginHeight = margin * interactionHeight;

			bounds = bounds.inflate(scaledXLength + scaledMarginWidth, scaledYLength + scaledMarginHeight, scaledZLength + scaledMarginWidth)
				.move(-entity.getX(), -entity.getY(), -entity.getZ());

			ScaleRenderUtils.renderInteractionBox(matrices, vertices, bounds);
		}
	}
}
