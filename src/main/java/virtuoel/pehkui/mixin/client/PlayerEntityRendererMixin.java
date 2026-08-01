package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin
{
	@ModifyReturnValue(method = "getRenderOffset", at = @At("RETURN"))
	private Vec3 pehkui$getPositionOffset(Vec3 original, AbstractClientPlayer entity, float tickDelta)
	{
		if (original != Vec3.ZERO)
		{
			return original.scale(ScaleUtils.getModelHeightScale(entity, tickDelta));
		}
		
		return original;
	}
}
