package virtuoel.pehkui.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin
{
	@Shadow
	public ServerPlayer player;
	
	@ModifyArg(method = "isEntityCollidingWithAnythingNew(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;DDD)Z", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;deflate(D)Lnet/minecraft/world/phys/AABB;", ordinal = 0))
	private double pehkui$onVehicleMove$contract(double value)
	{
		final float scale = ScaleUtils.getMotionScale(player);
		return scale < 1.0F ? value * scale : value;
	}
	
	@ModifyArg(method = "handleMoveVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
	private Vec3 pehkui$onVehicleMove$move(MoverType type, Vec3 movement)
	{
		final float scale = ScaleUtils.getMotionScale(player.getRootVehicle());
		return scale != 1.0F ? movement.scale(1.0F / scale) : movement;
	}
	
	@ModifyArg(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
	private Vec3 pehkui$onPlayerMove$move(MoverType type, Vec3 movement)
	{
		final float scale = ScaleUtils.getMotionScale(player);
		return scale != 1.0F ? movement.scale(1.0F / scale) : movement;
	}
}
