package virtuoel.pehkui.mixin;

import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import virtuoel.pehkui.util.PehkuiEntityExtensions;

@Mixin(NbtPredicate.class)
public class NbtPredicateMixin
{
	@Inject(method = "getEntityTagToCompare", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/world/entity/Entity;saveWithoutId(Lnet/minecraft/world/level/storage/ValueOutput;)V"))
	private static void pehkui$entityToNbt$before(Entity entity, CallbackInfoReturnable<CompoundTag> info)
	{
		((PehkuiEntityExtensions) entity).pehkui_setShouldIgnoreScaleNbt(true);
	}
	
	@Inject(method = "getEntityTagToCompare", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;saveWithoutId(Lnet/minecraft/world/level/storage/ValueOutput;)V"))
	private static void pehkui$entityToNbt$after(Entity entity, CallbackInfoReturnable<CompoundTag> info)
	{
		((PehkuiEntityExtensions) entity).pehkui_setShouldIgnoreScaleNbt(false);
	}
}
