package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin
{
	@Inject(method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", at = @At("RETURN"))
	private void pehkui$onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo info, @Local(ordinal = 0) LocalPlayer oldPlayer, @Local(ordinal = 1) LocalPlayer newPlayer)
	{
		ScaleUtils.loadScaleOnRespawn(newPlayer, oldPlayer, ScaleRenderUtils.wasPlayerAlive(packet));
	}
}
