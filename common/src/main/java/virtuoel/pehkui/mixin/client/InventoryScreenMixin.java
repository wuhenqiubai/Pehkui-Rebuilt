package virtuoel.pehkui.mixin.client;

import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.social.EntityPortraitWidget;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.util.ReflectionUtils;
import virtuoel.pehkui.util.ScaleUtils;

// 26.4：实体预览的提取逻辑从 InventoryScreen 移到了 EntityPortraitWidget
// （InventoryScreen / CreativeModeInventoryScreen / AbstractMountInventoryScreen 都改成调用它）。
// 签名一路在变：
//   1.21.x   (…IIIIIFFF…, LivingEntity)
//   snapshot-4 (…IIIIFFFF…, Entity)   —— size 由 int 变 float、实体放宽为 Entity
//   snapshot-5 (…IIIIFF…,   Entity)   —— 又去掉了两个 float
@Mixin(EntityPortraitWidget.class)
public abstract class InventoryScreenMixin
{
	@Unique private static final ThreadLocal<Map<ScaleType, ScaleData>> pehkui$SCALES = ThreadLocal.withInitial(Object2ObjectLinkedOpenHashMap::new);
	@Unique private static final ScaleData pehkui$IDENTITY = ScaleData.Builder.create().build();

	// 预览渲染期间的「包围盒宽/高缩放」快照，{0}=宽 {1}=高。
	// 必须在下面 HEAD 重置 ScaleData **之前**取值 —— 重置之后取到的恒为 1。
	@Unique private static final ThreadLocal<float[]> pehkui$PREVIEW_SCALES = ThreadLocal.withInitial(() -> new float[] { 1.0F, 1.0F });

	@Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIFFLnet/minecraft/world/entity/Entity;)V", at = @At(value = "HEAD"))
	private static void pehkui$drawEntity$head(GuiGraphicsExtractor graphics, int i, int j, int k, int l, float f, float g, Entity entity, CallbackInfo info, @Share("bounds") LocalRef<AABB> bounds)
	{
		final Map<ScaleType, ScaleData> scales = pehkui$SCALES.get();

		final float[] previewScales = pehkui$PREVIEW_SCALES.get();
		previewScales[0] = ScaleUtils.getBoundingBoxWidthScale(entity);
		previewScales[1] = ScaleUtils.getBoundingBoxHeightScale(entity);

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

	// 原版这段代码指望 renderState.scale（来自 minecraft:scale 属性）与 getBbHeight() 里的缩放一致，
	// 才能把高度除回原版值。Pehkui 用自己的 ScaleData、默认不同步那个属性，除法于是白做，
	// 偏移量 = 缩放后高度 / 2 随 scale 线性变大 —— 预览里的实体一路向下漂出视野。
	@ModifyReturnValue(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At(value = "RETURN"))
	private static EntityRenderState pehkui$unscalePreviewBounds(EntityRenderState state)
	{
		final float[] previewScales = pehkui$PREVIEW_SCALES.get();

		if (previewScales[0] != 1.0F)
		{
			state.boundingBoxWidth /= previewScales[0];
		}

		if (previewScales[1] != 1.0F)
		{
			state.boundingBoxHeight /= previewScales[1];
		}

		return state;
	}

	@Inject(method = "extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIFFLnet/minecraft/world/entity/Entity;)V", at = @At(value = "RETURN"))
	private static void pehkui$drawEntity$return(GuiGraphicsExtractor graphics, int i, int j, int k, int l, float f, float g, Entity entity, CallbackInfo info, @Share("bounds") LocalRef<AABB> bounds)
	{
		final Map<ScaleType, ScaleData> scales = pehkui$SCALES.get();

		for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			type.getScaleData(entity).fromScale(scales.get(type), false);
		}

		entity.setBoundingBox(bounds.get());
	}
}
