package virtuoel.pehkui.mixin.compat1206minus.compat1203plus;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.util.MixinConstants;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin
{
	@Dynamic
	@Inject(at = @At("RETURN"), method = MixinConstants.PERSISTENT_PROJECTILE_ENTITY_WITH_STACK_INIT)
	private void pehkui$construct(EntityType<? extends ProjectileEntity> type, LivingEntity owner, World world, ItemStack stack, CallbackInfo info)
	{
		final float scale = ScaleUtils.getEyeHeightScale(owner);
		
		if (scale != 1.0F)
		{
			final Entity self = ((Entity) (Object) this);
			
			final Vec3d pos = self.getPos();
			
			self.setPosition(pos.x, pos.y + ((1.0F - scale) * 0.1D), pos.z);
		}
	}
}
