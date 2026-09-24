package virtuoel.pehkui.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import virtuoel.kanos_config.api.MutableConfigEntry;
import virtuoel.pehkui.api.PehkuiConfig;
import virtuoel.pehkui.util.ConfigSyncUtils;

/**
 * ModMenu config screen editing the existing KanosConfig-backed entries
 * (config/pehkui/config.json). Entries are grouped into three tabs:
 * Common, Scale Types (per-type min/max) and Client. Synced entries are
 * greyed out because their value is overwritten by the server on join.
 */
public class PehkuiConfigScreen extends Screen
{
	private static final int ROWS_PER_PAGE = 7;

	private final Screen parent;

	private String tab = "common";
	private int page = 0;
	private final List<FieldRow> rows = new ArrayList<>();

	public PehkuiConfigScreen(Screen parent)
	{
		super(Component.literal("Pehkui Config"));
		this.parent = parent;
	}

	@Override
	protected void init()
	{
		rows.clear();
		clearWidgets();

		addRenderableWidget(Button.builder(Component.literal("Common"), b -> setTab("common")).size(70, 20).pos(10, 10).build());
		addRenderableWidget(Button.builder(Component.literal("Scale Types"), b -> setTab("scaletypes")).size(95, 20).pos(85, 10).build());
		addRenderableWidget(Button.builder(Component.literal("Client"), b -> setTab("client")).size(70, 20).pos(185, 10).build());

		final List<Map.Entry<String, MutableConfigEntry<?>>> tabEntries = getTabEntries(tab);
		final int totalPages = Math.max(1, (tabEntries.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
		page = Math.min(page, totalPages - 1);

		final int start = page * ROWS_PER_PAGE;
		final int end = Math.min(start + ROWS_PER_PAGE, tabEntries.size());

		int y = 40;
		for (int i = start; i < end; i++)
		{
			final Map.Entry<String, MutableConfigEntry<?>> entry = tabEntries.get(i);
			final FieldRow row = new FieldRow(entry.getKey(), entry.getValue());
			row.addWidgets(this, 12, y);
			rows.add(row);
			y += 26;
		}

		if (tabEntries.size() > ROWS_PER_PAGE)
		{
			addRenderableWidget(Button.builder(Component.literal("<"), b -> { if (page > 0) { page--; init(); } }).size(24, 20).pos(10, height - 30).build());
			addRenderableWidget(Button.builder(Component.literal(">"), b -> { if (page < totalPages - 1) { page++; init(); } }).size(24, 20).pos(38, height - 30).build());
			addRenderableWidget(Button.builder(Component.literal((page + 1) + "/" + totalPages), b -> {}).size(48, 20).pos(66, height - 30).build());
		}

		addRenderableWidget(Button.builder(Component.literal("Save & Close"), b -> { save(); onClose(); }).size(90, 20).pos(width - 190, height - 30).build());
		addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).size(70, 20).pos(width - 90, height - 30).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
	}

	private void setTab(String newTab)
	{
		tab = newTab;
		page = 0;
		init();
	}

	private static boolean isEditable()
	{
		return Minecraft.getInstance().getCurrentServer() == null;
	}

	private void save()
	{
		final boolean editable = isEditable();

		for (final FieldRow row : rows)
		{
			if (!row.synced || editable)
			{
				row.apply();
			}
		}

		PehkuiConfig.BUILDER.config.save();

		final IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();

		if (server != null)
		{
			ConfigSyncUtils.syncConfigs(server.getPlayerList().getPlayers());
		}
	}

	private List<Map.Entry<String, MutableConfigEntry<?>>> getTabEntries(String tab)
	{
		final List<Map.Entry<String, MutableConfigEntry<?>>> result = new ArrayList<>();

		for (final Map.Entry<String, MutableConfigEntry<?>> entry : ConfigSyncUtils.CONFIGS.entrySet())
		{
			final String name = entry.getKey();
			final boolean scaleTypeField = name.endsWith(".minimum") || name.endsWith(".maximum");

			if ("common".equals(tab) && !scaleTypeField && !"minimumCameraDepth".equals(name))
			{
				result.add(entry);
			}
			else if ("scaletypes".equals(tab) && scaleTypeField)
			{
				result.add(entry);
			}
			else if ("client".equals(tab) && "minimumCameraDepth".equals(name))
			{
				result.add(entry);
			}
		}

		return result;
	}

	@Override
	public void onClose()
	{
		Minecraft.getInstance().setScreenAndShow(parent);
	}

	private static final class FieldRow
	{
		private final String name;
		private final String translationKey;
		private final MutableConfigEntry<?> entry;
		private final boolean synced;

		private Checkbox checkbox = null;
		private EditBox editBox = null;

		private FieldRow(String name, MutableConfigEntry<?> entry)
		{
			this.name = name;
			this.entry = entry;
			this.synced = ConfigSyncUtils.isSyncedConfig(name);
			this.translationKey = (name.endsWith(".minimum") || name.endsWith(".maximum"))
				? "pehkui.configgui.scale_limits." + name
				: "pehkui.configgui." + name;
		}

		private void addWidgets(PehkuiConfigScreen screen, int x, int y)
		{
			final Object value = entry.getValue();
			final boolean editable = PehkuiConfigScreen.isEditable();
			final boolean enabled = !synced || editable;
			final Component description = Component.translatable(translationKey);
			final Component label = synced && !editable
				? description.copy().append(" [server]")
				: description;
			final Tooltip tooltip = Tooltip.create(description);

			if (value instanceof Boolean)
			{
				checkbox = Checkbox.builder(label, screen.font).pos(x, y).selected((Boolean) value).tooltip(tooltip).build();
				checkbox.active = enabled;
				screen.addRenderableWidget(checkbox);
			}
			else
			{
				final Button nameButton = Button.builder(label, b -> {}).size(160, 20).pos(x, y).tooltip(tooltip).build();
				nameButton.active = enabled;
				screen.addRenderableWidget(nameButton);

				editBox = new EditBox(screen.font, x + 168, y, screen.width - x - 180, 20, Component.literal(name));
				editBox.setValue(value == null ? "" : String.valueOf(value));
				editBox.setEditable(enabled);
				screen.addRenderableWidget(editBox);
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void apply()
		{
			if (synced && !PehkuiConfigScreen.isEditable())
			{
				return;
			}

			if (checkbox != null)
			{
				((MutableConfigEntry) entry).setValue(checkbox.selected());
			}
			else if (editBox != null)
			{
				final Object value = entry.getValue();
				final String text = editBox.getValue().trim();

				if (value instanceof Number)
				{
					try
					{
						if (value instanceof Double)
						{
							((MutableConfigEntry) entry).setValue(Double.parseDouble(text));
						}
						else if (value instanceof Integer)
						{
							((MutableConfigEntry) entry).setValue(Integer.parseInt(text));
						}
					}
					catch (NumberFormatException e)
					{
						// ignore invalid input, keep previous value
					}
				}
				else if (value instanceof List)
				{
					final List<String> list = new ArrayList<>();
					for (final String part : text.split(","))
					{
						if (!part.isEmpty())
						{
							list.add(part.trim());
						}
					}
					((MutableConfigEntry) entry).setValue(list);
				}
				else if (value instanceof String)
				{
					((MutableConfigEntry) entry).setValue(text);
				}
			}
		}
	}
}
