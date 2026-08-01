package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
	@Shadow @Final @Mutable
	private ServerEntityManager<Entity> entityManager;
	
	@ModifyReturnValue(method = "getDebugString", at = @At("RETURN"))
	private String pehkui$getDebugString(String value)
	{
		String additional = "";
		
		for (final Entity entity : entityManager.getLookup().iterate())
		{
			float maxScale = 1.0F;
			ScaleType maxType = null;
			
			float scale;
			for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
			{
				scale = type.getScaleData(entity).getBaseScale();
				if (scale != type.getDefaultBaseScale() && scale > maxScale)
				{
					maxScale = scale;
					maxType = type;
				}
			}
			
			if (maxType != null)
			{
				additional += additional.isEmpty() ? ", pehkui:scaled_entities: {[{" : "}, {";
				
				final Identifier id = ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, maxType);
				
				final String idString = Pehkui.MOD_ID.equals(id.getNamespace()) ? id.getPath() : id.toString();
				
				additional += "\"" + entity.getUuidAsString() + "\":\"" + EntityType.getId(entity.getType()) + "\",\"" + idString + "\":" + maxScale;
			}
		}
		
		return additional.isEmpty() ? value : (value + additional + "}]}");
	}
}
