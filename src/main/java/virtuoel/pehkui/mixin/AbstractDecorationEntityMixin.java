package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(AbstractDecorationEntity.class)
public abstract class AbstractDecorationEntityMixin
{
	@ModifyVariable(method = "dropStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "STORE"))
	private ItemEntity pehkui$dropStack(ItemEntity entity)
	{
		ScaleUtils.setScaleOfDrop(entity, (Entity) (Object) this);
		return entity;
	}
	
	@ModifyArg(method = "updateAttachmentPosition()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/AbstractDecorationEntity;setBoundingBox(Lnet/minecraft/util/math/Box;)V"))
	private Box pehkui$updateAttachmentPosition$setBoundingBox(Box box)
	{
		final AbstractDecorationEntity entity = (AbstractDecorationEntity) (Object) this;
		
		final Direction facing = entity.getHorizontalFacing();
		
		final double xLength = box.getLengthX() / -2.0D;
		final double yLength = box.getLengthY() / -2.0D;
		final double zLength = box.getLengthZ() / -2.0D;
		
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale(entity);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale(entity);
		
		if (widthScale != 1.0F || heightScale != 1.0F)
		{
			final double dX = xLength * (1.0D - widthScale);
			final double dY = yLength * (1.0D - heightScale);
			final double dZ = zLength * (1.0D - widthScale);
			box = box.expand(dX, dY, dZ);
			box = box.offset(dX * facing.getOffsetX(), dY * facing.getOffsetY(), dZ * facing.getOffsetZ());
		}
		
		return box;
	}
}
