package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.util.PehkuiClientPlayerLikeStateScale;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin
{
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientAvatarState;tick(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)V"))
	private void pehkui$tick$state(ClientAvatarState obj, Vec3 pos, Vec3 velocity, Operation<Void> original)
	{
		PehkuiClientPlayerLikeStateScale.setMotionScale(ScaleUtils.getMotionScale((Entity) (Object) this));
		
		try
		{
			original.call(obj, pos, velocity);
		}
		finally
		{
			PehkuiClientPlayerLikeStateScale.clearMotionScale();
		}
	}
}
