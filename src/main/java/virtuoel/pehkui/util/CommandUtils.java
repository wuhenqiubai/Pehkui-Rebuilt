package virtuoel.pehkui.util;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.command.argument.ScaleEasingArgumentType;
import virtuoel.pehkui.command.argument.ScaleModifierArgumentType;
import virtuoel.pehkui.command.argument.ScaleOperationArgumentType;
import virtuoel.pehkui.command.argument.ScaleTypeArgumentType;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.server.command.ScaleCommand;

public class CommandUtils
{
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Pehkui.MOD_ID);

	public static void registerArgumentTypes(final IEventBus modEventBus)
	{
		registerArgumentTypes(CommandUtils::registerConstantArgumentType);

		COMMAND_ARGUMENT_TYPES.register(modEventBus);
	}

	public static void registerCommands(final CommandDispatcher<CommandSourceStack> dispatcher)
	{
		ScaleCommand.register(dispatcher);
		DebugCommand.register(dispatcher);
	}

	public static void registerArgumentTypes(ArgumentTypeConsumer consumer)
	{
		consumer.register(Pehkui.id("scale_type"), ScaleTypeArgumentType.class, ScaleTypeArgumentType::scaleType);
		consumer.register(Pehkui.id("scale_modifier"), ScaleModifierArgumentType.class, ScaleModifierArgumentType::scaleModifier);
		consumer.register(Pehkui.id("scale_operation"), ScaleOperationArgumentType.class, ScaleOperationArgumentType::operation);
		consumer.register(Pehkui.id("scale_easing"), ScaleEasingArgumentType.class, ScaleEasingArgumentType::scaleEasing);
	}

	@FunctionalInterface
	public interface ArgumentTypeConsumer
	{
		<T extends ArgumentType<?>> void register(Identifier id, Class<T> argClass, Supplier<T> supplier);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends ArgumentType<?>> void registerConstantArgumentType(Identifier id, Class<? extends T> argClass, Supplier<T> supplier)
	{
		final ArgumentTypeInfo info = SingletonArgumentInfo.contextFree(supplier);
		COMMAND_ARGUMENT_TYPES.register(id.getPath(), () -> info);
		// 建立参数类型类 → ArgumentTypeInfo 的映射（游戏按类解析命令参数时使用），DeferredRegister 只注册 id → info 不够
		ArgumentTypeInfos.registerByClass((Class) argClass, info);
	}

	public static void sendFeedback(CommandSourceStack source, Supplier<Component> text, boolean broadcastToOps)
	{
		source.sendSuccess(text, broadcastToOps);
	}

	public static boolean testFloatRange(MinMaxBounds.Doubles range, float value)
	{
		return range.matches((double) value);
	}

	public static CompletableFuture<Suggestions> suggestIdentifiersIgnoringNamespace(String namespace, Iterable<Identifier> candidates, SuggestionsBuilder builder)
	{
		forEachMatchingIgnoringNamespace(
			namespace,
			candidates,
			builder.getRemaining().toLowerCase(Locale.ROOT),
			Function.identity(),
			id -> builder.suggest(String.valueOf(id))
		);

		return builder.buildFuture();
	}

	public static <T> void forEachMatchingIgnoringNamespace(String namespace, Iterable<T> candidates, String string, Function<T, Identifier> idFunc, Consumer<T> action)
	{
		final boolean hasColon = string.indexOf(':') > -1;

		Identifier id;
		for (final T object : candidates)
		{
			id = idFunc.apply(object);
			if (hasColon)
			{
				if (wordStartsWith(string, id.toString(), '_'))
				{
					action.accept(object);
				}
			}
			else if (
				wordStartsWith(string, id.getNamespace(), '_') ||
				id.getNamespace().equals(namespace) &&
				wordStartsWith(string, id.getPath(), '_')
			)
			{
				action.accept(object);
			}
		}
	}

	public static boolean wordStartsWith(String string, String substring, char wordSeparator)
	{
		for (int i = 0; !substring.startsWith(string, i); i++)
		{
			i = substring.indexOf(wordSeparator, i);
			if (i < 0)
			{
				return false;
			}
		}

		return true;
	}
}
