package virtuoel.pehkui.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

// 26.3 起 ItemInHandRenderer 更名为 FirstPersonHandsAndItemsRenderer，
// 且 submitArmWithItem 的首参由 AbstractClientPlayer 换成 PlayerRenderState、
// 并在第 2 位新增 FirstPersonHandsAndItemsRenderState（渲染全面 state 化）
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class ItemRendererMixin
{
	@Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "HEAD"))
	private void pehkui$renderFirstPersonItem$head(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState handState, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector queue, int light, CallbackInfo info)
	{
		// push 必须无条件执行，与下面的 pop 成对（否则 player 为 null 时矩阵栈会失衡）
		matrices.pushPose();

		// 26.3 起渲染不再传入实体，改用本地玩家：第一人称手持渲染必然属于本地玩家
		final LocalPlayer player = Minecraft.getInstance().player;

		if (player != null)
		{
			final float scale = ScaleUtils.getHeldItemScale(player, ScaleRenderUtils.getTickDelta(Minecraft.getInstance()));

			if (scale != 1.0F)
			{
				matrices.scale(scale, scale, scale);
			}
		}
	}

	@Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "RETURN"))
	private void pehkui$renderFirstPersonItem$return(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState handState, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector queue, int light, CallbackInfo info)
	{
		matrices.popPose();
	}
}
