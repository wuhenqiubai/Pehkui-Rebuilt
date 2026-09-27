package virtuoel.pehkui.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Coerce;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

/**
 * 手持/展示物品的缩放。
 *
 * <p>注意这里是 <b>@WrapMethod 包住整个方法</b>，不是原先的
 * 「@Inject(HEAD) pushPose / @Inject(RETURN) popPose」配对写法。
 *
 * <p>原写法只在方法正常直线执行时才平衡：若 renderStatic 内抛异常，或另一个 mod 在同一方法上
 * {@code @Inject(HEAD, cancellable = true)} 后 cancel（提前返回），RETURN 注入就不会执行，
 * 于是 pushPose 漏掉配对，把 {@code LevelRenderer.renderLevel} 的局部 PoseStack 撑成非空，
 * 原版收尾时会抛 {@code IllegalStateException: Pose stack not empty}。
 * 这个结构问题在上游 Pehkui 一直存在（1.18 首报、1.21 可复现）。
 * 改成包整方法 + try/finally 后，push/pop 在结构上不可能失配。
 *
 * <p>TODO 其余分支（1.21.11 / 26.1.2 / 26.2 / 26.3）的 ItemRendererMixin 仍是老的 HEAD/RETURN
 * 配对写法，有同样的隐患，需要时按本文件的写法一并处理。
 */
@Mixin(value = ItemRenderer.class, priority = 1010)
public class ItemRendererMixin
{
	@WrapMethod(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V")
	private void pehkui$renderItem(@Nullable LivingEntity entity, ItemStack item, @Coerce Object renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, @Nullable Level world, int light, int overlay, int seed, Operation<Void> original)
	{
		// 守卫只求值一次，两侧不可能不一致
		if (ScaleRenderUtils.shouldSkipHeadItemScaling(entity, item, renderMode))
		{
			original.call(entity, item, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
			return;
		}
		
		ScaleRenderUtils.logIfItemRenderCancelled();
		
		matrices.pushPose();
		
		try
		{
			if (!item.isEmpty() && entity != null)
			{
				final float tickDelta = ScaleRenderUtils.getTickDelta(Minecraft.getInstance());
				final float scale = ScaleUtils.getHeldItemScale(entity, tickDelta);
				
				if (scale != 1.0F)
				{
					matrices.scale(scale, scale, scale);
				}
			}
			
			matrices.pushPose();
			
			ScaleRenderUtils.saveLastRenderedItem(item);
			
			original.call(entity, item, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
		}
		finally
		{
			ScaleRenderUtils.clearLastRenderedItem();
			
			matrices.popPose();
			matrices.popPose();
		}
	}
}
