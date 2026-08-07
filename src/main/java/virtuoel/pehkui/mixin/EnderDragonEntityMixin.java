package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EnderDragon.class)
public class EnderDragonEntityMixin
{
	@ModifyArg(method = "onCrystalDestroyed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;hurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	private float pehkui$crystalDestroyed$damagePart(ServerLevel level, EnderDragonPart part, DamageSource source, float amount)
	{
		if (source.getEntity() instanceof Player)
		{
			final float scale = ScaleUtils.getAttackScale(source.getEntity());

			if (scale != 1.0F)
			{
				return amount / scale;
			}
		}

		return amount;
	}
}
