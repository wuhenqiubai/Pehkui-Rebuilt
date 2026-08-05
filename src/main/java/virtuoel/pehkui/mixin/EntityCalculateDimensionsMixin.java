package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import virtuoel.pehkui.util.ReflectionUtils;

@Mixin(Entity.class)
public abstract class EntityCalculateDimensionsMixin
{
	@Inject(method = "refreshDimensions", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;reapplyPosition()V"))
	private void pehkui$calculateDimensions(CallbackInfo info, @Local(ordinal = 0) EntityDimensions previous, @Local(ordinal = 1) EntityDimensions current)
	{
		final Entity self = (Entity) (Object) this;
		final Level world = self.level();
		
		final float currentWidth = ReflectionUtils.getDimensionsWidth(current);
		final float previousWidth = ReflectionUtils.getDimensionsWidth(previous);
		if (world.isClientSide() && self.getType() == EntityTypes.PLAYER && currentWidth > previousWidth)
		{
			final double prevW = Math.min(previousWidth, 4.0D);
			final double prevH = Math.min(ReflectionUtils.getDimensionsHeight(previous), 4.0D);
			final double currW = Math.min(currentWidth, 4.0D);
			final double currH = Math.min(ReflectionUtils.getDimensionsHeight(current), 4.0D);
			final Vec3 lastCenter = self.position().add(0.0D, prevH / 2.0D, 0.0D);
			final double w = Math.max(0.0F, currW - prevW) + 1.0E-6D;
			final double h = Math.max(0.0F, currH - prevH) + 1.0E-6D;
			final VoxelShape voxelShape = Shapes.create(AABB.ofSize(lastCenter, w, h, w));
			world.findFreePosition(self, voxelShape, lastCenter, currW, currH, currW)
				.ifPresent(vec -> self.setPos(vec.add(0.0D, -currH / 2.0D, 0.0D)));
		}
	}
}
