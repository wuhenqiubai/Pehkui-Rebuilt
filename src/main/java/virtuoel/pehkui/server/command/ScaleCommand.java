package virtuoel.pehkui.server.command;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;
import virtuoel.pehkui.command.argument.ScaleEasingArgumentType;
import virtuoel.pehkui.command.argument.ScaleModifierArgumentType;
import virtuoel.pehkui.command.argument.ScaleOperationArgumentType;
import virtuoel.pehkui.command.argument.ScaleTypeArgumentType;
import virtuoel.pehkui.mixin.DataCommandInvoker;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.I18nUtils;
import virtuoel.pehkui.util.PehkuiEntityExtensions;

public class ScaleCommand
{
	public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
	{
		if (FMLEnvironment.isProduction() && !PehkuiConfig.COMMON.enableCommands.get())
		{
			return;
		}
		
		final LiteralArgumentBuilder<CommandSourceStack> builder =
			Commands.literal("scale")
			.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
		
		registerOperation(builder);
		registerRandomize(builder);
		registerGet(builder);
		registerCompute(builder);
		registerReset(builder);
		registerModifier(builder);
		registerDelay(builder);
		registerEasing(builder);
		registerPersist(builder);
		registerNbt(builder);
		
		commandDispatcher.register(builder);
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerOperation(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.argument("operation", ScaleOperationArgumentType.operation())
				.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
					.then(Commands.argument("value", FloatArgumentType.floatArg())
						.then(Commands.argument("targets", EntityArgument.entities())
							.executes(context ->
							{
								final float scale = FloatArgumentType.getFloat(context, "value");
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								
								for (final Entity e : EntityArgument.getEntities(context, "targets"))
								{
									final ScaleData data = type.getScaleData(e);
									final ScaleOperationArgumentType.Operation operation = ScaleOperationArgumentType.getOperation(context, "operation");
									
									data.setTargetScale((float) operation.apply(data.getTargetScale(), scale));
								}
								
								return 1;
							})
						)
						.executes(context ->
						{
							final float scale = FloatArgumentType.getFloat(context, "value");
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							
							final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
							final ScaleOperationArgumentType.Operation operation = ScaleOperationArgumentType.getOperation(context, "operation");
							
							data.setTargetScale((float) operation.apply(data.getTargetScale(), scale));
							
							return 1;
						})
					)
				)
				.then(Commands.argument("value", FloatArgumentType.floatArg())
					.then(Commands.argument("targets", EntityArgument.entities())
						.executes(context ->
						{
							final float scale = FloatArgumentType.getFloat(context, "value");
							
							for (final Entity e : EntityArgument.getEntities(context, "targets"))
							{
								final ScaleData data = ScaleTypes.BASE.getScaleData(e);
								final ScaleOperationArgumentType.Operation operation = ScaleOperationArgumentType.getOperation(context, "operation");
								
								data.setTargetScale((float) operation.apply(data.getTargetScale(), scale));
							}
							
							return 1;
						})
					)
					.executes(context ->
					{
						final float scale = FloatArgumentType.getFloat(context, "value");
						
						final ScaleData data = ScaleTypes.BASE.getScaleData(context.getSource().getEntityOrException());
						final ScaleOperationArgumentType.Operation operation = ScaleOperationArgumentType.getOperation(context, "operation");
						
						data.setTargetScale((float) operation.apply(data.getTargetScale(), scale));
						
						return 1;
					})
				)
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerRandomize(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("randomize")
				.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
					.then(Commands.argument("minOperation", ScaleOperationArgumentType.operation())
						.then(Commands.argument("minValue", FloatArgumentType.floatArg())
							.then(Commands.argument("maxOperation", ScaleOperationArgumentType.operation())
								.then(Commands.argument("maxValue", FloatArgumentType.floatArg())
									.then(Commands.argument("targets", EntityArgument.entities())
										.executes(context ->
										{
											final float minValue = FloatArgumentType.getFloat(context, "minValue");
											final float maxValue = FloatArgumentType.getFloat(context, "maxValue");
											
											final ScaleOperationArgumentType.Operation minOperation = ScaleOperationArgumentType.getOperation(context, "minOperation");
											final ScaleOperationArgumentType.Operation maxOperation = ScaleOperationArgumentType.getOperation(context, "maxOperation");
											
											final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
											
											double min, max, target;
											for (final Entity e : EntityArgument.getEntities(context, "targets"))
											{
												final ScaleData data = type.getScaleData(e);
												
												target = data.getTargetScale();
												min = minOperation.apply(target, minValue);
												max = maxOperation.apply(target, maxValue);
												
												if (max < min)
												{
													final double temp = min;
													min = max;
													max = temp;
												}
												
												data.setTargetScale((float) (min + (RANDOM.nextFloat() * (max - min))));
											}
											
											return 1;
										})
									)
									.executes(context ->
									{
										final float minValue = FloatArgumentType.getFloat(context, "minValue");
										final float maxValue = FloatArgumentType.getFloat(context, "maxValue");
										
										final ScaleOperationArgumentType.Operation minOperation = ScaleOperationArgumentType.getOperation(context, "minOperation");
										final ScaleOperationArgumentType.Operation maxOperation = ScaleOperationArgumentType.getOperation(context, "maxOperation");
										
										final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
										
										final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
										
										final double target = data.getTargetScale();
										double min = minOperation.apply(target, minValue);
										double max = maxOperation.apply(target, maxValue);
										
										if (max < min)
										{
											final double temp = min;
											min = max;
											max = temp;
										}
										
										data.setTargetScale((float) (min + (RANDOM.nextFloat() * (max - min))));
										
										return 1;
									})
								)
							)
						)
					)
				)
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerGet(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("get")
				.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
					.then(Commands.argument("entity", EntityArgument.entity())
						.then(Commands.argument("scalingFactor", FloatArgumentType.floatArg())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final float scale = type.getScaleData(EntityArgument.getEntity(context, "entity")).getBaseScale();
								final int scaled = (int) (scale * FloatArgumentType.getFloat(context, "scalingFactor"));
								CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale, scaled), false);
								
								return scaled;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final float scale = type.getScaleData(EntityArgument.getEntity(context, "entity")).getBaseScale();
							CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale), false);
							
