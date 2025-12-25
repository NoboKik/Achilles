package template.rip.module.setting.settings;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.type.ImString;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.util.Identifier;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.GuiUtils;
import template.rip.api.util.KeyUtils;
import template.rip.api.util.RenderUtils;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.module.Module;
import template.rip.module.setting.Setting;
import template.rip.module.setting.SettingType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static template.rip.Template.moduleManager;

public class RegistrySetting<E> extends Setting {

	public static Predicate<Item> scaffoldPredicate = e -> e != Items.AIR && e instanceof BlockItem blo && blo.getBlock().getDefaultState().isSolid() && !blo.getBlock().getDefaultState().hasBlockEntity();
	public static Predicate<Item> autoHitCrystalPredicate = e -> true;

	private final ImString currentString = new ImString();
	public List<E> selected;
	public final List<E> defaultSelected;
	public final DefaultedRegistry<E> registry;
	public final Predicate<E> filter;
	private ArrayList<E> notSelected;

	public RegistrySetting(List<E> selected, Module parent, Description description, DefaultedRegistry<E> registry, Predicate<E> filter, String... name) {
		super(description, name);
	    this.parent = parent;
		this.registry = registry;
	    this.selected = new ArrayList<>(selected);
		this.defaultSelected = new ArrayList<>(selected);
		this.filter = filter;

		parent.addSettings(this);
	}

	public RegistrySetting(List<E> selected, Module parent, DefaultedRegistry<E> registry, String... name) {
		this(selected, parent, Description.of(), registry, e -> (e instanceof Item i ? i != Items.AIR : (!(e instanceof Block b) || !b.getDefaultState().isAir())), name);
	}

	public RegistrySetting(List<E> selected, Module parent, DefaultedRegistry<E> registry, Predicate<E> filter, String... name) {
		this(selected, parent, Description.of(), registry, filter, name);
	}

	@Override
	public float getHeight() {
		float currentHeight = 26f;

		if (notSelected != null) {
			currentHeight += 20f * notSelected.size();
		}

		currentHeight += 20f * selected.size();

		return currentHeight;
	}

	public String getTranslationKey(Object e) {
		if (e instanceof Item item) {
			return item.getTranslationKey();
		}
		if (e instanceof Block block) {
			return block.getTranslationKey();
		}
		if (e instanceof EntityType<?> entity) {
			return entity.getTranslationKey();
		}
		return "null";
	}

	public List<String> ids() {
		ArrayList<String> selectedIds = new ArrayList<>();
		ArrayList<E> sel = new ArrayList<>(selected);
		for (E e : sel) {
			selectedIds.add(registry.getId(e).toString());
		}
		return selectedIds;
	}

	public List<String> defaultIds() {
		ArrayList<String> selectedIds = new ArrayList<>();
		for (E e : defaultSelected) {
			selectedIds.add(registry.getId(e).toString());
		}
		return selectedIds;
	}

	public List<String> registryToId() {
		return registry.getIds().stream().map(Identifier::toString).collect(Collectors.toList());
	}

	public ArrayList<String> loadStrings(List<String> stringList) {
		List<E> copy = new ArrayList<>();
		ArrayList<String> failed = new ArrayList<>();
		for (String str : stringList) {
			E e = registry.get(Identifier.of(str));
			if (e != null) {//this is a lie
				copy.add(e);
			} else {
				failed.add(str);
			}
        }
		selected = new ArrayList<>(copy);
		return failed;
	}

	@Override
	public SettingType getType() {
		return SettingType.Registry;
	}

	public void loadIndexes(List<Integer> indexList) {
		List<E> copy = new ArrayList<>();
		for (Integer index : indexList) {
			E e = registry.get(index);
			copy.add(e);
		}
		selected = new ArrayList<>(copy);
	}

	@Override
	public void reset() {
		this.selected = new ArrayList<>(this.defaultSelected);
	}

