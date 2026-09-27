package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ServerEntity.class)
public abstract class EntityTrackerEntryMixin
{
	@Shadow @Final
	private Entity entity;
	@Shadow
	protected abstract void broadcastAndSend(Packet<?> packet);
	
	@Inject(at = @At("TAIL"), method = "sendChanges")
	private void pehkui$tick(CallbackInfo info)
	{
		ScaleUtils.syncScalesIfNeeded(entity, this::broadcastAndSend);
	}
	
	@ModifyExpressionValue(method = "sendChanges", at = @At(value = "CONSTANT", args = "doubleValue=7.62939453125E-6D"))
	private double pehkui$tick$minimumSquaredDistance(double value)
	{
		final float scale = ScaleUtils.getMotionScale(entity);
		
		return scale < 1.0F ? value * scale * scale : value;
	}
	
	@Inject(at = @At("HEAD"), method = "sendDirtyEntityData")
	private void pehkui$syncEntityData(CallbackInfo info)
	{
		ScaleUtils.syncScalesIfNeeded(entity, this::broadcastAndSend);
	}
}
