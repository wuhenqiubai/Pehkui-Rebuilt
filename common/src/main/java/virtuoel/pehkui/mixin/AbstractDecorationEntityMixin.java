package virtuoel.pehkui.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(HangingEntity.class)
public abstract class AbstractDecorationEntityMixin
{
	@ModifyVariable(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "STORE"))
	private ItemEntity pehkui$dropStack(ItemEntity entity)
	{
		ScaleUtils.setScaleOfDrop(entity, (Entity) (Object) this);
		return entity;
	}
	
	@ModifyArg(method = "recalculateBoundingBox()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/HangingEntity;setBoundingBox(Lnet/minecraft/world/phys/AABB;)V"))
	private AABB pehkui$updateAttachmentPosition$setBoundingBox(AABB box)
	{
		final HangingEntity entity = (HangingEntity) (Object) this;
		
		final Direction facing = entity.getDirection();
		
		final double xLength = box.getXsize() / -2.0D;
		final double yLength = box.getYsize() / -2.0D;
		final double zLength = box.getZsize() / -2.0D;
		
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale(entity);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale(entity);
		
		if (widthScale != 1.0F || heightScale != 1.0F)
		{
			final double dX = xLength * (1.0D - widthScale);
			final double dY = yLength * (1.0D - heightScale);
			final double dZ = zLength * (1.0D - widthScale);
			box = box.inflate(dX, dY, dZ);
			box = box.move(dX * facing.getStepX(), dY * facing.getStepY(), dZ * facing.getStepZ());
		}
		
		return box;
	}
}
