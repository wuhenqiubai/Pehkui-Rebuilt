package virtuoel.pehkui.mixin.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(EntityRenderState.class)
// NeoForge 下隐式 implements 接口不生效，需用 @Implements 显式声明接口注入
// 接口方法名（getModelWidthScale 等）不带前缀，prefix="pehkui$" 拼接后匹配本类 private 方法 pehkui$getModelWidthScale 等
@Implements({
	@Interface(iface = PehkuiEntityRenderStateExtensions.class, prefix = "pehkui$")
})
public class EntityRenderStateMixin
{
	@Unique
	private float pehkui$modelWidthScale = 1.0F;

	@Unique
	private float pehkui$modelHeightScale = 1.0F;

	@Unique
	private float pehkui$vanillaScale = 1.0F;

	public float pehkui$getModelWidthScale()
	{
		return pehkui$modelWidthScale;
	}

	public void pehkui$setModelWidthScale(float scale)
	{
		pehkui$modelWidthScale = scale;
	}

	public float pehkui$getModelHeightScale()
	{
		return pehkui$modelHeightScale;
	}

	public void pehkui$setModelHeightScale(float scale)
	{
		pehkui$modelHeightScale = scale;
	}

	public float pehkui$getVanillaScale()
	{
		return pehkui$vanillaScale;
	}

	public void pehkui$setVanillaScale(float scale)
	{
		pehkui$vanillaScale = scale;
	}
}
