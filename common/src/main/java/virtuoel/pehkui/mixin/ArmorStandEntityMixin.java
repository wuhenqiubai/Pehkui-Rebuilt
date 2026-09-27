package virtuoel.pehkui.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ArmorStand.class)
public abstract class ArmorStandEntityMixin
{
	@ModifyVariable(method = "getClickedSlot", at = @At(value = "STORE"))
	private double pehkui$getSlotFromPosition(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);
		return scale != 1.0F ? value / scale : value;
	}
}
