package virtuoel.pehkui.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.SerializableArgument;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.resources.Identifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.I18nUtils;

public class ScaleEasingArgumentType implements SerializableArgument.ContextFree<Float2FloatFunction>
{
	private static final Collection<String> EXAMPLES = Arrays.asList("linear", Pehkui.MOD_ID + ":linear");
	public static final ScaleEasingArgumentType INSTANCE = new ScaleEasingArgumentType();
	public static final SerializableArgument.Type<ScaleEasingArgumentType> TYPE = SerializableArgument.singletonType(INSTANCE);
	public static final DynamicCommandExceptionType INVALID_ENTRY_EXCEPTION = new DynamicCommandExceptionType(arg ->
	{
		return I18nUtils.translate("argument.pehkui.easing.invalid", "Unknown easing '%s'", arg);
	});

	@Override
	public SerializableArgument.Type<? extends SerializableArgument.ContextFree<Float2FloatFunction>> type()
	{
		return TYPE;
	}

	@Override
	public Float2FloatFunction parse(StringReader stringReader) throws CommandSyntaxException
	{
		final Identifier identifier = Identifier.read(stringReader);
		return Optional.ofNullable(ScaleRegistries.getEntry(ScaleRegistries.SCALE_EASINGS, identifier)).orElseThrow(() -> INVALID_ENTRY_EXCEPTION.create(identifier));
	}
	
	@Override
	public CompletableFuture<Suggestions> listSuggestions(CommandContext<? extends SuggestionContext> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return CommandUtils.suggestIdentifiersIgnoringNamespace(Pehkui.MOD_ID, ScaleRegistries.SCALE_EASINGS.keySet(), builder);
	}
	
	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
	
	public static ArgumentType<Float2FloatFunction> scaleEasing()
	{
		return INSTANCE.toBrigadier();
	}
	
	public static Float2FloatFunction getScaleEasingArgument(CommandContext<CommandSourceStack> context, String name)
	{
		return context.getArgument(name, Float2FloatFunction.class);
	}
}
