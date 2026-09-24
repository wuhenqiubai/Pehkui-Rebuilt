package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Pig.class)
public class PigEntityMixin
{
	@ModifyExpressionValue(method = "thunderHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LightningBolt;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/pig/Pig;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"))
	private Mob pehkui$onStruckByLightning(Mob converted, ServerLevel world, LightningBolt lightning)
	{
		if (converted instanceof ZombifiedPiglin)
		{
			ScaleUtils.loadScale(converted, (Entity) (Object) this);
		}
		
		return converted;
	}
}
