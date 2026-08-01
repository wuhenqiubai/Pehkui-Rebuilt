package virtuoel.pehkui.mixin;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(VillagerMakeLove.class)
public class VillagerBreedTaskMixin
{
	@Inject(method = "breed(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;Lnet/minecraft/world/entity/npc/Villager;)Ljava/util/Optional;", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
	private void pehkui$createChild(ServerLevel serverWorld, Villager villagerEntity, Villager villagerEntity2, CallbackInfoReturnable<Optional<Villager>> info, @Local(ordinal = 2) Villager child)
	{
		ScaleUtils.loadAverageScales(child, villagerEntity, villagerEntity2);
	}
}
