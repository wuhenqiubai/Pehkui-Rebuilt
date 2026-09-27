package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.level.Level;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(LlamaSpit.class)
public class LlamaSpitEntityMixin
{
	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/animal/horse/Llama;)V")
	private void pehkui$construct(Level world, Llama owner, CallbackInfo info)
	{
		ScaleUtils.setScaleOfProjectile((Entity) (Object) this, owner);
	}
	
	@ModifyExpressionValue(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/animal/horse/Llama;)V", at = @At(value = "CONSTANT", args = "doubleValue=0.10000000149011612D"))
	private double pehkui$construct$eyeOffset(double value, Level world, Llama owner)
	{
		final float scale = ScaleUtils.getEyeHeightScale(owner);
		
		return scale != 1.0F ? value * scale : value;
	}
	
	@ModifyExpressionValue(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/animal/horse/Llama;)V", at = @At(value = "CONSTANT", args = "floatValue=1.0F"))
	private float pehkui$construct$widthOffset(float value, Level world, Llama owner)
	{
		final float scale = ScaleUtils.getBoundingBoxWidthScale(owner);
		
		return scale != 1.0F ? value * scale : value;
	}
}
