package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Ravager.class)
public abstract class RavagerEntityMixin
{
	@WrapOperation(method = "getAttackBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;deflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB pehkui$getAttackBox$contract(AABB obj, double x, double y, double z, Operation<AABB> original)
	{
		final float scale = ScaleUtils.getEntityReachScale((Entity) (Object) this);
		
		if (scale != 1.0F)
		{
			x *= scale;
			z *= scale;
		}
		
		return original.call(obj, x, y, z);
	}
}
