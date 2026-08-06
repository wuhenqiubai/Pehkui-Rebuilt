package virtuoel.pehkui.server.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.spongepowered.asm.mixin.MixinEnvironment;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.network.DebugPacket;
import virtuoel.pehkui.network.DebugPayload;
import virtuoel.pehkui.util.CommandUtils;
import virtuoel.pehkui.util.ConfigSyncUtils;
import virtuoel.pehkui.util.I18nUtils;
import virtuoel.pehkui.util.ReflectionUtils;
import virtuoel.pehkui.util.VersionUtils;

public class DebugCommand
{
	public static void register(final CommandDispatcher<CommandSourceStack> commandDispatcher)
	{
		final LiteralArgumentBuilder<CommandSourceStack> builder =
			Commands.literal("scale")
			.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
		
		builder.then(Commands.literal("debug")
			.then(ConfigSyncUtils.registerConfigCommands())
		);
		
		if (FabricLoader.getInstance().isDevelopmentEnvironment() || PehkuiConfig.COMMON.enableCommands.get())
		{
			builder
				.then(Commands.literal("debug")
					.then(Commands.literal("delete_scale_data")
						.then(Commands.literal("uuid")
							.then(Commands.argument("uuid", StringArgumentType.string())
								.executes(context ->
								{
									final String uuidString = StringArgumentType.getString(context, "uuid");
									
									try
									{
										MARKED_UUIDS.add(UUID.fromString(uuidString));
									}
									catch (IllegalArgumentException e)
									{
										context.getSource().sendFailure(I18nUtils.translate("commands.pehkui.debug.delete.uuid.invalid", "Invalid UUID \"%s\".", uuidString));
										return 0;
									}
									
									return 1;
								})
							)
						)
						.then(Commands.literal("username")
							.then(Commands.argument("username", StringArgumentType.string())
								.executes(context ->
								{
									MARKED_USERNAMES.add(StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT));
									
									return 1;
								})
							)
						)
					)
					.then(Commands.literal("garbage_collect")
						.executes(context ->
						{
							final Packet<?> packet;
							
							packet = ServerPlayNetworking.createClientboundPacket((CustomPacketPayload) (Object) new DebugPayload(PacketType.GARBAGE_COLLECT));
							
							ReflectionUtils.sendPacket(context.getSource().getPlayerOrException().connection, packet);
							
							System.gc();
							
							return 1;
						})
					)
				);
		}
		
		if (FabricLoader.getInstance().isDevelopmentEnvironment() || PehkuiConfig.COMMON.enableDebugCommands.get())
		{
			builder
				.then(Commands.literal("debug")
					.then(Commands.literal("run_mixin_tests")
						.executes(DebugCommand::runMixinTests)
					)
					.then(Commands.literal("run_tests")
						.executes(DebugCommand::runTests)
					)
				);
		}
		
		commandDispatcher.register(builder);
	}
	
	private static final Collection<UUID> MARKED_UUIDS = new HashSet<>();
	private static final Collection<String> MARKED_USERNAMES = new HashSet<>();
	
	public static boolean unmarkEntityForScaleReset(final Entity entity, final CompoundTag nbt)
	{
		if (entity instanceof Player && MARKED_USERNAMES.remove(((Player) entity).getName().getString().toLowerCase(Locale.ROOT)))
		{
			return true;
		}
		
		return nbt.getIntArray("UUID").map(DebugCommand::uuidFromIntArray).map(MARKED_UUIDS::remove).orElse(false);
	}
	
	private static UUID uuidFromIntArray(int[] values)
	{
		return new UUID(((long) values[0] << 32) | (values[1] & 0xFFFFFFFFL), ((long) values[2] << 32) | (values[3] & 0xFFFFFFFFL));
	}
	
	private static final List<EntityType<? extends Entity>> TYPES = Arrays.asList(
		EntityType.ZOMBIE,
		EntityType.CREEPER,
		EntityType.END_CRYSTAL,
		EntityType.BLAZE
	);
	
	private static int runTests(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		Entity entity = context.getSource().getEntityOrException();
		
		Direction dir = entity.getDirection();
		Direction opposite = dir.getOpposite();
		
		Direction left = dir.getCounterClockWise();
		Direction right = dir.getClockWise();
		
		int distance = 4;
		int spacing = 2;
		
		int width = ((TYPES.size() - 1) * (spacing + 1)) + 1;
		
		Vec3 pos = entity.position();
		BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos(pos.x, pos.y, pos.z).move(dir, distance).move(left, width / 2);
		
		Level w = entity.level();
		
		for (EntityType<?> t : TYPES)
		{
			w.setBlockAndUpdate(mut, Blocks.POLISHED_ANDESITE.defaultBlockState());
			final Entity e = t.create(w, EntitySpawnReason.COMMAND);
			
			e.absSnapTo(mut.getX() + 0.5, mut.getY() + 1, mut.getZ() + 0.5, opposite.toYRot(), 0);
			e.snapTo(mut.getX() + 0.5, mut.getY() + 1, mut.getZ() + 0.5, opposite.toYRot(), 0);
			e.setYHeadRot(opposite.toYRot());
			
			e.addTag("pehkui");
			
			w.addFreshEntity(e);
			
			mut.move(right, spacing + 1);
		}
		
		// TODO set command block w/ entity to void and block destroy under player pos
		
		int successes = -1;
		int total = -1;
		
		CommandUtils.sendFeedback(context.getSource(), () -> I18nUtils.translate("commands.pehkui.debug.test.success", "Tests succeeded: %d/%d", successes, total), false);
		
		return 1;
	}
	
	public static enum PacketType
	{
		MIXIN_AUDIT,
		GARBAGE_COLLECT
		;
	}
	
	private static int runMixinTests(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		final Entity executor = context.getSource().getEntity();
		if (executor instanceof ServerPlayer)
		{
			final Packet<?> packet;
			
			packet = ServerPlayNetworking.createClientboundPacket((CustomPacketPayload) (Object) new DebugPayload(PacketType.MIXIN_AUDIT));
			
			ReflectionUtils.sendPacket(((ServerPlayer) executor).connection, packet);
		}
		
		CommandUtils.sendFeedback(context.getSource(), () -> I18nUtils.translate("commands.pehkui.debug.audit.start", "Starting Mixin environment audit..."), false);
		MixinEnvironment.getCurrentEnvironment().audit();
		CommandUtils.sendFeedback(context.getSource(), () -> I18nUtils.translate("commands.pehkui.debug.audit.end", "Mixin environment audit complete!"), false);
		
		return 1;
	}
}
