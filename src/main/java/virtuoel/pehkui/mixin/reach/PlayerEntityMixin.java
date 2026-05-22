package virtuoel.pehkui.mixin.reach;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
{
	@ModifyExpressionValue(method = "doSweepingAttack(Lnet/minecraft/entity/Entity;FLnet/minecraft/entity/damage/DamageSource;F)V", at = @At(value = "CONSTANT", args = "doubleValue=9.0D"))
	private double pehkui$attack$distance(double value)
	{
		final float scale = ScaleUtils.getEntityReachScale((Entity) (Object) this);
		
		return scale > 1.0F ? scale * scale * value : value;
	}
}
