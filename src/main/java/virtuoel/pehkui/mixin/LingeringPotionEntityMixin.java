package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.LingeringPotionEntity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(LingeringPotionEntity.class)
public class LingeringPotionEntityMixin
{
	@ModifyArg(method = "spawnAreaEffectCloud(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/hit/HitResult;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/AreaEffectCloudEntity;setRadius(F)V"))
	private float pehkui$applyLingeringPotion$setRadius(float value)
	{
		return value * ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
	}
	
	@ModifyArg(method = "spawnAreaEffectCloud(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/hit/HitResult;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/AreaEffectCloudEntity;setRadiusOnUse(F)V"))
	private float pehkui$applyLingeringPotion$setRadiusOnUse(float value)
	{
		return value * ScaleUtils.getBoundingBoxWidthScale((Entity) (Object) this);
	}
	
	@ModifyArg(method = "spawnAreaEffectCloud(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/hit/HitResult;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
	private Entity pehkui$applyLingeringPotion$entity(Entity entity)
	{
		ScaleUtils.loadScale(entity, (Entity) (Object) this);
		
		return entity;
	}
}
