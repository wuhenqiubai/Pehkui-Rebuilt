package virtuoel.pehkui.mixin;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemMixin
{
	@Inject(at = @At("RETURN"), method = "spawnOffspringFromSpawnEgg(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;")
	private void pehkui$spawnBaby(Player user, Mob mobEntity, EntityType<? extends Mob> entityType, ServerLevel serverWorld, Vec3 vec3d, ItemStack itemStack, CallbackInfoReturnable<Optional<Mob>> info)
	{
		info.getReturnValue().ifPresent(e ->
		{
			ScaleUtils.loadScale(e, mobEntity);
		});
	}
}
