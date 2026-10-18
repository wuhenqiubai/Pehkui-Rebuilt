package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import virtuoel.pehkui.util.ScaleUtils;

// 26.3 起 view blocking 检测由 ScreenEffectRenderer.getViewBlockingState 迁至
// LevelExtractor.getViewBlockingState。
// 26.4 又拆了一层：getViewBlockingState 现在只用 BlockMatcher 找方块，
// 「在方块里」遮罩的颜色计算（含 0.1F 常量）下移到 LevelExtractor.extractPlayerState。
// 该方法只针对本地玩家的 LevelRenderState.playerRenderState 调用一次，故直接取 player 即可。
@Mixin(LevelExtractor.class)
public abstract class InGameOverlayRendererMixin
{
	@ModifyExpressionValue(method = "extractPlayerState", at = @At(value = "CONSTANT", args = "floatValue=0.1F"))
	private float pehkui$getInWallBlockState$offset(float value)
	{
		final LocalPlayer player = Minecraft.getInstance().player;

		if (player == null)
		{
			return value;
		}

		final float scale = ScaleUtils.getEyeHeightScale(player);

		return scale != 1.0F ? value * scale : value;
	}
}
