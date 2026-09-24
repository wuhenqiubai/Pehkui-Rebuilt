package virtuoel.pehkui.util;

public class PehkuiClientPlayerLikeStateScale
{
	private static final ThreadLocal<Float> MOTION_SCALE = new ThreadLocal<>();
	
	public static void setMotionScale(float scale)
	{
		MOTION_SCALE.set(scale);
	}
	
	public static void clearMotionScale()
	{
		MOTION_SCALE.remove();
	}
	
	public static float getMotionScale()
	{
		final Float scale = MOTION_SCALE.get();
		
		return scale == null ? 1.0F : scale.floatValue();
	}
}
