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
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.I18nUtils;

public class ScaleModifierArgumentType implements SerializableArgument.ContextFree<ScaleModifier>
{
	private static final Collection<String> EXAMPLES = Arrays.asList("identity", Pehkui.MOD_ID + ":identity");
	public static final ScaleModifierArgumentType INSTANCE = new ScaleModifierArgumentType();
	public static final SerializableArgument.Type<ScaleModifierArgumentType> TYPE = SerializableArgument.singletonType(INSTANCE);
	public static final DynamicCommandExceptionType INVALID_ENTRY_EXCEPTION = new DynamicCommandExceptionType(arg ->
	{
		return I18nUtils.translate("argument.pehkui.modifier.invalid", "Unknown scale modifier '%s'", arg);
	});

	@Override
	public SerializableArgument.Type<? extends SerializableArgument.ContextFree<ScaleModifier>> type()
	{
		return TYPE;
	}

	@Override
	public ScaleModifier parse(StringReader stringReader) throws CommandSyntaxException
	{
		final Identifier identifier = Identifier.read(stringReader);
		return Optional.ofNullable(ScaleRegistries.getEntry(ScaleRegistries.SCALE_MODIFIERS, identifier)).orElseThrow(() -> INVALID_ENTRY_EXCEPTION.create(identifier));
	}
	
	@Override
	public CompletableFuture<Suggestions> listSuggestions(CommandContext<? extends SuggestionContext> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return CommandUtils.suggestIdentifiersIgnoringNamespace(Pehkui.MOD_ID, ScaleRegistries.SCALE_MODIFIERS.keySet(), builder);
	}
	
	@Override
	public Collection<String> getExamples()
	{
		return EXAMPLES;
	}
	
	public static ArgumentType<ScaleModifier> scaleModifier()
	{
		return INSTANCE.toBrigadier();
	}
	
	public static ScaleModifier getScaleModifierArgument(CommandContext<CommandSourceStack> context, String name)
	{
		return context.getArgument(name, ScaleModifier.class);
	}
}
