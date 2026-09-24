package virtuoel.pehkui.network;

import java.util.Collection;
import net.minecraft.network.FriendlyByteBuf;
import virtuoel.pehkui.util.ConfigSyncUtils;
import virtuoel.pehkui.util.ConfigSyncUtils.SyncableConfigEntry;

public class ConfigSyncPacket
{
	public Collection<SyncableConfigEntry<?>> configEntries;
	public Runnable action;
	
	public ConfigSyncPacket(final Collection<SyncableConfigEntry<?>> configEntries)
	{
		this.configEntries = configEntries;
	}
	
	public ConfigSyncPacket(final FriendlyByteBuf buf)
	{
		this.action = ConfigSyncUtils.readConfigs(buf);
	}
	
	public void write(final FriendlyByteBuf buf)
	{
		ConfigSyncUtils.write(configEntries, buf);
	}
}
