package virtuoel.pehkui.mixin;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import virtuoel.pehkui.Pehkui;

public class PehkuiMixinConfigPlugin implements IMixinConfigPlugin
{
	private static final String MIXIN_PACKAGE = "virtuoel.pehkui.mixin";

	@Override
	public void onLoad(String mixinPackage)
	{
		if (!mixinPackage.startsWith(MIXIN_PACKAGE))
		{
			throw new IllegalArgumentException(
				String.format("Invalid package: Expected \"%s\", but found \"%s\".", mixinPackage, mixinPackage)
			);
		}
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@ApiStatus.Experimental
	private static final boolean DISABLE_THREAD_SAFETY = Boolean.parseBoolean(System.getProperty("pehkui.disableThreadSafety"));

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		if (!mixinClassName.startsWith(MIXIN_PACKAGE))
		{
			throw new IllegalArgumentException(
				String.format("Invalid package for class \"%s\": Expected \"%s\", but found \"%s\".", targetClassName, MIXIN_PACKAGE, mixinClassName)
			);
		}

		if (mixinClassName.endsWith("ThreadSafeScaledEntityMixin"))
		{
			return !DISABLE_THREAD_SAFETY;
		}
		else if (mixinClassName.endsWith("ThreadUnsafeScaledEntityMixin"))
		{
			if (DISABLE_THREAD_SAFETY)
			{
				Pehkui.LOGGER.warn("Found property -Dpehkui.disableThreadSafety=true. The synchronized() blocks in scale getters have been disabled.");
			}

			return DISABLE_THREAD_SAFETY;
		}

		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{

	}

	@Override
	public List<String> getMixins()
	{
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{

	}
}
