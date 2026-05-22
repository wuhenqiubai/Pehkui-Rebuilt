package virtuoel.pehkui.mixin.compat117plus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(SlimeEntity.class)
public class SlimeEntityMixin
{
	@Inject(method = "method_63653(IFFLnet/minecraft/entity/mob/SlimeEntity;)V", at = @At("HEAD"))
	private void pehkui$remove$copyScale(int size, float offsetX, float offsetZ, SlimeEntity entity, CallbackInfo info)
	{
		ScaleUtils.loadScale(entity, (Entity) (Object) this);
	}
	
	@ModifyExpressionValue(method = "method_63653(IFFLnet/minecraft/entity/mob/SlimeEntity;)V", at = @At(value = "CONSTANT", args = "doubleValue=0.5D"))
	private double pehkui$remove$verticalOffset(double value)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale((Entity) (Object) this);
		
		if (scale != 1.0F)
		{
			return value * scale;
		}
		
		return value;
	}
}
