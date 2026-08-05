package virtuoel.pehkui.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PehkuiPacketHandler
{
	public static void register(final RegisterPayloadHandlersEvent event)
	{
		final PayloadRegistrar registrar = event.registrar("3.0.0");
		registrar.playToClient(ScalePayload.ID, ScalePayload.CODEC, ScalePayload::handle);
		registrar.playToClient(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC, ConfigSyncPayload::handle);
		registrar.playToClient(DebugPayload.ID, DebugPayload.CODEC, DebugPayload::handle);
	}
}
