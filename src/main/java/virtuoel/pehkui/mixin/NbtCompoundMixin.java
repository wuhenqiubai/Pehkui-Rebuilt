package virtuoel.pehkui.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.nbt.NbtCompound;
import virtuoel.pehkui.util.NbtCompoundExtensions;

@Mixin(NbtCompound.class)
public abstract class NbtCompoundMixin implements NbtCompoundExtensions
{
	@Shadow
	public abstract boolean containsUuid(String key);
	@Shadow
	public abstract UUID getUuid(String key);
	
	@Override
	public boolean pehkui_containsUuid(String key)
	{
		return containsUuid(key);
	}
	
	@Override
	public UUID pehkui_getUuid(String key)
	{
		return getUuid(key);
	}
}
