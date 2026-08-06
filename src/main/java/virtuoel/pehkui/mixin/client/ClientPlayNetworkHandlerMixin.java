package virtuoel.pehkui.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import virtuoel.pehkui.util.ScaleRenderUtils;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin
{
	@Unique
	private LocalPlayer pehkui$respawnOldPlayer = null;

	// 跨维度（1.21.2+ 所有版本）时服务端 RespawnPacket keep=(byte)3 走 shouldKeep(2) 分支不调 resetPos()。
	// 不能用 @Local 在 @At("RETURN") 提取 old/new（vanilla 1.21.x 局部变量槽复用，MixinExtras 验证失败），
	// 也不能 @Shadow minecraft 字段（refmap 不映射该字段）。
	// 故 HEAD 暂存 oldPlayer（Minecraft.getInstance().player = 旧玩家），RETURN 时已是新玩家。
	@Inject(method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", at = @At("HEAD"))
	private void pehkui$captureRespawnOldPlayer(CallbackInfo info)
	{
		pehkui$respawnOldPlayer = Minecraft.getInstance().player;
	}

	@Inject(method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", at = @At("RETURN"))
	private void pehkui$onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo info)
	{
		ScaleUtils.loadScaleOnRespawn(Minecraft.getInstance().player, pehkui$respawnOldPlayer, ScaleRenderUtils.wasPlayerAlive(packet));
	}
}
