package virtuoel.pehkui.mixin.client;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import virtuoel.pehkui.util.ScaleRenderUtils;

@Mixin(ClientLevel.class)
public class ClientWorldMixin
{
	@Inject(method = "fillReportDetails", at = @At(value = "RETURN"))
	private void pehkui$addDetailsToCrashReport(CrashReport report, CallbackInfoReturnable<CrashReportCategory> info)
	{
		ScaleRenderUtils.addDetailsToCrashReport(info.getReturnValue());
	}
}
