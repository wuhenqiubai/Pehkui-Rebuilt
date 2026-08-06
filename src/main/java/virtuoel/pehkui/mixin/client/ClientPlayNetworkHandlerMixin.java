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
	// 注入点不能挂在 LocalPlayer.resetPos()——跨维度（1.21.2+ 所有版本）时服务端 RespawnPacket keep=(byte)3，shouldKeep((byte)2)=true 走 if 分支（保留实体数据），resetPos() 只在 else 分支（死亡重生）调用。
	// 故改为 @At("RETURN")，覆盖死亡重生 + 跨维度所有路径（oldPlayer=@Local(0), newPlayer=@Local(1)）
	@Inject(method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", at = @At("RETURN"))
	private void pehkui$onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo info, @Local(ordinal = 0) LocalPlayer oldPlayer, @Local(ordinal = 1) LocalPlayer newPlayer)
	{
		ScaleUtils.loadScaleOnRespawn(newPlayer, oldPlayer, ScaleRenderUtils.wasPlayerAlive(packet));
	}
}
