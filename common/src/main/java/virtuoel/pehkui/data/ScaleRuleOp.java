package virtuoel.pehkui.data;

import java.util.function.DoubleBinaryOperator;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;

/**
 * A single operation inside a scale rule, applied as {@code result = operation(base, value)}.
 * <p>
 * The operation itself comes from {@code ScaleRegistries.SCALE_OPERATIONS}, so it is the same
 * set the {@code /scale operation} command uses. The optional per-scale adjustments
 * ({@code delay} / {@code easing} / {@code persist}) ride along in the same JSON object;
 * a {@code null} / negative value means "not specified, leave it alone".
 */
public final class ScaleRuleOp
{
	private final DoubleBinaryOperator operation;
	private final float value;
	private final int tickDelay;
	private final Float2FloatFunction easing;
	private final Boolean persist;

	public ScaleRuleOp(DoubleBinaryOperator operation, float value, int tickDelay, @Nullable Float2FloatFunction easing, @Nullable Boolean persist)
	{
		this.operation = operation;
		this.value = value;
		this.tickDelay = tickDelay;
		this.easing = easing;
		this.persist = persist;
	}

	public float apply(float base)
	{
		return (float) operation.applyAsDouble(base, value);
	}

	public DoubleBinaryOperator getOperation()
	{
		return operation;
	}

	public float getValue()
	{
		return value;
	}

	/**
	 * @return the tick delay to apply, or a negative value when the rule does not set one
	 */
	public int getTickDelay()
	{
		return tickDelay;
	}

	@Nullable
	public Float2FloatFunction getEasing()
	{
		return easing;
	}

	@Nullable
	public Boolean getPersist()
	{
		return persist;
	}
}
