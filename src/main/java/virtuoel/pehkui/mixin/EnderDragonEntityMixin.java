package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin
{
	@ModifyArg(method = "crystalDestroyed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/decoration/EndCrystalEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/damage/DamageSource;)V", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;damagePart(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/boss/dragon/EnderDragonPart;Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private float pehkui$crystalDestroyed$damagePart(float amount, @Local(ordinal = 0) PlayerEntity attacker)
	{
		if (attacker != null)
		{
			final float scale = ScaleUtils.getAttackScale(attacker);
			
			if (scale != 1.0F)
			{
				return amount / scale;
			}
		}
		
		return amount;
	}
}
