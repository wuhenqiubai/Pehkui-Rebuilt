package virtuoel.pehkui.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
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
	public static final Class<?> LITERAL_TEXT;
	public static final MethodHandle GET_FLYING_SPEED, SET_FLYING_SPEED, GET_MOUNTED_HEIGHT_OFFSET, SEND_PACKET, IS_DUMMY, GET_WIDTH, GET_HEIGHT, CREATE_S2C_PACKET, GET_HOLDING_ENTITY, CONSTRUCT_ID_FROM_STRING, CONSTRUCT_ID_FROM_STRINGS;
	
	static
	{
		final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
		final Int2ObjectMap<MethodHandle> h = new Int2ObjectArrayMap<>();
		
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		String mapped = "unset";
		Class<?>[] c = new Class<?>[1];
		Method m;
		Field f;
		
		try
		{
			final boolean is117Plus = VersionUtils.MINOR >= 17;
			final boolean is118Minus = VersionUtils.MINOR <= 18;
			final boolean is1193Minus = VersionUtils.MINOR < 19 || (VersionUtils.MINOR == 19 && VersionUtils.PATCH <= 3);
			final boolean is1201Minus = VersionUtils.MINOR < 20 || (VersionUtils.MINOR == 20 && VersionUtils.PATCH <= 1);
			final boolean is1204Minus = VersionUtils.MINOR < 20 || (VersionUtils.MINOR == 20 && VersionUtils.PATCH <= 4);
			final boolean is1206Minus = VersionUtils.MINOR < 20 || (VersionUtils.MINOR == 20 && VersionUtils.PATCH <= 6);
			
			if (is118Minus)
			{
				mapped = mappingResolver.mapClassName("intermediary", "net.minecraft.class_2585");
				c[0] = Class.forName(mapped);
			}
			
			if (is1193Minus)
			{
				mapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_1309", "field_6281", "F");
				f = LivingEntity.class.getField(mapped);
				f.setAccessible(true);
				h.put(0, lookup.unreflectGetter(f));
				h.put(1, lookup.unreflectSetter(f));
			}
			
			if (is1201Minus)
			{
				mapped = mappingResolver.mapMethodName("intermediary", "net.minecraft.class_1297", "method_5621", "()D");
				m = Entity.class.getMethod(mapped);
				h.put(2, lookup.unreflect(m));
				
				mapped = mappingResolver.mapMethodName("intermediary", is117Plus ? "net.minecraft.class_5629" : "net.minecraft.class_3244", "method_14364", "(Lnet/minecraft/class_2596;)V");
				m = (is117Plus ? ServerPlayerConnection.class : ServerGamePacketListenerImpl.class).getMethod(mapped, Packet.class);
				h.put(3, lookup.unreflect(m));
				
				mapped = mappingResolver.mapMethodName("intermediary", "net.minecraft.class_2096", "method_9041", "()Z");
				m = MinMaxBounds.class.getMethod(mapped);
				h.put(4, lookup.unreflect(m));
			}
			
			if (is1204Minus)
			{
				mapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_4048", "field_18067", "F");
				f = EntityDimensions.class.getField(mapped);
				f.setAccessible(true);
				h.put(5, lookup.unreflectGetter(f));
				
				mapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_4048", "field_18068", "F");
				f = EntityDimensions.class.getField(mapped);
				f.setAccessible(true);
				h.put(6, lookup.unreflectGetter(f));
			}
			
			if (is1204Minus && ModLoaderUtils.isModLoaded("fabric-networking-api-v1"))
			{
				m = ServerPlayNetworking.class.getMethod("createS2CPacket", ResourceLocation.class, FriendlyByteBuf.class);
				h.put(7, lookup.unreflect(m));
			}
			
			if (is1206Minus)
			{
				mapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_1308", "method_5933", "()Lnet/minecraft/class_1297;");
				m = Mob.class.getMethod(mapped);
				h.put(8, lookup.unreflect(m));
				
				h.put(9, lookup.unreflectConstructor(ResourceLocation.class.getDeclaredConstructor(String.class)));
				
				h.put(10, lookup.unreflectConstructor(ResourceLocation.class.getDeclaredConstructor(String.class, String.class)));
			}
		}
		catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | NoSuchFieldException e)
		{
			Pehkui.LOGGER.error("Current name lookup: {}", mapped);
			Pehkui.LOGGER.catching(e);
		}
		
		LITERAL_TEXT = c[0];
		GET_FLYING_SPEED = h.get(0);
		SET_FLYING_SPEED = h.get(1);
		GET_MOUNTED_HEIGHT_OFFSET = h.get(2);
		SEND_PACKET = h.get(3);
		IS_DUMMY = h.get(4);
		GET_WIDTH = h.get(5);
		GET_HEIGHT = h.get(6);
		CREATE_S2C_PACKET = h.get(7);
		GET_HOLDING_ENTITY = h.get(8);
		CONSTRUCT_ID_FROM_STRING = h.get(9);
		CONSTRUCT_ID_FROM_STRINGS = h.get(10);
	}
	
	public static Packet<ClientCommonPacketListener> createS2CPacket(ResourceLocation channelName, FriendlyByteBuf buf)
	{
		try
		{
			return (Packet<ClientCommonPacketListener>) CREATE_S2C_PACKET.invoke(channelName, buf);
		}
		catch (final Throwable e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static ResourceLocation constructIdentifier(final String id)
	{
		if (CONSTRUCT_ID_FROM_STRING != null)
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
				throw new RuntimeException(e);
			}
		}
		
		return ResourceLocation.parse(id);
	}
	
	public static ResourceLocation constructIdentifier(final String namespace, final String path)
	{
		if (CONSTRUCT_ID_FROM_STRINGS != null)
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
				throw new RuntimeException(e);
			}
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
