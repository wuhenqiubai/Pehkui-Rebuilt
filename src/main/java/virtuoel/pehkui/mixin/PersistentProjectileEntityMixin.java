package virtuoel.pehkui.mixin;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(PersistentProjectileEntity.class)
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

	@ModifyArg(method = "getEntityCollision", index = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;getEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Lnet/minecraft/util/hit/EntityHitResult;"))
	private Box pehkui$getEntityCollision$expand(World world, ProjectileEntity entity, Vec3d vec3d, Vec3d vec3d2, Box box, Predicate<Entity> predicate)
	{
		final float width = ScaleUtils.getBoundingBoxWidthScale(entity);
		final float height = ScaleUtils.getBoundingBoxHeightScale(entity);

		if (width != 1.0F || height != 1.0F)
		{
			return box.expand(width - 1.0D, height - 1.0D, width - 1.0D);
		}

		return box;
	}

	@ModifyVariable(method = "onEntityHit", at = @At(value = "STORE"))
	private float pehkui$onEntityHit(float value)
	{
		final float scale = ScaleUtils.getMotionScale((Entity) (Object) this);

		return scale != 1.0F ? ScaleUtils.divideClamped(value, scale) : value;
	}

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V")
	private void pehkui$construct(EntityType<? extends ProjectileEntity> type, LivingEntity owner, World world, ItemStack stack, ItemStack shotFrom, CallbackInfo info)
	{
		final float scale = ScaleUtils.getEyeHeightScale(owner);

		if (scale != 1.0F)
		{
			final Entity self = ((Entity) (Object) this);

			final Vec3d pos = self.getEntityPos();

			self.setPosition(pos.x, pos.y + ((1.0F - scale) * 0.1D), pos.z);
		}
	}
}
