package virtuoel.pehkui.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(value = ItemEntity.class, priority = 1010)
public class ItemEntityMixin
{
	@ModifyArg(method = "mergeWithNeighbours", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
	private AABB pehkui$tryMerge$box(AABB box)
	{
		final ItemEntity self = (ItemEntity) (Object) this;
		final AABB bounds = self.getBoundingBox();
		
		final double xExpand = box.getXsize() - bounds.getXsize();
		final double yExpand = box.getYsize() - bounds.getYsize();
		final double zExpand = box.getZsize() - bounds.getZsize();
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale(self);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale(self);
		
		return bounds.inflate(
			widthScale != 1.0F ? widthScale * xExpand : xExpand,
			heightScale != 1.0F ? heightScale * yExpand : yExpand,
			widthScale != 1.0F ? widthScale * zExpand : zExpand);
	}
}
