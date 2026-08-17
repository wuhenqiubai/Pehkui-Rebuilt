package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.HitResult;
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

	// 1.21.5+ 攻击距离由物品组件 AttackRange 决定，且 raycastHitResult 直接读组件（不走 entityAttackRange）。
	// 缩放 getClosesetHit 收到的 AttackRange，使客户端瞄准射线距离与缩放后的服务端攻击判定一致
	@WrapOperation(method = "raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/AttackRange;getClosesetHit(Lnet/minecraft/world/entity/Entity;FLjava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;"))
	private HitResult pehkui$raycastHitResult$getClosesetHit(AttackRange range, Entity entity, float f, Predicate<Entity> predicate, Operation<HitResult> original)
	{
		final float scale = ScaleUtils.getEntityReachScale(entity);

		return original.call(scale != 1.0F ? ScaleUtils.scaleAttackRange(range, scale) : range, entity, f, predicate);
	}
}
