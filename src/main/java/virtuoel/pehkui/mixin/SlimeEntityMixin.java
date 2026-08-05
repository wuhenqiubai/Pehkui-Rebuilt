package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Slime.class)
public class SlimeEntityMixin
{
	// NeoForge 1.21.1 史莱姆分裂改为 children.forEach(level::addFreshEntity) lambda，注入点迁移到子体 setSize
	@Inject(method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;setSize(IZ)V", shift = Shift.BEFORE))
	private void pehkui$remove$copyScale(Entity.RemovalReason reason, CallbackInfo info, @Local Slime slime)
	{
		ScaleUtils.loadScale(slime, (Entity) (Object) this);
	}
	
	@ModifyExpressionValue(method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At(value = "CONSTANT", args = "doubleValue=0.5D"))
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
