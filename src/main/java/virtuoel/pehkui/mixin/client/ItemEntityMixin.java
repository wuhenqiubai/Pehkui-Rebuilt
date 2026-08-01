package virtuoel.pehkui.mixin.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin
{
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/entity/item/ItemEntity;)V")
	private void pehkui$construct(ItemEntity entity, CallbackInfo info)
	{
		ScaleUtils.loadScale((Entity) (Object) this, entity);
	}
}
