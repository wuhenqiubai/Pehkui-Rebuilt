package virtuoel.pehkui.mixin;

import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import virtuoel.pehkui.util.PehkuiEntityExtensions;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin
{
	@Inject(method = "method_9957", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/world/entity/Entity;saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"))
	private static void pehkui$nbtSelector$before(CompoundTag nbt, boolean negated, Entity entity, CallbackInfoReturnable<Boolean> info)
	{
		((PehkuiEntityExtensions) entity).pehkui_setShouldIgnoreScaleNbt(true);
	}
	
	@Inject(method = "method_9957", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"))
	private static void pehkui$nbtSelector$after(CompoundTag nbt, boolean negated, Entity entity, CallbackInfoReturnable<Boolean> info)
	{
		((PehkuiEntityExtensions) entity).pehkui_setShouldIgnoreScaleNbt(false);
	}
}
