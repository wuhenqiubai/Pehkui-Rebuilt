package virtuoel.pehkui.mixin;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(BrushItem.class)
public class BrushItemMixin
{
	@WrapOperation(method = "calculateHitResult(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnViewVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/HitResult;"))
	private HitResult pehkui$getHitResult$getCollision(Entity entity, Predicate<Entity> predicate, double range, Operation<HitResult> original)
	{
		final float scale = ScaleUtils.getBlockReachScale(entity);
		
		return original.call(entity, predicate, scale != 1.0F ? range * scale : range);
	}
}
