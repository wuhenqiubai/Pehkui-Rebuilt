package virtuoel.pehkui.util;

import net.minecraft.advancements.critereon.MinMaxBounds;
import virtuoel.pehkui.api.ScaleType;

public interface PehkuiEntitySelectorReaderExtensions
{
	ScaleType pehkui_getScaleType();
	void pehkui_setScaleType(final ScaleType scaleType);
	
	MinMaxBounds<? extends Number> pehkui_getScaleRange();
	void pehkui_setScaleRange(final MinMaxBounds.Doubles baseScaleRange);
	
	ScaleType pehkui_getComputedScaleType();
	void pehkui_setComputedScaleType(final ScaleType computedScaleType);
	
	MinMaxBounds<? extends Number> pehkui_getComputedScaleRange();
	void pehkui_setComputedScaleRange(final MinMaxBounds.Doubles computedScaleRange);
}
