package virtuoel.pehkui.mixin.client;

import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.social.EntityPortraitWidget;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.util.ReflectionUtils;

// 26.4：实体预览的提取逻辑从 InventoryScreen 移到了 EntityPortraitWidget
// （InventoryScreen / CreativeModeInventoryScreen / AbstractMountInventoryScreen 都改成调用它）。
// 同时签名变了：旧 (…IIIIIFFF…, LivingEntity) → 新 (…IIIIFFFF…, Entity)，
// 即 size 由 int 变 float、实体参数由 LivingEntity 放宽为 Entity。
@Mixin(EntityPortraitWidget.class)
public abstract class InventoryScreenMixin
{
	@Unique private static final ThreadLocal<Map<ScaleType, ScaleData>> pehkui$SCALES = ThreadLocal.withInitial(Object2ObjectLinkedOpenHashMap::new);
	@Unique private static final ScaleData pehkui$IDENTITY = ScaleData.Builder.create().build();

	// TODO：MoJang叕改坏了
	@Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIFFFFLnet/minecraft/world/entity/Entity;)V", at = @At(value = "HEAD"))
	private static void pehkui$drawEntity$head(GuiGraphicsExtractor graphics, int i, int j, int k, int l, float f, float g, float h, float m, Entity entity, CallbackInfo info, @Share("bounds") LocalRef<AABB> bounds)
	{
		final Map<ScaleType, ScaleData> scales = pehkui$SCALES.get();

		ScaleData data;
		ScaleData cachedData;
		for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			cachedData = scales.computeIfAbsent(type, t -> ScaleData.Builder.create().type(t).build());
			data = type.getScaleData(entity);
			cachedData.fromScale(data, false);
			data.fromScale(pehkui$IDENTITY, false);
		}

		bounds.set(entity.getBoundingBox());

		final EntityDimensions dims = entity.getDimensions(entity.getPose());
		final Vec3 pos = entity.position();
		final double r = ReflectionUtils.getDimensionsWidth(dims) / 2.0D;
		final double height = ReflectionUtils.getDimensionsHeight(dims);
		final double xPos = pos.x;
		final double yPos = pos.y;
		final double zPos = pos.z;
		final AABB box = new AABB(xPos - r, yPos, zPos - r, xPos + r, yPos + height, zPos + r);

		entity.setBoundingBox(box);
	}

	// TODO:同上
	@Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIFFFFLnet/minecraft/world/entity/Entity;)V", at = @At(value = "RETURN"))
	private static void pehkui$drawEntity$return(GuiGraphicsExtractor graphics, int i, int j, int k, int l, float f, float g, float h, float m, Entity entity, CallbackInfo info, @Share("bounds") LocalRef<AABB> bounds)
	{
		final Map<ScaleType, ScaleData> scales = pehkui$SCALES.get();

		for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			type.getScaleData(entity).fromScale(scales.get(type), false);
		}

		entity.setBoundingBox(bounds.get());
	}
}
