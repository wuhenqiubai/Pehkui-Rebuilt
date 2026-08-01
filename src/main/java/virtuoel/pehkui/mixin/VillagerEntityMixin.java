package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin
{
	@ModifyExpressionValue(method = "onStruckByLightning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LightningEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;convertTo(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/conversion/EntityConversionContext;Lnet/minecraft/entity/conversion/EntityConversionContext$Finalizer;)Lnet/minecraft/entity/mob/MobEntity;"))
	private MobEntity pehkui$onStruckByLightning(MobEntity converted, ServerWorld world, LightningEntity lightning)
	{
		if (converted != null)
		{
			ScaleUtils.loadScale(converted, (Entity) (Object) this);
		}
		
		return converted;
	}
}
