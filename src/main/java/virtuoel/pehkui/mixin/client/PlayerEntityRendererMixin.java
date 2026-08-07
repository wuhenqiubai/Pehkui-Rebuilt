package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin
{
	@ModifyReturnValue(method = "getRenderOffset", at = @At("RETURN"))
	private Vec3 pehkui$getPositionOffset(Vec3 original, AvatarRenderState state)
	{
		if (original != Vec3.ZERO)
		{
			final float scale = ((PehkuiEntityRenderStateExtensions) state).getModelHeightScale();
			return original.scale(scale);
		}

		return original;
	}
}
