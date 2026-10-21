package virtuoel.pehkui.neoforge.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import virtuoel.pehkui.util.ScaleUtils;

// DragonMine Z 兼容（NeoForge 专有路径）。
//
// NeoForge 打补丁后的 Entity.refreshDimensions() 是：
//     EntityDimensions current = this.getDimensions(pose);      // Pehkui 在这里缩放
//     EntityEvent.Size event = EventHooks.getEntitySizeForge(this, pose, current);
//     this.dimensions = event.getNewSize();                    // ← 第三方可在此覆盖
// DMZ 的 EntityEvent.Size 处理器把尺寸换成 EntityDimensions.fixed(...)，于是 Pehkui 在
// getDimensions 上的缩放被整体丢弃；而且 fixed 尺寸按 vanilla EntityDimensions.scale() 的
// 约定不参与后续缩放，所以不在这里补一刀就永远补不回来。
//
// 只对 fixed 的返回值重新施加缩放：Pehkui 自己 getDimensions 的产物源自
// EntityType 的 scalable 尺寸，不会是 fixed —— 因此 fixed 恰好是「被第三方替换过」的
// 可靠判据，未替换时不会重复缩放。开关与 mod 是否安装由 getScaledDimensions 内部判断。
@Mixin(Entity.class)
public abstract class EntityRefreshDimensionsMixin
{
	@ModifyExpressionValue(
		method = "refreshDimensions",
		at = @At(
			value = "INVOKE",
			target = "Lnet/neoforged/neoforge/event/entity/EntityEvent$Size;getNewSize()Lnet/minecraft/world/entity/EntityDimensions;"
		)
	)
	private EntityDimensions pehkui$rescaleEventSize(EntityDimensions original)
	{
		if (!original.fixed())
		{
			return original;
		}

		return ScaleUtils.getScaledDimensions(original, (Entity) (Object) this);
	}
}
