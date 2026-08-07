package virtuoel.pehkui.mixin;

import java.util.function.Predicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.PehkuiEntitySelectorReaderExtensions;

@Mixin(EntitySelectorParser.class)
public abstract class EntitySelectorReaderMixin implements PehkuiEntitySelectorReaderExtensions
{
	@Shadow
	public abstract void addPredicate(Predicate<Entity> predicate);
	
	@Unique
	MinMaxBounds.Doubles pehkui$scaleRange = (MinMaxBounds.Doubles) (Object) MinMaxBounds.Doubles.ANY;
	@Unique
	MinMaxBounds.Doubles pehkui$computedScaleRange = (MinMaxBounds.Doubles) (Object) MinMaxBounds.Doubles.ANY;
	@Unique
	ScaleType pehkui$scaleType = ScaleTypes.INVALID;
	@Unique
	ScaleType pehkui$computedScaleType = ScaleTypes.INVALID;
	
	@Inject(method = "finalizePredicates", at = @At("HEAD"))
	private void pehkui$buildPredicate(CallbackInfo info)
	{
		if (!this.pehkui$scaleRange.isAny())
		{
			final ScaleType scaleType = this.pehkui$scaleType == ScaleTypes.INVALID ? ScaleTypes.BASE : this.pehkui$scaleType;
			addPredicate(e -> CommandUtils.testFloatRange(this.pehkui$scaleRange, scaleType.getScaleData(e).getBaseScale()));
		}
		
		if (!this.pehkui$computedScaleRange.isAny())
		{
			final ScaleType scaleType = this.pehkui$computedScaleType == ScaleTypes.INVALID ? ScaleTypes.BASE : this.pehkui$computedScaleType;
			addPredicate(e -> CommandUtils.testFloatRange(this.pehkui$computedScaleRange, scaleType.getScaleData(e).getScale()));
		}
	}
	
	@Override
	public ScaleType pehkui_getScaleType()
	{
		return this.pehkui$scaleType;
	}
	
	@Override
	public void pehkui_setScaleType(final ScaleType scaleType)
	{
		this.pehkui$scaleType = scaleType;
	}
	
	@Override
	public MinMaxBounds<? extends Number> pehkui_getScaleRange()
	{
		return this.pehkui$scaleRange;
	}
	
	@Override
	public void pehkui_setScaleRange(final MinMaxBounds.Doubles baseScaleRange)
	{
		this.pehkui$scaleRange = baseScaleRange;
	}
	
	@Override
	public ScaleType pehkui_getComputedScaleType()
	{
		return this.pehkui$computedScaleType;
	}
	
	@Override
	public void pehkui_setComputedScaleType(final ScaleType computedScaleType)
	{
		this.pehkui$computedScaleType = computedScaleType;
	}
	
	@Override
	public MinMaxBounds<? extends Number> pehkui_getComputedScaleRange()
	{
		return this.pehkui$computedScaleRange;
	}
	
	@Override
	public void pehkui_setComputedScaleRange(final MinMaxBounds.Doubles computedScaleRange)
	{
		this.pehkui$computedScaleRange = computedScaleRange;
	}
}
