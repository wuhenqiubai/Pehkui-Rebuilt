package virtuoel.pehkui.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

@Mixin(PlayerList.class)
public class PlayerManagerMixin
{
	@Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
	private void pehkui$onPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo info)
	{
		for (ScaleType type : ScaleRegistries.SCALE_TYPES.values())
		{
			type.getScaleData(player).markForSync(true);
		}
	}
}