	@Override
	public void render() {
		float[] c = JColor.getGuiColor().getFloatColor();

		ImGui.pushID(String.format("%s/%s", parent.getName(), this.getName()));

		float x1 = ImGui.getCursorScreenPosX();
		float y1 = ImGui.getCursorScreenPosY();

		if (lastIsSearched) {
			ImGui.pushStyleColor(ImGuiCol.Text, AchillesMenu.searchRGB);
		}

		RenderUtils.drawTexts(getFullName());

		if (lastIsSearched) {
			ImGui.popStyleColor();
		}

		ImVec2 last = ImGui.getCursorPos();
		ImGui.sameLine(0, 0);

		float x2 = ImGui.getCursorScreenPosX();
		ImGui.setCursorPos(last.x, last.y);

		ImGui.pushItemWidth(180f);
		ImGui.pushStyleColor(ImGuiCol.FrameBg,          c[0], c[1], c[2], 0.8f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgHovered,   c[0], c[1], c[2], 0.7f);
		ImGui.pushStyleColor(ImGuiCol.FrameBgActive,    c[0], c[1], c[2], 0.6f);
		ImGui.pushStyleColor(ImGuiCol.Button,           c[0], c[1], c[2], 0.8f);
		ImGui.pushStyleColor(ImGuiCol.ButtonHovered,    c[0], c[1], c[2], 0.7f);
		ImGui.pushStyleColor(ImGuiCol.ButtonActive,     c[0], c[1], c[2], 0.6f);
		ImGui.pushStyleColor(ImGuiCol.Header,        0.21f, 0.24f, 0.31f, 0.4f);
		ImGui.pushStyleColor(ImGuiCol.HeaderHovered, 0.21f, 0.24f, 0.31f, 0.3f);
		ImGui.pushStyleColor(ImGuiCol.HeaderActive,  0.21f, 0.24f, 0.31f, 0.2f);

		if (!selected.isEmpty()) {
			ArrayList<E> toRemove = new ArrayList<>();
			for (E e : selected) {
				float x = ImGui.getCursorPosX();
				float y = ImGui.getCursorPosY();

				if (ImGui.invisibleButton("##Selected-" + GuiUtils.uncoverTranslation(getTranslationKey(e)), 180, 18))
                    toRemove.add(e);

				if (ImGui.isItemHovered())
					ImGui.pushStyleColor(ImGuiCol.Text, 0.83f-0.3f, 0.86f-0.3f, 0.94f-0.3f, 1f);
				else
					ImGui.pushStyleColor(ImGuiCol.Text, 0.83f-0.2f, 0.86f-0.2f, 0.94f-0.2f, 1f);
				ImGui.setCursorPos(x,y);
				ImGui.text(GuiUtils.uncoverTranslation(getTranslationKey(e)));
				ImGui.popStyleColor();
				ImGui.setCursorPos(x, y+20);
			}
			toRemove.forEach(e -> selected.remove(e));
		}

		ImGui.pushItemWidth(170f);
		ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.15f, 0.17f, 0.22f, 1f);
		ImGui.pushStyleColor(ImGuiCol.TextDisabled, 0.83f, 0.86f, 0.94f, 0.5f);
		ImGui.inputTextWithHint("", "Search...", currentString);
		ImGui.popStyleColor(2);
		ImGui.popItemWidth();

		moduleManager.typing = false;
		if (ImGui.isItemFocused()) {
			moduleManager.typing = true;
			if (ImGui.isKeyDown(ImGui.getIO().getKeyMap(ImGuiKey.Backspace)) && currentString.isNotEmpty()) {
				currentString.set(currentString.get().substring(0, currentString.get().length() - 1));
			}
		}
		String input = currentString.get();

		int i = 0;
//		System.out.println(input.replace(" ", "").toLowerCase());
		notSelected = new ArrayList<>();
		for (E e : registry) {
			if (!selected.contains(e) && filter.test(e) && GuiUtils.uncoverTranslation(getTranslationKey(e)).replace(" ", "").toLowerCase().contains(input.replace(" ", "").toLowerCase()) && i < 5) {
				i++;
				notSelected.add(e);
			}
		}

		if (!notSelected.isEmpty()) {
			List<E> selectable = notSelected.size() > 4 ? notSelected.subList(0, 4) : notSelected;
//			System.out.println("size: "+selectable.size());
			for (E e : selectable) {
//				System.out.println("stuff: "+GuiUtils.uncoverTranslation(getTranslationKey(e)));
				float x = ImGui.getCursorPosX();
				float y = ImGui.getCursorPosY();

				if (ImGui.invisibleButton("##NotSelected-" + GuiUtils.uncoverTranslation(getTranslationKey(e)), 180, 18))
					selected.add(e);

				if (ImGui.isItemHovered())
					ImGui.pushStyleColor(ImGuiCol.Text, 0.83f-0.6f, 0.86f-0.6f, 0.94f-0.6f, 1f);
				else
					ImGui.pushStyleColor(ImGuiCol.Text, 0.83f-0.4f, 0.86f-0.4f, 0.94f-0.4f, 1f);
				ImGui.setCursorPos(x,y);
				ImGui.text(GuiUtils.uncoverTranslation(getTranslationKey(e)));
				ImGui.popStyleColor();
				ImGui.setCursorPos(x, y+20);
			}
		}

		float y2 = ImGui.getCursorScreenPosY();

		if (ImGui.getMousePosX() >= x1 && ImGui.getMousePosX() <= x2 && ImGui.getMousePosY() >= y1 && ImGui.getMousePosY() <= y2) {
			if (!getDescription().isEmpty()) {
				ToolTipHolder.setToolTip(getDescription().getContent());
			}
			if (KeyUtils.isKeyPressed(getResetKey())) {
				reset();
			}
		}

		ImGui.popStyleColor(9);
		ImGui.popItemWidth();
		ImGui.popID();
		ImGui.setCursorPos(ImGui.getCursorPosX(), ImGui.getCursorPosY()+10);
	}

	public RegistrySetting<E> setAdvanced() {
		advanced = true;
		return this;
	}
}