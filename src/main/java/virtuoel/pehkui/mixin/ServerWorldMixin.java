package virtuoel.pehkui.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

@Mixin(ServerLevel.class)
public class ServerWorldMixin
{
	@Shadow @Final @Mutable
	private PersistentEntitySectionManager<Entity> entityManager;
	
	@ModifyReturnValue(method = "getWatchdogStats", at = @At("RETURN"))
	private String pehkui$getDebugString(String value)
	{
		String additional = "";
		
		for (final Entity entity : entityManager.getEntityGetter().getAll())
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
				
				final ResourceLocation id = ScaleRegistries.getId(ScaleRegistries.SCALE_TYPES, maxType);
				
				final String idString = Pehkui.MOD_ID.equals(id.getNamespace()) ? id.getPath() : id.toString();
				
				additional += "\"" + entity.getStringUUID() + "\":\"" + EntityType.getKey(entity.getType()) + "\",\"" + idString + "\":" + maxScale;
			}
		}
		
		return additional.isEmpty() ? value : (value + additional + "}]}");
	}
}
