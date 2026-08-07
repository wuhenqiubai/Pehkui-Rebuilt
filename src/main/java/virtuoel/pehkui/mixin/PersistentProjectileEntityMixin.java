package virtuoel.pehkui.mixin;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractArrow.class)
public abstract class PersistentProjectileEntityMixin
{
	@Inject(at = @At("HEAD"), method = "setOwner")
	private void pehkui$setOwner(@Nullable Entity entity, CallbackInfo info)
	{
		if (entity != null)
		{
			ScaleUtils.setScaleOfProjectile((Entity) (Object) this, entity);
		}
	}

	@ModifyArg(method = "findHitEntity", index = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/EntityHitResult;"))
	private AABB pehkui$getEntityCollision$expand(Level world, Projectile entity, Vec3 vec3d, Vec3 vec3d2, AABB box, Predicate<Entity> predicate)
	{
		final float width = ScaleUtils.getBoundingBoxWidthScale(entity);
		final float height = ScaleUtils.getBoundingBoxHeightScale(entity);

		if (width != 1.0F || height != 1.0F)
		{
			return box.inflate(width - 1.0D, height - 1.0D, width - 1.0D);
		}

		return box;
	}

	@ModifyVariable(method = "onHitEntity", at = @At(value = "STORE"))
	private float pehkui$onEntityHit(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale != 1.0F ? ScaleUtils.divideClamped(value, scale) : value;
	}

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V")
	private void pehkui$construct(EntityType<? extends Projectile> type, LivingEntity owner, Level world, ItemStack stack, ItemStack shotFrom, CallbackInfo info)
	{
		final float scale = ScaleUtils.getEyeHeightScale(owner);

		if (scale != 1.0F)
		{
			final Entity self = ((Entity) (Object) this);

			final Vec3 pos = self.position();

			self.setPos(pos.x, pos.y + ((1.0F - scale) * 0.1D), pos.z);
		}
	}
}