							return (int) scale;
						})
					)
					.then(Commands.argument("scalingFactor", FloatArgumentType.floatArg())
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final float scale = type.getScaleData(context.getSource().getEntityOrException()).getBaseScale();
							final int scaled = (int) (scale * FloatArgumentType.getFloat(context, "scalingFactor"));
							CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale, scaled), false);
							
							return scaled;
						})
					)
					.executes(context ->
					{
						final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
						final float scale = type.getScaleData(context.getSource().getEntityOrException()).getBaseScale();
						CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale), false);
						
						return (int) scale;
					})
				)
				.then(Commands.argument("scalingFactor", FloatArgumentType.floatArg())
					.executes(context ->
					{
						final float scale = ScaleTypes.BASE.getScaleData(context.getSource().getEntityOrException()).getBaseScale();
						final int scaled = (int) (scale * FloatArgumentType.getFloat(context, "scalingFactor"));
						CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale, scaled), false);
						
						return scaled;
					})
				)
				.then(Commands.argument("entity", EntityArgument.entity())
					.then(Commands.argument("scalingFactor", FloatArgumentType.floatArg())
						.executes(context ->
						{
							final float scale = ScaleTypes.BASE.getScaleData(EntityArgument.getEntity(context, "entity")).getBaseScale();
							final int scaled = (int) (scale * FloatArgumentType.getFloat(context, "scalingFactor"));
							CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale, scaled), false);
							
							return scaled;
						})
					)
					.executes(context ->
					{
						final float scale = ScaleTypes.BASE.getScaleData(EntityArgument.getEntity(context, "entity")).getBaseScale();
						CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale), false);
						
						return (int) scale;
					})
				)
				.executes(context ->
				{
					final float scale = ScaleTypes.BASE.getScaleData(context.getSource().getEntityOrException()).getBaseScale();
					CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale), false);
					
					return (int) scale;
				})
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerCompute(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("compute")
				.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
					.then(Commands.argument("entity", EntityArgument.entity())
						.then(Commands.argument("scalingFactor", FloatArgumentType.floatArg())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final float scale = type.getScaleData(EntityArgument.getEntity(context, "entity")).getScale();
								final int scaled = (int) (scale * FloatArgumentType.getFloat(context, "scalingFactor"));
								CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale, scaled), false);
								
								return scaled;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final float scale = type.getScaleData(EntityArgument.getEntity(context, "entity")).getScale();
							CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale), false);
							
							return (int) scale;
						})
					)
					.then(Commands.argument("scalingFactor", FloatArgumentType.floatArg())
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final float scale = type.getScaleData(context.getSource().getEntityOrException()).getScale();
							final int scaled = (int) (scale * FloatArgumentType.getFloat(context, "scalingFactor"));
							CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale, scaled), false);
							
							return scaled;
						})
					)
					.executes(context ->
					{
						final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
						final float scale = type.getScaleData(context.getSource().getEntityOrException()).getScale();
						CommandUtils.sendFeedback(context.getSource(), () -> scaleText(scale), false);
						
						return (int) scale;
					})
				)
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerReset(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("reset")
				.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
					.then(Commands.argument("targets", EntityArgument.entities())
						.executes(context ->
						{
							for (final Entity e : EntityArgument.getEntities(context, "targets"))
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final ScaleData data = type.getScaleData(e);
								final Boolean persist = data.getPersistence();
								data.resetScale();
								data.setPersistence(persist);
							}
							
							return 1;
						})
					)
					.executes(context ->
					{
						final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
						final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
						final Boolean persist = data.getPersistence();
						data.resetScale();
						data.setPersistence(persist);
						
						return 1;
					})
				)
				.then(Commands.argument("targets", EntityArgument.entities())
					.executes(context ->
					{
						for (final Entity e : EntityArgument.getEntities(context, "targets"))
						{
							for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
							{
								final ScaleData data = type.getScaleData(e);
								final Boolean persist = data.getPersistence();
								data.resetScale();
								data.setPersistence(persist);
							}
						}
						
						return 1;
					})
				)
				.executes(context ->
				{
					for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
					{
						final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
						final Boolean persist = data.getPersistence();
						data.resetScale();
						data.setPersistence(persist);
					}
					
					return 1;
				})
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerModifier(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("modifier")
				.then(Commands.literal("get")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("entity", EntityArgument.entity())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final ScaleData data = type.getScaleData(EntityArgument.getEntity(context, "entity"));
								
								final String modifierString = String.join(", ", data.getBaseValueModifiers().stream().map(e -> ScaleRegistries.getId(ScaleRegistries.SCALE_MODIFIERS, e).toString()).collect(Collectors.toList()));
								
								CommandUtils.sendFeedback(context.getSource(), () -> modifierText(modifierString), false);
								
								return 1;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							
							final String modifierString = String.join(", ", type.getDefaultBaseValueModifiers().stream().map(e -> ScaleRegistries.getId(ScaleRegistries.SCALE_MODIFIERS, e).toString()).collect(Collectors.toList()));
							
							CommandUtils.sendFeedback(context.getSource(), () -> modifierText(modifierString), false);
							
							return 1;
						})
					)
				)
				.then(Commands.literal("add")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("scale_modifier", ScaleModifierArgumentType.scaleModifier())
							.then(Commands.argument("targets", EntityArgument.entities())
								.executes(context ->
								{
									for (final Entity e : EntityArgument.getEntities(context, "targets"))
									{
										final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
										final ScaleModifier modifier = ScaleModifierArgumentType.getScaleModifierArgument(context, "scale_modifier");
										final ScaleData data = type.getScaleData(e);
										data.getBaseValueModifiers().add(modifier);
									}
									
									return 1;
								})
							)
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final ScaleModifier modifier = ScaleModifierArgumentType.getScaleModifierArgument(context, "scale_modifier");
								final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
								data.getBaseValueModifiers().add(modifier);
								
								return 1;
							})
						)
					)
				)
				.then(Commands.literal("remove")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("scale_modifier", ScaleModifierArgumentType.scaleModifier())
							.then(Commands.argument("targets", EntityArgument.entities())
								.executes(context ->
								{
									for (final Entity e : EntityArgument.getEntities(context, "targets"))
									{
										final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
										final ScaleModifier modifier = ScaleModifierArgumentType.getScaleModifierArgument(context, "scale_modifier");
										final ScaleData data = type.getScaleData(e);
										data.getBaseValueModifiers().remove(modifier);
									}
									
									return 1;
								})
							)
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final ScaleModifier modifier = ScaleModifierArgumentType.getScaleModifierArgument(context, "scale_modifier");
								final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
								data.getBaseValueModifiers().remove(modifier);
								
								return 1;
							})
						)
					)
				)
				.then(Commands.literal("reset")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("targets", EntityArgument.entities())
							.executes(context ->
							{
								for (final Entity e : EntityArgument.getEntities(context, "targets"))
								{
									final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
									final ScaleData data = type.getScaleData(e);
									
									final SortedSet<ScaleModifier> baseValueModifiers = data.getBaseValueModifiers();
									
									baseValueModifiers.clear();
									baseValueModifiers.addAll(type.getDefaultBaseValueModifiers());
								}
								
								return 1;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
							
							final SortedSet<ScaleModifier> baseValueModifiers = data.getBaseValueModifiers();
							
							baseValueModifiers.clear();
							baseValueModifiers.addAll(type.getDefaultBaseValueModifiers());
							
							return 1;
						})
					)
				)
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerDelay(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("delay")
				.then(Commands.literal("set")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("ticks", IntegerArgumentType.integer())
							.then(Commands.argument("targets", EntityArgument.entities())
								.executes(context ->
								{
									final int ticks = IntegerArgumentType.getInteger(context, "ticks");
									final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
									
									for (final Entity e : EntityArgument.getEntities(context, "targets"))
									{
										final ScaleData data = type.getScaleData(e);
										
										data.setScaleTickDelay(ticks);
									}
									
									return 1;
								})
							)
							.executes(context ->
							{
								final int ticks = IntegerArgumentType.getInteger(context, "ticks");
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								
								final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
								
								data.setScaleTickDelay(ticks);
								
								return 1;
							})
						)
					)
					.then(Commands.argument("ticks", IntegerArgumentType.integer())
						.then(Commands.argument("targets", EntityArgument.entities())
							.executes(context ->
							{
								final int ticks = IntegerArgumentType.getInteger(context, "ticks");
								
								for (final Entity e : EntityArgument.getEntities(context, "targets"))
								{
									final ScaleData data = ScaleTypes.BASE.getScaleData(e);
									
									data.setScaleTickDelay(ticks);
								}
								
								return 1;
							})
						)
						.executes(context ->
						{
							final int ticks = IntegerArgumentType.getInteger(context, "ticks");
							
							final ScaleData data = ScaleTypes.BASE.getScaleData(context.getSource().getEntityOrException());
							
							data.setScaleTickDelay(ticks);
							
							return 1;
						})
					)
				)
				.then(Commands.literal("get")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("entity", EntityArgument.entity())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final int ticks = type.getScaleData(EntityArgument.getEntity(context, "entity")).getScaleTickDelay();
								CommandUtils.sendFeedback(context.getSource(), () -> delayText(ticks), false);
								return 1;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final int ticks = type.getScaleData(context.getSource().getEntityOrException()).getScaleTickDelay();
							CommandUtils.sendFeedback(context.getSource(), () -> delayText(ticks), false);
							return 1;
						})
					)
					.then(Commands.argument("entity", EntityArgument.entity())
						.executes(context ->
						{
							final int ticks = ScaleTypes.BASE.getScaleData(EntityArgument.getEntity(context, "entity")).getScaleTickDelay();
							CommandUtils.sendFeedback(context.getSource(), () -> delayText(ticks), false);
							return 1;
						})
					)
					.executes(context ->
					{
						final int ticks = ScaleTypes.BASE.getScaleData(context.getSource().getEntityOrException()).getScaleTickDelay();
						CommandUtils.sendFeedback(context.getSource(), () -> delayText(ticks), false);
						return 1;
					})
				)
				.then(Commands.literal("reset")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("targets", EntityArgument.entities())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final int ticks = type.getDefaultTickDelay();
								
								for (final Entity e : EntityArgument.getEntities(context, "targets"))
								{
									final ScaleData data = type.getScaleData(e);
									
									data.setScaleTickDelay(ticks);
								}
								
								CommandUtils.sendFeedback(context.getSource(), () -> delayText(ticks), false);
								
								return 1;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final int ticks = type.getDefaultTickDelay();
							
							final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
							
							data.setScaleTickDelay(ticks);
							
							CommandUtils.sendFeedback(context.getSource(), () -> delayText(ticks), false);
							
							return 1;
						})
					)
					.then(Commands.argument("targets", EntityArgument.entities())
						.executes(context ->
						{
							final int ticks = ScaleTypes.BASE.getDefaultTickDelay();
							
							for (final Entity e : EntityArgument.getEntities(context, "targets"))
							{
								final ScaleData data = ScaleTypes.BASE.getScaleData(e);
								
								data.setScaleTickDelay(ticks);
							}
							
							CommandUtils.sendFeedback(context.getSource(), () -> delayText(ticks), false);
							
							return 1;
						})
					)
					.executes(context ->
					{
						final int ticks = ScaleTypes.BASE.getDefaultTickDelay();
						
						final ScaleData data = ScaleTypes.BASE.getScaleData(context.getSource().getEntityOrException());
						
						data.setScaleTickDelay(ticks);
						
						CommandUtils.sendFeedback(context.getSource(), () -> delayText(ticks), false);
						
						return 1;
					})
				)
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerEasing(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("easing")
				.then(Commands.literal("set")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("easing", ScaleEasingArgumentType.scaleEasing())
							.then(Commands.argument("targets", EntityArgument.entities())
								.executes(context ->
								{
									final Float2FloatFunction easing = ScaleEasingArgumentType.getScaleEasingArgument(context, "easing");
									final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
									
									for (final Entity e : EntityArgument.getEntities(context, "targets"))
									{
										final ScaleData data = type.getScaleData(e);
										
										data.setEasing(easing);
									}
									
									return 1;
								})
							)
							.executes(context ->
							{
								final Float2FloatFunction easing = ScaleEasingArgumentType.getScaleEasingArgument(context, "easing");
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								
								final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
								
								data.setEasing(easing);
								
								return 1;
							})
						)
					)
				)
				.then(Commands.literal("get")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("entity", EntityArgument.entity())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final Float2FloatFunction easing = type.getScaleData(EntityArgument.getEntity(context, "entity")).getEasing();
								CommandUtils.sendFeedback(context.getSource(), () -> easingText(easing, type), false);
								return 1;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final Float2FloatFunction easing = type.getScaleData(context.getSource().getEntityOrException()).getEasing();
							CommandUtils.sendFeedback(context.getSource(), () -> easingText(easing, type), false);
							return 1;
						})
					)
					.then(Commands.argument("entity", EntityArgument.entity())
						.executes(context ->
						{
							final Float2FloatFunction easing = ScaleTypes.BASE.getScaleData(EntityArgument.getEntity(context, "entity")).getEasing();
							CommandUtils.sendFeedback(context.getSource(), () -> easingText(easing, ScaleTypes.BASE), false);
							return 1;
						})
					)
					.executes(context ->
					{
						final Float2FloatFunction easing = ScaleTypes.BASE.getScaleData(context.getSource().getEntityOrException()).getEasing();
						CommandUtils.sendFeedback(context.getSource(), () -> easingText(easing, ScaleTypes.BASE), false);
						return 1;
					})
				)
				.then(Commands.literal("reset")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("targets", EntityArgument.entities())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								
								for (final Entity e : EntityArgument.getEntities(context, "targets"))
								{
									final ScaleData data = type.getScaleData(e);
									
									data.setEasing(null);
								}
								
								CommandUtils.sendFeedback(context.getSource(), () -> easingText(null, type), false);
								
								return 1;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							
							final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
							
							data.setEasing(null);
							
							CommandUtils.sendFeedback(context.getSource(), () -> easingText(null, type), false);
							
							return 1;
						})
					)
					.then(Commands.argument("targets", EntityArgument.entities())
						.executes(context ->
						{
							for (final Entity e : EntityArgument.getEntities(context, "targets"))
							{
								final ScaleData data = ScaleTypes.BASE.getScaleData(e);
								
								data.setEasing(null);
							}
							
							CommandUtils.sendFeedback(context.getSource(), () -> easingText(null, ScaleTypes.BASE), false);
							
							return 1;
						})
					)
					.executes(context ->
					{
						final ScaleData data = ScaleTypes.BASE.getScaleData(context.getSource().getEntityOrException());
						
						data.setEasing(null);
						
						CommandUtils.sendFeedback(context.getSource(), () -> easingText(null, ScaleTypes.BASE), false);
						
						return 1;
					})
				)
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerPersist(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("persist")
				.then(Commands.literal("set")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("enabled", BoolArgumentType.bool())
							.then(Commands.argument("targets", EntityArgument.entities())
								.executes(context ->
								{
									final boolean persist = BoolArgumentType.getBool(context, "enabled");
									final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
									
									for (final Entity e : EntityArgument.getEntities(context, "targets"))
									{
										final ScaleData data = type.getScaleData(e);
										
										data.setPersistence(persist);
									}
									
									return 1;
								})
							)
							.executes(context ->
							{
								final boolean persist = BoolArgumentType.getBool(context, "enabled");
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								
								final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
								
								data.setPersistence(persist);
								
								return 1;
							})
						)
					)
					.then(Commands.argument("enabled", BoolArgumentType.bool())
						.then(Commands.argument("targets", EntityArgument.entities())
							.executes(context ->
							{
								final boolean persist = BoolArgumentType.getBool(context, "enabled");
								
								for (final Entity e : EntityArgument.getEntities(context, "targets"))
								{
									for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
									{
										final ScaleData data = type.getScaleData(e);
										data.setPersistence(persist);
									}
								}
								
								return 1;
							})
						)
						.executes(context ->
						{
							final boolean persist = BoolArgumentType.getBool(context, "enabled");
							
							for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
							{
								final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
								data.setPersistence(persist);
							}
							
							return 1;
						})
					)
				)
				.then(Commands.literal("get")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("entity", EntityArgument.entity())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								final Boolean persist = type.getScaleData(EntityArgument.getEntity(context, "entity")).getPersistence();
								CommandUtils.sendFeedback(context.getSource(), () -> persistenceText(persist, type), false);
								return 1;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							final Boolean persist = type.getScaleData(context.getSource().getEntityOrException()).getPersistence();
							CommandUtils.sendFeedback(context.getSource(), () -> persistenceText(persist, type), false);
							return 1;
						})
					)
				)
				.then(Commands.literal("reset")
					.then(Commands.argument("scale_type", ScaleTypeArgumentType.scaleType())
						.then(Commands.argument("targets", EntityArgument.entities())
							.executes(context ->
							{
								final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
								
								for (final Entity e : EntityArgument.getEntities(context, "targets"))
								{
									final ScaleData data = type.getScaleData(e);
									
									data.setPersistence(null);
								}
								
								return 1;
							})
						)
						.executes(context ->
						{
							final ScaleType type = ScaleTypeArgumentType.getScaleTypeArgument(context, "scale_type");
							
							final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
							
							data.setPersistence(null);
							
							return 1;
						})
					)
					.then(Commands.argument("targets", EntityArgument.entities())
						.executes(context ->
						{
							for (final Entity e : EntityArgument.getEntities(context, "targets"))
							{
								for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
								{
									final ScaleData data = type.getScaleData(e);
									data.setPersistence(null);
								}
							}
							
							return 1;
						})
					)
					.executes(context ->
					{
						for (final ScaleType type : ScaleRegistries.SCALE_TYPES.values())
						{
							final ScaleData data = type.getScaleData(context.getSource().getEntityOrException());
							data.setPersistence(null);
						}
						
						return 1;
					})
				)
			);
		
		return builder;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> registerNbt(final LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder
			.then(Commands.literal("nbt")
				.then(Commands.literal("get")
					.then(Commands.argument("entity", EntityArgument.entity())
						.then(Commands.argument("path", NbtPathArgument.nbtPath())
							.executes(context ->
								DataCommandInvoker.Path.callGetData(
									context.getSource(),
									new EntityScaleDataObject(EntityArgument.getEntity(context, "entity")),
									NbtPathArgument.getPath(context, "path")
								)
							)
							.then(Commands.argument("scale", DoubleArgumentType.doubleArg())
								.executes(context ->
									DataCommandInvoker.Scaled.callGetNumeric(
										context.getSource(),
										new EntityScaleDataObject(EntityArgument.getEntity(context, "entity")),
										NbtPathArgument.getPath(context, "path"),
										DoubleArgumentType.getDouble(context, "scale")
									)
								)
							)
						)
						.executes(context ->
							DataCommandInvoker.Get.callGetData(
								context.getSource(),
								new EntityScaleDataObject(EntityArgument.getEntity(context, "entity"))
							)
						)
					)
					.executes(context ->
					{
						final EntityDataAccessor obj = new EntityScaleDataObject(context.getSource().getEntityOrException());
						
						final CompoundTag nbt = obj.getData();
						CommandUtils.sendFeedback(context.getSource(), () -> obj.getPrintSuccess(nbt), false);
						
						return nbt.size();
					})
				)
			);
		
		return builder;
	}
	
	private static class EntityScaleDataObject extends EntityDataAccessor
	{
		private final Entity entity;
		
		public EntityScaleDataObject(Entity entity)
		{
			super(entity);
			this.entity = entity;
		}
		
		@Override
		public void setData(CompoundTag nbt) throws CommandSyntaxException
		{
			((PehkuiEntityExtensions) entity).pehkui_readScaleNbt(nbt);
		}
		
		@Override
		public CompoundTag getData()
		{
			return ((PehkuiEntityExtensions) entity).pehkui_writeScaleNbt(new CompoundTag());
		}
	}
	
	private static Component scaleText(float scale)
	{
		final long denominator = (long) (1.0F / scale);
		if (((long) scale) != 1L && Float.compare(scale, 1.0F / denominator) == 0)
		{
			return I18nUtils.translate("commands.pehkui.scale.get.fraction.message", "Scale: %s (1/%s)", format(scale), format(denominator));
		}
		
		return I18nUtils.translate("commands.pehkui.scale.get.message", "Scale: %s", format(scale));
	}
	
	private static Component scaleText(float scale, int multiplied)
	{
		final long denominator = (long) (1.0F / scale);
		if (((long) scale) != 1L && Float.compare(scale, 1.0F / denominator) == 0)
		{
			return I18nUtils.translate("commands.pehkui.scale.get.fraction.factor.message", "Scale: %s (1/%s) | (%s)", format(scale), format(denominator), format(multiplied));
		}
		
		return I18nUtils.translate("commands.pehkui.scale.get.factor.message", "Scale: %s | (%s)", format(scale), format(multiplied));
	}
	
	private static Component modifierText(String modifierString)
	{
		return I18nUtils.translate("commands.pehkui.scale.modifier.get.message", "%s", modifierString.isEmpty() ? "N/A" : modifierString);
	}
	
	private static Component delayText(int ticks)
	{
		return I18nUtils.translate("commands.pehkui.scale.delay.get.message", "Delay: %s ticks", format(ticks));
	}
	
	private static Component persistenceText(@Nullable Boolean persist, ScaleType type)
	{
		final String unlocalized = "commands.pehkui.scale.persist." + (persist != null ? persist : ("default." + type.getDefaultPersistence()));
		final String message = "Persistent: " + (persist == null ? "default (" + type.getDefaultPersistence() + ")" : persist);
		return I18nUtils.translate(unlocalized, message);
	}
	
	private static Component easingText(@Nullable Float2FloatFunction easing, ScaleType type)
	{
		final String easingId = ScaleRegistries.getId(ScaleRegistries.SCALE_EASINGS, easing != null ? easing : type.getDefaultEasing()).toString();
		final String unlocalized = "commands.pehkui.scale.easing" + (easing != null ? "" : ".default");
		final String message = "Easing: " + (easing == null ? "default (" + easingId + ")" : easingId);
		return I18nUtils.translate(unlocalized, message, easingId);
	}
	
	private static final Random RANDOM = new Random();
	
	private static final DecimalFormat SCALE_FORMAT;
	
	static
	{
		SCALE_FORMAT = new DecimalFormat("#,##0");
		SCALE_FORMAT.setMaximumFractionDigits(340);
	}
	
	private static String format(int scale)
	{
		return SCALE_FORMAT.format(scale);
	}
	
	private static String format(long scale)
	{
		return SCALE_FORMAT.format(scale);
	}
	
	private static String format(float scale)
	{
		return SCALE_FORMAT.format(scale);
	}
}
