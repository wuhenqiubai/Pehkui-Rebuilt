package virtuoel.pehkui.mixin.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements PehkuiEntityRenderStateExtensions
{
	@Unique
	private float pehkui$modelWidthScale = 1.0F;
	
	@Unique
	private float pehkui$modelHeightScale = 1.0F;

	@Unique
	private float pehkui$vanillaScale = 1.0F;

	@Override
	public float pehkui$getVanillaScale()
	{
		return pehkui$vanillaScale;
	}

	@Override
	public void pehkui$setVanillaScale(float scale)
	{
		pehkui$vanillaScale = scale;
	}

	@Override
	public float pehkui$getModelWidthScale()
	{
		return pehkui$modelWidthScale;
	}
	
	@Override
	public void pehkui$setModelWidthScale(float scale)
	{
		pehkui$modelWidthScale = scale;
	}
	
	@Override
	public float pehkui$getModelHeightScale()
	{
		return pehkui$modelHeightScale;
	}
	
	@Override
	public void pehkui$setModelHeightScale(float scale)
	{
		pehkui$modelHeightScale = scale;
	}
}
