package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(TargetGoal.class)
public class TrackTargetGoalMixin
{
	@Shadow LivingEntity targetMob;
	@Shadow @Final @Mutable Mob mob;
	
	@ModifyReturnValue(method = "getFollowDistance", at = @At("RETURN"))
	private double pehkui$getFollowRange(double original)
	{
		LivingEntity target = this.mob.getTarget();
		if (target == null && (target = this.targetMob) == null)
		{
			return original;
		}
		
		final float scale = ScaleUtils.getVisibilityScale(target);
		
		return scale != 1.0F ? original * scale : original;
	}
}
