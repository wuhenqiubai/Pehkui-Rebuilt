package virtuoel.pehkui.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameEntityRendererMixin
{
	@ModifyVariable(method = "render", at = @At(value = "STORE"))
	private Vec3 pehkui$render(Vec3 value, ItemFrame itemFrameEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
	{
		final float widthScale = ScaleUtils.getBoundingBoxWidthScale(itemFrameEntity);
		final float heightScale = ScaleUtils.getBoundingBoxHeightScale(itemFrameEntity);
		
		if (widthScale != 1.0F || heightScale != 1.0F)
		{
			value = value.multiply(1.0F / widthScale, 1.0F / heightScale, 1.0F / widthScale);
			
			final Direction facing = itemFrameEntity.getDirection();
			
			final double widthOffset = ((0.0625D - (0.03125D * widthScale)) - 0.03125D) / widthScale;
			final double heightOffset = ((0.0625D - (0.03125D * heightScale)) - 0.03125D) / heightScale;
			
			value = value.add(widthOffset * facing.getStepX(), heightOffset * facing.getStepY(), widthOffset * facing.getStepZ());
		}
		
		return value;
	}
}
