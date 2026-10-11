package virtuoel.pehkui.mixin.client;

import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
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

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin
{
	@Unique private static final ThreadLocal<Map<ScaleType, ScaleData>> pehkui$SCALES = ThreadLocal.withInitial(Object2ObjectLinkedOpenHashMap::new);
	@Unique private static final ScaleData pehkui$IDENTITY = ScaleData.Builder.create().build();

	/**
	 * 预览渲染期间实体的「包围盒宽/高缩放」快照，{0}=宽 {1}=高。
	 *
	 * <p>必须在下面 HEAD 里重置 ScaleData <b>之前</b>取值 —— 重置之后
	 * {@code ScaleUtils.getBoundingBoxXxxScale} 一律返回 1，就拿不到真实缩放了。
	 */
	@Unique private static final ThreadLocal<float[]> pehkui$PREVIEW_SCALES = ThreadLocal.withInitial(() -> new float[] { 1.0F, 1.0F });

	@Inject(method = "renderEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "HEAD"))
	private static void pehkui$drawEntity$head(GuiGraphics drawContext, int left, int top, int right, int bottom, int size, float mouseX, float mouseY, float yOffset, LivingEntity entity, CallbackInfo info, @Share("bounds") LocalRef<AABB> bounds)
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
		final double h = ReflectionUtils.getDimensionsHeight(dims);
		final double xPos = pos.x;
		final double yPos = pos.y;
		final double zPos = pos.z;
		final AABB box = new AABB(xPos - r, yPos, zPos - r, xPos + r, yPos + h, zPos + r);

		entity.setBoundingBox(box);
	}

	/**
	 * 把预览用的 render state 的包围盒尺寸除回**原版值**。
	 *
	 * <p>1.21.2+ 起，原版 {@code InventoryScreen} 自己就在做「预览取消缩放」：
	 * <pre>
	 * livingEntityRenderState.boundingBoxHeight = ...boundingBoxHeight / ...scale;
	 * livingEntityRenderState.scale = 1.0F;
	 * Vector3f vector3f = new Vector3f(0.0F, entityRenderState.boundingBoxHeight / 2.0F + f, 0.0F);
	 * </pre>
	 * 它指望 {@code scale}（即 {@code entity.getScale()}，来自原版 {@code minecraft:scale} 属性）
	 * 与 {@code getBbHeight()} 里的缩放是同一份。但 Pehkui 用的是自己的 ScaleData，
	 * **默认不同步到那个属性**（{@code applyVanillaScale} 等开关默认全 false），
	 * 于是除法白做，偏移量 = 缩放后高度 / 2，随 scale 线性变大 → 预览里的实体一路向下漂出视野。
	 *
	 * <p>这里在 {@code EntityRenderer} 读出 {@code entity.getBbHeight()} **之后**把它除回原版值，
	 * 使 {@code boundingBoxHeight} 与原版本意一致。剩下的原版属性缩放仍由原版自己那行除法处理。
	 *
	 * <p>注意不能改用 {@code entity.refreshDimensions()} 让 {@code getBbHeight()} 直接变回原版 ——
	 * {@code Player.refreshDimensions()} 会 {@code setPos(...)} 重定位玩家，每帧调用有挪动真实玩家的风险。
	 * 这里只改每次调用新建的临时 render state，不碰实体状态。
	 */
	@ModifyReturnValue(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At(value = "RETURN"))
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

	@Inject(method = "renderEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "RETURN"))
	private static void pehkui$drawEntity$return(GuiGraphics drawContext, int left, int top, int right, int bottom, int size, float mouseX, float mouseY, float yOffset, LivingEntity entity, CallbackInfo info, @Share("bounds") LocalRef<AABB> bounds)
	{
		final Map<ScaleType, ScaleData> scales = pehkui$SCALES.get();

		for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			type.getScaleData(entity).fromScale(scales.get(type), false);
		}

		entity.setBoundingBox(bounds.get());
	}
}
