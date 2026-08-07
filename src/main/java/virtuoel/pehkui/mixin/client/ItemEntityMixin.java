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
	@Inject(at = @At("RETURN"), method = "restoreFrom")
	private void pehkui$copyFrom(Entity entity, CallbackInfo info)
	{
		if (entity instanceof ItemEntity)
		{
			ScaleUtils.loadScale((Entity) (Object) this, entity);
		}
	}
}
