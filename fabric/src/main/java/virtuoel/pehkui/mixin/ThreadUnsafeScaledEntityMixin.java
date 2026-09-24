package virtuoel.pehkui.mixin;

import java.util.Map;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.util.PehkuiEntityExtensions;
import virtuoel.pehkui.util.ScaleCachingUtils;

@Mixin(Entity.class)
public abstract class ThreadUnsafeScaledEntityMixin implements PehkuiEntityExtensions
{
	private Map<ScaleType, ScaleData> pehkui_scaleTypes = new Object2ObjectOpenHashMap<>();
	
	@Override
	public ScaleData pehkui_getScaleData(ScaleType type)
	{
		if (ScaleCachingUtils.ENABLE_CACHING)
		{
			ScaleData[] scaleCache = pehkui_getScaleCache();
			
			if (scaleCache == null)
			{
				pehkui_setScaleCache(scaleCache = new ScaleData[ScaleCachingUtils.CACHED.length]);
			}
			
			final ScaleData cached = ScaleCachingUtils.getCachedData(scaleCache, type);
			
			if (cached != null)
			{
				return cached;
			}
		}
		
		final Map<ScaleType, ScaleData> scaleTypes = pehkui_getScales();
		
		ScaleData scaleData = scaleTypes.get(type);
		
		if (scaleData == null)
		{
			if (!scaleTypes.containsKey(type))
			{
				scaleTypes.put(type, null);
				scaleTypes.put(type, scaleData = pehkui_constructScaleData(type));
				
				if (ScaleCachingUtils.ENABLE_CACHING)
				{
					ScaleCachingUtils.setCachedData(pehkui_getScaleCache(), type, scaleData);
				}
			}
			else
			{
				scaleData = scaleTypes.get(type);
			}
		}
		
		return scaleData;
	}
	
	@Override
	public Map<ScaleType, ScaleData> pehkui_getScales()
	{
		Map<ScaleType, ScaleData> scaleTypes = pehkui_scaleTypes;
		
		if (scaleTypes == null)
		{
			pehkui_scaleTypes = scaleTypes = new Object2ObjectOpenHashMap<>();
			
			ScaleRegistries.SCALE_TYPES.values().forEach(this::pehkui_getScaleData);
		}
		
		return scaleTypes;
	}
}
