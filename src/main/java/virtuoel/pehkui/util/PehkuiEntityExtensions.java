package virtuoel.pehkui.util;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;

public interface PehkuiEntityExtensions
{
	ScaleData pehkui_constructScaleData(ScaleType type);
	
	ScaleData pehkui_getScaleData(ScaleType type);
	
	ScaleData[] pehkui_getScaleCache();
	
	void pehkui_setScaleCache(ScaleData[] scaleCache);
	
	Map<ScaleType, ScaleData> pehkui_getScales();
	
	boolean pehkui_shouldSyncScales();
	
	void pehkui_setShouldSyncScales(boolean sync);
	
	boolean pehkui_shouldIgnoreScaleNbt();
	
	void pehkui_setShouldIgnoreScaleNbt(boolean ignore);
	
	void pehkui_readScaleNbt(CompoundTag nbt);
	
	CompoundTag pehkui_writeScaleNbt(CompoundTag nbt);
	
	boolean pehkui_isFirstUpdate();

	boolean pehkui_getOnGround();

	void pehkui_setOnGround(boolean onGround);

	void pehkui_setPosDirectly(BlockPos pos);

	@Nullable
	ScaleType pehkui_getRuleScaleType();

	void pehkui_setRuleScaleType(@Nullable ScaleType type);
}
