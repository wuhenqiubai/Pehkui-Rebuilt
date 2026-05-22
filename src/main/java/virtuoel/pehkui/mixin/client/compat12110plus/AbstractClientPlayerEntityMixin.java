package virtuoel.pehkui.mixin.client.compat12110plus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerLikeState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import virtuoel.pehkui.util.PehkuiClientPlayerLikeStateScale;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin
{
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerLikeState;tick(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)V"))
	private void pehkui$tick$state(ClientPlayerLikeState obj, Vec3d pos, Vec3d velocity, Operation<Void> original)
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
