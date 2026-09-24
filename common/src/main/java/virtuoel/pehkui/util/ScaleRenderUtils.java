package virtuoel.pehkui.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import virtuoel.pehkui.Pehkui;
import virtuoel.pehkui.api.PehkuiConfig;

public class ScaleRenderUtils
{
	public static float getTickDelta(final Minecraft client)
	{
		return client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
	}

	public static boolean wasPlayerAlive(final ClientboundRespawnPacket packet)
	{
		return packet.shouldKeep((byte) 1);
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
				final String stackKey = lastRenderedStack.getItemName().getString();
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
