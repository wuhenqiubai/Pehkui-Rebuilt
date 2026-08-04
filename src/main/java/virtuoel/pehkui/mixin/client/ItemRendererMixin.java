package virtuoel.pehkui.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(value = ItemInHandRenderer.class, priority = 1010)
public class ItemRendererMixin
{
	@Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "HEAD"))
	private void pehkui$renderItem$head(@Nullable LivingEntity entity, ItemStack item, ItemDisplayContext renderMode, PoseStack matrices, SubmitNodeCollector queue, int light, CallbackInfo info)
	{
		if (ScaleRenderUtils.shouldSkipHeadItemScaling(entity, item, renderMode))
		{
			return;
		}
		
		ScaleRenderUtils.logIfItemRenderCancelled();
		
		matrices.pushPose();
		
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
	}
	
	@Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "RETURN"))
	private void pehkui$renderItem$return(@Nullable LivingEntity entity, ItemStack item, ItemDisplayContext renderMode, PoseStack matrices, SubmitNodeCollector queue, int light, CallbackInfo info)
	{
		if (ScaleRenderUtils.shouldSkipHeadItemScaling(entity, item, renderMode))
		{
			return;
		}
		
		ScaleRenderUtils.clearLastRenderedItem();
		
		matrices.popPose();
		matrices.popPose();
	}
}
