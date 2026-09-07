package virtuoel.pehkui.util;

import java.nio.file.Path;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * 平台抽象：把 fabric / neoforge 的平台差异隔离于此，使 {@code common} 模块不直接引用任何
 * 平台 API（net.fabricmc.* / net.neoforged.*），只依赖本抽象类。
 *
 * 各平台在入口装配时通过 {@link #setInstance} 注入自己的实现。
 */
public abstract class Platform
{
	public static volatile Platform INSTANCE = new Unsupported();

	/**
	 * 判断某个 mod 是否已安装。
	 */
	public abstract boolean isModLoaded(String modId);

	/**
	 * config 根目录（fabric: FabricLoader.getConfigDir；neoforge: FMLPaths.CONFIGDIR）。
	 */
	public abstract Path getConfigDir();

	/**
	 * 是否运行在开发环境（fabric: isDevelopmentEnvironment；neoforge: 非生产）。
	 */
	public abstract boolean isDevelopmentEnvironment();

	/**
	 * 当前 Minecraft 版本字符串（如 "26.2"）。
	 */
	public abstract String getMinecraftVersion();

	/**
	 * 把一个自定义 payload 包装成服务端发放的 packet（fabric: ServerPlayNetworking.createClientboundPacket；neoforge: new ClientboundCustomPayloadPacket）。
	 */
	public abstract Packet<?> createClientboundPacket(CustomPacketPayload payload);

	public static void setInstance(Platform platform)
	{
		INSTANCE = platform;
	}

	/** 默认 unloaded 占位实现（未设置时兜底，避免 NPE）。 */
	public static final class Unsupported extends Platform
	{
		@Override
		public boolean isModLoaded(String modId)
		{
			return false;
		}

		@Override
		public Path getConfigDir()
		{
			return null;
		}

		@Override
		public boolean isDevelopmentEnvironment()
		{
			return false;
		}

		@Override
		public String getMinecraftVersion()
		{
			return "";
		}

		@Override
		public Packet<?> createClientboundPacket(CustomPacketPayload payload)
		{
			return null;
		}
	}
}
