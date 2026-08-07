package virtuoel.pehkui.util;

import java.lang.invoke.MethodHandle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.PehkuiConfig;

public class ScaleRenderUtils
{
	// NeoForge 1.21.1 单版本 + Mojmap：无需跨版本 intermediary 反射，句柄恒为 null，相关方法直接走官方 API fallback
	public static final MethodHandle DRAW_BOX_OUTLINE = null, SHOULD_KEEP_PLAYER_ATTRIBUTES = null, HAS_EXTENDED_REACH = null, GET_TICK_DELTA = null;

	public static float getTickDelta(final Minecraft client)
	{
		if (GET_TICK_DELTA != null)
		{
			try
			{
				return (float) GET_TICK_DELTA.invoke(client);
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}

		return client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}

	public static boolean hasExtendedReach(final MultiPlayerGameMode interactionManager)
	{
		if (HAS_EXTENDED_REACH != null)
		{
			try
			{
				return (boolean) HAS_EXTENDED_REACH.invoke(interactionManager);
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}

		return interactionManager.getPlayerMode().isCreative();
	}

	public static boolean wasPlayerAlive(final ClientboundRespawnPacket packet)
	{
		if (VersionUtils.MINOR < 19 || (VersionUtils.MINOR == 19 && VersionUtils.PATCH <= 2))
		{
			if (SHOULD_KEEP_PLAYER_ATTRIBUTES != null)
			{
				try
				{
					return (boolean) SHOULD_KEEP_PLAYER_ATTRIBUTES.invoke(packet);
				}
				catch (Throwable e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		return packet.shouldKeep((byte) 1);
	}

	public static void renderInteractionBox(@Nullable final Object matrices, @Nullable final Object vertices, final AABB box)
	{
		renderInteractionBox(matrices, vertices, box, 0.25F, 1.0F, 0.0F, 1.0F);
	}

	public static void renderInteractionBox(@Nullable final Object matrices, @Nullable final Object vertices, final AABB box, final float red, final float green, final float blue, final float alpha)
	{
		if (VersionUtils.MINOR >= 15)
		{
			return;
		}
		else if (DRAW_BOX_OUTLINE != null)
		{
			try
			{
				DRAW_BOX_OUTLINE.invoke(box, red, green, blue, alpha);
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public static final float modifyProjectionMatrixDepthByWidth(float depth, @Nullable Entity entity, float tickDelta)
	{
		return entity == null ? depth : modifyProjectionMatrixDepth(ScaleUtils.getBoundingBoxWidthScale(entity, tickDelta), depth, entity, tickDelta);
	}

	public static final float modifyProjectionMatrixDepthByHeight(float depth, @Nullable Entity entity, float tickDelta)
	{
		return entity == null ? depth : modifyProjectionMatrixDepth(ScaleUtils.getEyeHeightScale(entity, tickDelta), depth, entity, tickDelta);
	}

	public static final float modifyProjectionMatrixDepth(float depth, @Nullable Entity entity, float tickDelta)
	{
		return entity == null ? depth : modifyProjectionMatrixDepth(Math.min(ScaleUtils.getBoundingBoxWidthScale(entity, tickDelta), ScaleUtils.getEyeHeightScale(entity, tickDelta)), depth, entity, tickDelta);
	}

	public static final float modifyProjectionMatrixDepth(float scale, float depth, Entity entity, float tickDelta)
	{
		if (scale < 1.0F)
		{
			return Math.max(depth * scale, (float) PehkuiConfig.CLIENT.minimumCameraDepth.get().doubleValue());
		}

		return depth;
	}

	public static boolean shouldSkipHeadItemScaling(@Nullable LivingEntity entity, ItemStack item, Object renderMode)
	{
		if ("HEAD".equals(((Enum<?>) renderMode).name()))
		{
			if (entity == null || (entity.getItemBySlot(EquipmentSlot.MAINHAND) != item && entity.getItemBySlot(EquipmentSlot.OFFHAND) != item))
			{
				return true;
			}
		}

		return false;
	}

	public static void logIfRenderCancelled()
	{
		logIfItemRenderCancelled(true);
		logIfEntityRenderCancelled(true);
	}

	private static final Set<Item> loggedItems = ConcurrentHashMap.newKeySet();
	private static ItemStack lastRenderedStack = null;
	private static int itemRecursionDepth = 0;
	private static int maxItemRecursionDepth = 2;

	public static void logIfItemRenderCancelled()
	{
		logIfItemRenderCancelled(false);
	}

	private static void logIfItemRenderCancelled(final boolean force)
	{
		if (lastRenderedStack != null && (force || itemRecursionDepth >= maxItemRecursionDepth))
		{
			final Item i = lastRenderedStack.getItem();
			if (force || !loggedItems.contains(i))
			{
				final String stackKey = lastRenderedStack.getItem().getDescriptionId();
				final String itemKey = lastRenderedStack.getItem().getDescriptionId();
				if (stackKey.equals(itemKey))
				{
					Pehkui.LOGGER.error("[{}]: Did something cancel item rendering early? Matrix stack was not popped after rendering item {} ({}).", Pehkui.MOD_ID, stackKey, lastRenderedStack.getItem());
				}
				else
				{
					Pehkui.LOGGER.error("[{}]: Did something cancel item rendering early? Matrix stack was not popped after rendering item {} ({}) ({})", Pehkui.MOD_ID, stackKey, itemKey, lastRenderedStack.getItem());
				}

				loggedItems.add(i);
			}
		}
	}

	public static void saveLastRenderedItem(final ItemStack currentStack)
	{
		if (itemRecursionDepth == 0)
		{
			lastRenderedStack = currentStack;
		}

		itemRecursionDepth++;
	}

	public static void clearLastRenderedItem()
	{
		lastRenderedStack = null;
		itemRecursionDepth = 0;
	}

	private static final Set<EntityType<?>> loggedEntityTypes = ConcurrentHashMap.newKeySet();
	private static EntityType<?> lastRenderedEntity = null;
	private static int entityRecursionDepth = 0;
	private static int maxEntityRecursionDepth = 2;

	public static void logIfEntityRenderCancelled()
	{
		logIfEntityRenderCancelled(false);
	}

	private static void logIfEntityRenderCancelled(final boolean force)
	{
		if (lastRenderedEntity != null && (force || entityRecursionDepth >= maxEntityRecursionDepth))
		{
			if (force || !loggedEntityTypes.contains(lastRenderedEntity))
			{
				final Identifier id = EntityType.getKey(lastRenderedEntity);

				Pehkui.LOGGER.error("[{}]: Did something cancel entity rendering early? Matrix stack was not popped after rendering entity {}.", Pehkui.MOD_ID, id);

				loggedEntityTypes.add(lastRenderedEntity);
			}
		}
	}

	public static void saveLastRenderedEntity(final EntityType<?> type)
	{
		if (entityRecursionDepth == 0)
		{
			lastRenderedEntity = type;
		}

		entityRecursionDepth++;
	}

	public static void clearLastRenderedEntity()
	{
		lastRenderedEntity = null;
		entityRecursionDepth = 0;
	}

	public static void addDetailsToCrashReport(CrashReportCategory section)
	{
		if (lastRenderedStack != null)
		{
			section.setDetail("pehkui:debug/render/item", lastRenderedStack.getItem().getDescriptionId());
		}

		if (lastRenderedEntity != null)
		{
			final Identifier id = EntityType.getKey(lastRenderedEntity);

			section.setDetail("pehkui:debug/render/entity", id);
		}
	}
}
