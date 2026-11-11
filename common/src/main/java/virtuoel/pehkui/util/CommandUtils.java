package virtuoel.pehkui.util;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.SerializableArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.command.argument.ScaleEasingArgumentType;
import virtuoel.pehkui.command.argument.ScaleModifierArgumentType;
import virtuoel.pehkui.command.argument.ScaleOperationArgumentType;
import virtuoel.pehkui.command.argument.ScaleTypeArgumentType;
import virtuoel.pehkui.server.command.DebugCommand;
import virtuoel.pehkui.server.command.ScaleCommand;

public class CommandUtils
{
	public static void registerCommands(final CommandDispatcher<CommandSourceStack> dispatcher)
	{
		ScaleCommand.register(dispatcher);
		DebugCommand.register(dispatcher);
	}

	public static void registerArgumentTypes(ArgumentTypeConsumer consumer)
	{
		consumer.register(Pehkui.id("scale_type"), ScaleTypeArgumentType.INSTANCE);
		consumer.register(Pehkui.id("scale_modifier"), ScaleModifierArgumentType.INSTANCE);
		consumer.register(Pehkui.id("scale_operation"), ScaleOperationArgumentType.INSTANCE);
		consumer.register(Pehkui.id("scale_easing"), ScaleEasingArgumentType.INSTANCE);
	}

	@FunctionalInterface
	public interface ArgumentTypeConsumer
	{
		void register(Identifier id, SerializableArgument<?> argument);
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
