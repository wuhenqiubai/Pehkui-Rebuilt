package virtuoel.pehkui.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import virtuoel.pehkui.Pehkui;

public final class ReflectionUtils
{
	// NeoForge 1.21.1 单版本 + Mojmap：无需跨版本 intermediary 反射映射，句柄恒为 null，相关方法直接走官方 API fallback
	public static final Class<?> LITERAL_TEXT = null;
	public static final MethodHandle GET_FLYING_SPEED = null, SET_FLYING_SPEED = null, GET_MOUNTED_HEIGHT_OFFSET = null, SEND_PACKET = null, IS_DUMMY = null, GET_WIDTH = null, GET_HEIGHT = null, GET_HOLDING_ENTITY = null, CONSTRUCT_ID_FROM_STRING = null, CONSTRUCT_ID_FROM_STRINGS = null;

	public static ResourceLocation constructIdentifier(final String id)
	{
		try
		{
			return (ResourceLocation) CONSTRUCT_ID_FROM_STRING.invoke(id);
		}
		catch (final ResourceLocationException e)
		{
			throw e;
		}
		catch (final Throwable e)
		{
			// CONSTRUCT_ID_FROM_STRING 恒为 null，走官方 API
		}

		return ResourceLocation.parse(id);
	}

	public static ResourceLocation constructIdentifier(final String namespace, final String path)
	{
		try
		{
			return (ResourceLocation) CONSTRUCT_ID_FROM_STRINGS.invoke(namespace, path);
		}
		catch (final ResourceLocationException e)
		{
			throw e;
		}
		catch (final Throwable e)
		{
			// CONSTRUCT_ID_FROM_STRINGS 恒为 null，走官方 API
		}

		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}

	public static @Nullable Entity getHoldingEntity(final Entity leashed)
	{
		if (GET_HOLDING_ENTITY != null)
		{
			if (leashed instanceof Mob)
			{
				try
				{
					return (Entity) GET_HOLDING_ENTITY.invoke((Mob) leashed);
				}
				catch (final Throwable e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		else
		{
			if (leashed instanceof Leashable)
			{
				return ((Leashable) leashed).getLeashHolder();
			}
		}

		return null;
	}

	public static float getFlyingSpeed(final LivingEntity entity)
	{
		try
		{
			return (float) GET_FLYING_SPEED.invoke(entity);
		}
		catch (final Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void setFlyingSpeed(final LivingEntity entity, final float speed)
	{
		try
		{
			 SET_FLYING_SPEED.invoke(entity, speed);
		}
		catch (final Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public static double getMountedHeightOffset(final Entity entity)
	{
		if (GET_MOUNTED_HEIGHT_OFFSET != null)
		{
			try
			{
				return (double) GET_MOUNTED_HEIGHT_OFFSET.invoke(entity);
			}
			catch (final Throwable e)
			{
				throw new RuntimeException(e);
			}
		}

		return getDimensionsHeight(entity.getDimensions(entity.getPose())) * 0.75;
	}

	public static float getDimensionsWidth(final EntityDimensions dimensions)
	{
		if (GET_WIDTH != null)
		{
			try
			{
				return (float) GET_WIDTH.invoke(dimensions);
			}
			catch (final Throwable e)
			{
				throw new RuntimeException(e);
			}
		}

		return dimensions.width();
	}

	public static float getDimensionsHeight(final EntityDimensions dimensions)
	{
		if (GET_HEIGHT != null)
		{
			try
			{
				return (float) GET_HEIGHT.invoke(dimensions);
			}
			catch (final Throwable e)
			{
				throw new RuntimeException(e);
			}
		}

		return dimensions.height();
	}

	public static void setOnGround(final Entity entity, final boolean onGround)
	{
		if (VersionUtils.MINOR >= 16)
		{
			entity.setOnGround(onGround);
		}
		else
		{
			final PehkuiEntityExtensions e = (PehkuiEntityExtensions) entity;
			e.pehkui_setOnGround(onGround);
		}
	}

	public static void sendPacket(final ServerGamePacketListenerImpl handler, final Packet<?> packet)
	{
		if (SEND_PACKET != null)
		{
			try
			{
				if (VersionUtils.MINOR <= 16)
				{
					SEND_PACKET.invoke(handler, packet);
				}
				else
				{
					SEND_PACKET.invoke((ServerPlayerConnection) (Object) handler, packet);
				}
			}
			catch (final Throwable e)
			{
				throw new RuntimeException(e);
			}

			return;
		}

		handler.send(packet);
	}

	public static boolean isDummy(final MinMaxBounds<?> range)
	{
		if (IS_DUMMY != null)
		{
			try
			{
				return (boolean) IS_DUMMY.invoke(range);
			}
			catch (final Throwable e)
			{
				throw new RuntimeException(e);
			}
		}

		return range.isAny();
	}

	public static Optional<Field> getField(final Optional<Class<?>> classObj, final String fieldName)
	{
		return classObj.map(c ->
		{
			try
			{
				final Field f = c.getDeclaredField(fieldName);
				f.setAccessible(true);
				return f;
			}
			catch (SecurityException | NoSuchFieldException e)
			{

			}
			return null;
		});
	}

	public static void setField(final Optional<Class<?>> classObj, final String fieldName, Object object, Object value)
	{
		ReflectionUtils.getField(classObj, fieldName).ifPresent(f ->
		{
			try
			{
				f.set(object, value);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{

			}
		});
	}

	public static Optional<Method> getMethod(final Optional<Class<?>> classObj, final String methodName, Class<?>... args)
	{
		return classObj.map(c ->
		{
			try
			{
				final Method m = c.getMethod(methodName, args);
				m.setAccessible(true);
				return m;
			}
			catch (SecurityException | NoSuchMethodException e)
			{

			}
			return null;
		});
	}

	public static <T> Optional<Constructor<T>> getConstructor(final Optional<Class<T>> clazz, final Class<?>... params)
	{
		return clazz.map(c ->
		{
			try
			{
				return c.getConstructor(params);
			}
			catch (NoSuchMethodException | SecurityException e)
			{
				return null;
			}
		});
	}

	public static Optional<Class<?>> getClass(final String className, final String... classNames)
	{
		Optional<Class<?>> ret = getClass(className);

		for (final String name : classNames)
		{
			if (ret.isPresent())
			{
				return ret;
			}

			ret = getClass(name);
		}

		return ret;
	}

	public static Optional<Class<?>> getClass(final String className)
	{
		try
		{
			return Optional.of(Class.forName(className));
		}
		catch (ClassNotFoundException e)
		{

		}

		return Optional.empty();
	}

	private ReflectionUtils()
	{

	}
}
