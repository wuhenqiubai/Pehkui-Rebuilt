package virtuoel.pehkui.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Gui.class)
public abstract class InGameHudMixin
{
	@Shadow
	protected abstract Player getCameraPlayer();
	
	@Shadow @Final @Mutable
	private Minecraft minecraft;
	
	@ModifyArg(method = "renderHealthLevel(Lnet/minecraft/client/gui/GuiGraphics;)V", index = 0, at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
	private float pehkui$renderStatusBars(float value)
	{
		final float healthScale = ScaleUtils.getHealthScale(getCameraPlayer(), ScaleRenderUtils.getTickDelta(minecraft));
		
		return healthScale != 1.0F ? value * healthScale : value;
	}
}
