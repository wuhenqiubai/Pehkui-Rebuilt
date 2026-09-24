package virtuoel.pehkui.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public abstract class GhastEntityShootFireballGoalMixin
{
	@Shadow @Final Ghast ghast;
	
	@ModifyArg(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private Entity pehkui$tick$spawnEntity(Entity entity)
	{
		final float scale = ScaleUtils.getBoundingBoxHeightScale(ghast);
		
		if (scale != 1.0F)
		{
			final Vec3 pos = entity.position();
			
			entity.setPos(pos.x, pos.y - ((1.0D - scale) * 0.5D), pos.z);
		}
		
		return entity;
	}
}
