package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(HeldItemRenderer.class)
public class ItemRendererMixin
{
	@Inject(method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V", at = @At(value = "HEAD"))
	private void pehkui$renderFirstPersonItem$head(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, CallbackInfo info)
	{
		final float tickDelta = ScaleRenderUtils.getTickDelta(net.minecraft.client.MinecraftClient.getInstance());
		final float scale = ScaleUtils.getHeldItemScale(player, tickDelta);

		matrices.push();

		if (scale != 1.0F)
		{
			matrices.scale(scale, scale, scale);
		}
	}

	@Inject(method = "renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V", at = @At(value = "RETURN"))
	private void pehkui$renderFirstPersonItem$return(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, CallbackInfo info)
	{
		matrices.pop();
	}
}
