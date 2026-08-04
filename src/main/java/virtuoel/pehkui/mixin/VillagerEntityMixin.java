package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Villager.class)
public class VillagerEntityMixin
{
	@Inject(method = "thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V", at = @At(value = "RETURN"))
	private void pehkui$onStruckByLightning(ServerLevel world, LightningBolt lightning, CallbackInfo info, @Local Witch witchEntity)
	{
		ScaleUtils.loadScale(witchEntity, (Entity) (Object) this);
	}
}
