package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.entity.state.EntityRenderState;
import virtuoel.pehkui.util.PehkuiEntityRenderStateExtensions;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements PehkuiEntityRenderStateExtensions
{
	@Unique
	private float pehkui$modelWidthScale = 1.0F;
	
	@Unique
	private float pehkui$modelHeightScale = 1.0F;
	
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
