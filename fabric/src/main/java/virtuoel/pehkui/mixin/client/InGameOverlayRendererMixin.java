package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import virtuoel.pehkui.util.ScaleUtils;

// 26.3 起 view blocking 检测由 ScreenEffectRenderer.getViewBlockingState 迁至
// LevelExtractor.getViewBlockingState（方法体与其中的 0.1F 常量原样保留）
@Mixin(LevelExtractor.class)
public abstract class InGameOverlayRendererMixin
{
	@ModifyExpressionValue(method = "getViewBlockingState", at = @At(value = "CONSTANT", args = "floatValue=0.1F"))
	private static float pehkui$getInWallBlockState$offset(float value, LocalPlayer player, Frustum frustum)
	{
		final float scale = ScaleUtils.getEyeHeightScale(player);

		return scale != 1.0F ? value * scale : value;
	}
}
