package virtuoel.pehkui.util;

import net.minecraft.world.entity.EntityType;

public interface PehkuiEntityRenderStateExtensions
{
	float pehkui$getModelWidthScale();

	void pehkui$setModelWidthScale(float scale);

	float pehkui$getModelHeightScale();

	void pehkui$setModelHeightScale(float scale);

	EntityType<?> pehkui$getEntityType();

	void pehkui$setEntityType(EntityType<?> entityType);
}
