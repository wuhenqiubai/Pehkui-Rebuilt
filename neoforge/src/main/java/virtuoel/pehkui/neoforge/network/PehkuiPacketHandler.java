package virtuoel.pehkui.neoforge.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import virtuoel.pehkui.neoforge.PehkuiClient;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePayload;

/**
 * NeoForge 侧 payload 注册。payload 类本身在 common（不含加载器 API），
 * 具体处理逻辑委派给平台侧的 {@link PehkuiClient}。
 */
public class PehkuiPacketHandler
{
	public static void register(final RegisterPayloadHandlersEvent event)
	{
		final PayloadRegistrar registrar = event.registrar("3.0.0");
		registrar.playToClient(ScalePayload.ID, ScalePayload.CODEC, (payload, context) -> PehkuiClient.handleScalePacket(context, payload));
		// ConfigSyncPayload 不在此注册：NeoForge 侧配置走原生 ModConfigSpec 同步
		// （上游形态），没有 KanosConfig 那套 synced(...) 需要广播。
		registrar.playToClient(DebugPayload.ID, DebugPayload.CODEC, (payload, context) -> PehkuiClient.handleDebugPacket(context, payload));
	}
}
