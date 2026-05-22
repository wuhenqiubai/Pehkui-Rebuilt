package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin
{
	@Shadow @Final Entity entity;
	@Shadow @Final EntityTrackerEntry.TrackerPacketSender packetSender;
	
	@Inject(at = @At("TAIL"), method = "tick")
	private void pehkui$tick(CallbackInfo info)
	{
		ScaleUtils.syncScalesIfNeeded(entity, this::pehkui$sendSyncPacket);
	}
	
	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "doubleValue=7.62939453125E-6D"))
	private double pehkui$tick$minimumSquaredDistance(double value)
	{
		final float scale = ScaleUtils.getMotionScale(entity);
		
		return scale < 1.0F ? value * scale * scale : value;
	}
	
	@Inject(at = @At("HEAD"), method = "syncEntityData")
	private void pehkui$syncEntityData(CallbackInfo info)
	{
		ScaleUtils.syncScalesIfNeeded(entity, this::pehkui$sendSyncPacket);
	}
	
	@SuppressWarnings("unchecked")
	private void pehkui$sendSyncPacket(Packet<?> packet)
	{
		packetSender.sendToSelfAndListeners((Packet<? super ClientPlayPacketListener>) packet);
	}
}
