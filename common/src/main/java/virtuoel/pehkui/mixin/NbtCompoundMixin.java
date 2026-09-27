package virtuoel.pehkui.mixin;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import virtuoel.pehkui.util.NbtCompoundExtensions;

@Mixin(CompoundTag.class)
public abstract class NbtCompoundMixin implements NbtCompoundExtensions
{
	@Shadow
	public abstract boolean hasUUID(String key);
	@Shadow
	public abstract UUID getUUID(String key);
	
	@Override
	public boolean pehkui_containsUuid(String key)
	{
		return hasUUID(key);
	}
	
	@Override
	public UUID pehkui_getUuid(String key)
	{
		return getUUID(key);
	}
}
