package virtuoel.pehkui.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.util.ScaleUtils;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin
{
	@Inject(at = @At("HEAD"), method = "restoreFrom")
	private void pehkui$copyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo info)
	{
		ScaleUtils.loadScaleOnRespawn((ServerPlayer) (Object) this, oldPlayer, alive);
	}
}
