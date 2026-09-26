package virtuoel.pehkui.neoforge;

import java.util.function.Supplier;

import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.util.CommandUtils;

/**
 * NeoForge 版命令参数类型注册。common 的 {@link CommandUtils} 已去平台化（不依赖 neoforge 的 DeferredRegister），
 * 这段 neoforge 专属注册逻辑放到平台模块。
 */
public class CommandUtilsImpl
{
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Pehkui.MOD_ID);

	public static void register(final IEventBus modEventBus)
	{
		CommandUtils.registerArgumentTypes(CommandUtilsImpl::registerConstantArgumentType);
		COMMAND_ARGUMENT_TYPES.register(modEventBus);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T extends ArgumentType<?>> void registerConstantArgumentType(Identifier id, Class<T> argClass, Supplier<T> supplier)
	{
		final ArgumentTypeInfo info = SingletonArgumentInfo.contextFree(supplier);
		COMMAND_ARGUMENT_TYPES.register(id.getPath(), () -> info);
		// 建立参数类型类 → ArgumentTypeInfo 的映射（游戏按类解析命令参数时使用），DeferredRegister 只注册 id → info 不够
		ArgumentTypeInfos.registerByClass((Class) argClass, info);
	}
}
