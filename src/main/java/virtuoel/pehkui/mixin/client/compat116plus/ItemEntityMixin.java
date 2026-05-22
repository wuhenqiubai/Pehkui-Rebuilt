package virtuoel.pehkui.mixin.client.compat116plus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin
{
	@Inject(at = @At("RETURN"), method = "copyFrom")
	private void pehkui$copyFrom(Entity entity, CallbackInfo info)
	{
		if (entity instanceof ItemEntity)
		{
			ScaleUtils.loadScale((Entity) (Object) this, entity);
		}
	}
}
