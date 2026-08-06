package virtuoel.pehkui.util;

import net.minecraft.world.entity.EntityType;

public interface PehkuiEntityRenderStateExtensions
{
	float getModelWidthScale();

	void setModelWidthScale(float scale);

	float getModelHeightScale();

	void setModelHeightScale(float scale);

	EntityType<?> getEntityType();

	void setEntityType(EntityType<?> entityType);
}
