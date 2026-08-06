package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin
{
	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=3.0F"))
	private float pehkui$tickMovement$flightSpeed(float value)
	{
		final float scale = ScaleUtils.getFlightScale((Entity) (Object) this);

		return scale != 1.0F ? scale * value : value;
	}

	// 1.21.2+ walkDist 推进移到 LocalPlayer.move（原 Entity.move 的 0.6F CONSTANT 注入失效）。
	// 大实体（motion scale>1）时 walkDist 推进过快导致 view bobbing 频率异常——恢复 1.21.1 的除以 scale 行为
	@ModifyExpressionValue(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "CONSTANT", args = "floatValue=0.6F"))
	private float pehkui$move$bobbing(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale != 1.0F ? value / scale : value;
	}

	@ModifyExpressionValue(method = "updateAutoJump", at = { @At(value = "CONSTANT", args = "floatValue=1.2F"), @At(value = "CONSTANT", args = "floatValue=0.75F") })
	private float pehkui$autoJump$heightAndBoost(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);
		final float jumpScale = ScaleUtils.getJumpHeightScale((Entity) (Object) this);

		return scale != 1.0F || jumpScale != 1.0F ? scale * jumpScale * value : value;
	}

	@ModifyExpressionValue(method = "sendPosition", at = @At(value = "CONSTANT", args = "doubleValue=2.0E-4D"))
	private double pehkui$sendMovementPackets$minVelocity(double value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale < 1.0F ? scale * value : value;
	}
}
