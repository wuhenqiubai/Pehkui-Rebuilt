package virtuoel.pehkui.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EndCrystal.class)
public abstract class EndCrystalEntityMixin
{
	@ModifyExpressionValue(method = "hurtServer", at = @At(value = "CONSTANT", args = "floatValue=6.0F"))
	private float pehkui$damage$createExplosion(float power)
	{
		final float scale = ScaleUtils.getExplosionScale((Entity) (Object) this);

		return scale != 1.0F ? power * scale : power;
	}
}
