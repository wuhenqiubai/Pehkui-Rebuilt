package virtuoel.pehkui.api;

import java.util.ArrayList;
import java.util.List;

/**
 * 轻量事件，替代 Fabric API 的 {@code net.fabricmc.fabric.api.event.Event}。
 * 语义与 Fabric 一致：{@link #register} 添加监听器，{@link #invoker} 返回组合回调（调用时按注册顺序触发全部监听器）。
 */
public class ScaleEvent
{
	private final List<ScaleEventCallback> listeners = new ArrayList<ScaleEventCallback>();

	public void register(ScaleEventCallback listener)
	{
		listeners.add(listener);
	}

	public ScaleEventCallback invoker()
	{
		return data ->
		{
			for (final ScaleEventCallback listener : listeners)
			{
				listener.onEvent(data);
			}
		};
	}
}
