package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
	@Shadow @Final @Mutable
	Minecraft minecraft;

	@Unique
	boolean pehkui$isBobbing = false;

	@ModifyExpressionValue(method = "getProjectionMatrix(F)Lorg/joml/Matrix4f;", at = @At(value = "CONSTANT", args = "floatValue=0.05F"))
	private float pehkui$getBasicProjectionMatrix$depth(float value)
	{
		return ScaleRenderUtils.modifyProjectionMatrixDepth(value, minecraft.getCameraEntity(), ScaleRenderUtils.getTickDelta(minecraft));
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
	private void pehkui$renderWorld$before(DeltaTracker tickCounter, CallbackInfo info)
	{
		pehkui$isBobbing = true;
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
	private void pehkui$renderWorld$after(DeltaTracker tickCounter, CallbackInfo info)
	{
		pehkui$isBobbing = false;
	}

	@WrapOperation(method = "bobView", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
	private void pehkui$bobView$translate(PoseStack obj, float x, float y, float z, Operation<Void> original)
	{
		if (pehkui$isBobbing)
		{
			final float scale = ScaleUtils.getViewBobbingScale(minecraft.getCameraEntity(), ScaleRenderUtils.getTickDelta(minecraft));

			if (scale != 1.0F)
			{
				x *= scale;
				y *= scale;
				z *= scale;
			}
		}

		original.call(obj, x, y, z);
	}
}
