package virtuoel.pehkui.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Leashable;

public final class ReflectionUtils
{
	public static Identifier constructIdentifier(final String id)
	{
		return Identifier.parse(id);
	}

	public static Identifier constructIdentifier(final String namespace, final String path)
	{
		return Identifier.fromNamespaceAndPath(namespace, path);
	}

	public static @Nullable Entity getHoldingEntity(final Entity leashed)
	{
		if (leashed instanceof Leashable)
		{
			return ((Leashable) leashed).getLeashHolder();
		}

		return null;
	}

	public static float getDimensionsWidth(final EntityDimensions dimensions)
	{
		return dimensions.width();
	}

	public static float getDimensionsHeight(final EntityDimensions dimensions)
	{
		return dimensions.height();
	}

	public static void setOnGround(final Entity entity, final boolean onGround)
	{
		entity.setOnGround(onGround);
	}

	public static void sendPacket(final ServerGamePacketListenerImpl handler, final Packet<?> packet)
	{
		handler.send(packet);
	}

	public static boolean isDummy(final MinMaxBounds<?> range)
	{
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
