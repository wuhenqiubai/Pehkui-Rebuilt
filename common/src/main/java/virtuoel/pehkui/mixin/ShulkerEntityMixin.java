package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(Shulker.class)
public class ShulkerEntityMixin
{
	@ModifyReturnValue(method = "makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;", at = @At("RETURN"))
	private AABB pehkui$calculateBoundingBox(AABB box)
	{
		final Shulker entity = (Shulker) (Object) this;
		
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale(entity);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale(entity);
		
		if (widthScale != 1.0F || heightScale != 1.0F)
		{
			final Direction facing = entity.getAttachFace().getOpposite();
			
			final double xLength = box.getXsize() / -2.0D;
			final double yLength = box.getYsize() / -2.0D;
			final double zLength = box.getZsize() / -2.0D;
			
			final double dX = xLength * (1.0D - widthScale);
			final double dY = yLength * (1.0D - heightScale);
			final double dZ = zLength * (1.0D - widthScale);
			box = box.inflate(dX, dY, dZ);
			box = box.move(dX * facing.getStepX(), dY * facing.getStepY(), dZ * facing.getStepZ());
		}
		
		return box;
	}
}
