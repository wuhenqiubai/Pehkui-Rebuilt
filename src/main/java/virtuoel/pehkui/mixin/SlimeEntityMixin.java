package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractCubeMob.class)
public class SlimeEntityMixin
{
	@Inject(method = "lambda$remove$0(IFFLnet/minecraft/world/entity/monster/cubemob/AbstractCubeMob;)V", at = @At("HEAD"))
	private void pehkui$remove$copyScale(int size, float offsetX, float offsetZ, AbstractCubeMob entity, CallbackInfo info)
	{
		ScaleUtils.loadScale(entity, (Entity) (Object) this);
	}
	
	@ModifyExpressionValue(method = "setUpSplitCube(Lnet/minecraft/world/entity/monster/cubemob/AbstractCubeMob;IFF)V", at = @At(value = "CONSTANT", args = "doubleValue=0.5D"))
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
