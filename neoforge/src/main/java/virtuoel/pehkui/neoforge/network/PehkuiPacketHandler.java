package virtuoel.pehkui.neoforge.network;

import virtuoel.pehkui.neoforge.PehkuiClient;
import virtuoel.pehkui.network.ConfigSyncPayload;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.network.ScalePayload;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PehkuiPacketHandler
{
	public static void register(final RegisterPayloadHandlersEvent event)
	{
		final PayloadRegistrar registrar = event.registrar("3.0.0");
		registrar.playToClient(ScalePayload.ID, ScalePayload.CODEC, (payload, context) -> PehkuiClient.handleScalePacket(context, payload));
		registrar.playToClient(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC, (payload, context) -> PehkuiClient.handleConfigSyncPacket(context, payload));
		registrar.playToClient(DebugPayload.ID, DebugPayload.CODEC, (payload, context) -> PehkuiClient.handleDebugPacket(context, payload));
	}
}
