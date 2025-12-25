package template.rip.module;

import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.MathUtils;
import template.rip.gui.clickgui.SearchHolder;
import template.rip.gui.utils.Nameable;
import template.rip.module.modules.client.ConfigModule;
import template.rip.module.modules.render.ArrayListModule;
import template.rip.module.setting.Setting;
import template.rip.module.setting.settings.DividerSetting;
import template.rip.module.setting.settings.KeybindSetting;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Module implements Nameable {

	protected static MinecraftClient mc = MinecraftClient.getInstance();

	private final String[] name;
	private final String internalName;
	private final Description description;
	public List<Setting> settings = new ArrayList<>();
	public List<Setting> renderSettings = new ArrayList<>();
	public KeybindSetting keybind = new KeybindSetting(this, -1, false, Description.of("The module's Hold/Toggle keybind."), "Keybind").setAsModuleKeyBind();
	private final Category category;
	private boolean enabled;
	private boolean showOptions;

	public JColor nameColor;
	public String[] displayName = {};

	public long lastToggleOptions = 0L;
	public float settingsOpenProgress = 0f;
	private boolean isAnimatingOpening = false;

	// For renderables
	public boolean isFocused = false;
	public ImVec2 updatedPos = new ImVec2(0, 0);
	public ImVec2 position = new ImVec2();
	public boolean reloadPosition = false;
	private long lastOpenFrame;
	private static final HashMap<String, Module> name2ModuleCheck = new HashMap<>();
	private HashMap<String, Setting> getSettingByName;
	private DividerSetting lastDivider = null;
	private boolean lastIsSearched;

	public Module(@NotNull Category category, Description description, String... names) {
		if (names.length == 1) {// config compatibility
			names = MathUtils.split(names[0]);
		}
		this.name = new String[names.length];
		System.arraycopy(names, 0, this.name, 0, names.length);
		this.description = description;
		this.category = category;
		setInitKey();

		StringBuilder internalBuilder = new StringBuilder();
		for (String n : names) {
			internalBuilder.append(n.charAt(0));
		}
		this.internalName = internalBuilder.toString();

		enabled = false;
		showOptions = false;

		if (name2ModuleCheck.containsKey(internalName)) {
			Module exist = name2ModuleCheck.get(internalName);
//			System.out.println(exist.getClass().getSimpleName() + " already registered with " + exist.getName()  + Arrays.toString(exist.getFullName())  + ", " + getClass().getSimpleName() + " " + Arrays.toString(getFullName()) + " attempted to register with the same name");
		}
		name2ModuleCheck.put(internalName, this);
	}

	public void setInitKey() {}

	public void addSettings(Setting... settings) {
		this.settings.addAll(Arrays.asList(settings));
		this.settings.sort(Comparator.comparingInt(s -> s == keybind ? 1 : 0));
		this.renderSettings.addAll(Arrays.asList(settings));
		this.renderSettings.sort(Comparator.comparingInt(s -> s == keybind ? 1 : 0));

		if (getSettingByName == null) {
			getSettingByName = new HashMap<>();
		}

        for (Setting set : settings) {
			if (getSettingByName.containsKey(set.getName())) {
				Setting exist = getSettingByName.get(set.getName());
//				System.out.println(getClass().getSimpleName() + ": " + exist.getClass().getSimpleName() + " already registered with " + exist.getName()  + Arrays.toString(exist.getFullName())  + ", " + set.getClass().getSimpleName() + " " + Arrays.toString(set.getFullName()) + " attempted to register with the same name");
			}
            getSettingByName.put(set.getName(), set);
        }
	}

	@Nullable
	public Setting getSetting(String name) {
		return getSettingByName.get(name);
	}

	@Override
	public String getName() {
		return internalName;
	}

	public String[] getFullName() {
		String[] s = new String[name.length];
		System.arraycopy(name, 0, s, 0, name.length);
		return s;
	}

	public void setSearched(boolean isSearched) {
		lastIsSearched = isSearched;
		settings.forEach(s -> s.setSearched(false));
	}

	public void prepSearch(String[] str) {
		AtomicBoolean bl = new AtomicBoolean(false);
		settings.forEach(s -> bl.set(s.prepSearch(str) || bl.get()));
		lastIsSearched = SearchHolder.indexOfStrInValue(name, str) != -1 || bl.get();
	}

	/**
	 * Useful for debugging inside modules
	 * @param object
	 */
	public void info(Object object) {
		try {
			mc.inGameHud.getChatHud().addMessage(Text.of(object.toString()));
		} catch(Exception exception) {
			System.out.println("There's a problem with a value inside info method inside Module class!");
		}
	}

	public boolean isNotSearched() {
		return !lastIsSearched;
	}

	public Category getCategory() {
		return this.category;
	}

	public Description getDescription() {
		return description;
	}

	public String getSuffix() { return ""; }

	public String[] getNameSuffix() {
		String[] gns = new String[this.name.length + 1];
		gns[this.name.length] = getSuffix();
		System.arraycopy(this.name, 0, gns, 0, this.name.length);
		return gns;
	}

	public int getKey() {
		return keybind.getCode();
	}

	public boolean isHold() {
		return keybind.isHold();
	}

	public void setKey(int key) {
		this.keybind.setKeyCode(key);
	}

	public void toggle() {
		setEnabled(!isEnabled());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			if (enabled) {
				enable();
			} else {
				disable();
			}
		} // else this module is already enabled/disabled
	}

	public void setShowOptions(boolean show) {
		if (showOptions() && !show) {
			toggleShowOptions();
		} else if (show && !showOptions()) {
			toggleShowOptions();
		}
	}

	public boolean showOptions() {
		return showOptions;
	}

	public boolean isAnimatingOpening() {
		return isAnimatingOpening;
	}

	public void setAnimatingOpening(boolean animatingOpening) {
		isAnimatingOpening = animatingOpening;
	}

	public void toggleShowOptions() {
		if (Template.frame == lastOpenFrame) {
			return;
		}
		lastOpenFrame = Template.frame;
		this.showOptions = !this.showOptions;
		this.lastToggleOptions = System.currentTimeMillis();
		this.isAnimatingOpening = true;

		if (this.showOptions) {
			this.settingsOpenProgress = 0;

			for (Setting setting : settings) {
				if (setting instanceof DividerSetting) {
					((DividerSetting)setting).setOpen(false);
				}
			}
		}
	}

	protected void enable() {
		this.enabled = true;
		onEnable();
		Template.EVENTBUS.subscribe(this);
		ArrayListModule.moduleToggleOn.put(this, System.currentTimeMillis());
		//doFunny();
	}

	protected void doFunny() {
		Template.funny();
	}

	protected void disable() {
		this.enabled = false;
		onDisable();
		unPressKeyBindSettings();
		Template.EVENTBUS.unsubscribe(this);
		ArrayListModule.moduleToggleOff.put(this, System.currentTimeMillis());
	}

	public void onEnable() {}

	public void onDisable() {}

	public boolean nullCheck() {
		return (mc.player != null && mc.world != null && mc.interactionManager != null && mc.getNetworkHandler() != null);
	}

	public void renderSettings() {
		renderSettings.forEach(Setting::renderIfDoDisplay);
	}

	public void mergeDividers() {
		List<Setting> settingsList = new ArrayList<>(settings);

		for (Setting setting : settingsList) {
			if (setting instanceof DividerSetting) {
				lastDivider = (DividerSetting) setting;
			} else if (setting != this.keybind && lastDivider != null) {
				lastDivider.addSetting(setting);
			}
		}
	}

	public float getSettingsHeight() {
		float totalHeight = 0;
		DividerSetting lastDivider = null;

		for (Setting setting : settings) {
			if (setting.advanced && !Template.isAdvanced()) continue;

			if (setting instanceof DividerSetting divider) {
				lastDivider = divider;
			} else if (!setting.doDisplay() || (lastDivider != null && lastDivider.settings.contains(setting))) {
				continue;
			}

			totalHeight += setting.getHeight();
		}

		return totalHeight;
	}

	public float getSettingsHeight2() {
		boolean isNormal = true;
		for (Setting s : settings) {
			if (s instanceof DividerSetting) {
				isNormal = false;
				break;
			}
		}

		if (this instanceof ConfigModule) {
			isNormal = false;
		}

		if (!isNormal) {
			ImGui.beginChild(this.getName() + "/SettingsCalc", 1, 1, false);
			float Y = ImGui.getCursorPosY();
			this.renderSettings();
			float nextY = ImGui.getCursorPosY() - Y;
			ImGui.endChild();

			return nextY;
		} else {
			return getSettingsHeight();
		}
	}

	public void unPressKeyBindSettings() {
		settings.stream().filter(s -> s instanceof KeybindSetting).forEach(s -> ((KeybindSetting) s).setPressed(false));
	}

	public static int compare(Module m1, Module m2) {
		String[] fullNameOne = m1.getFullName();
		String[] fullNameTwo = m2.getFullName();
		for (int i = 0; i < Math.min(fullNameOne.length, fullNameTwo.length); i++) {
			int comp = CharSequence.compare(fullNameOne[i], fullNameTwo[i]);
			if (comp != 0) {
				return comp;
			}
		}
		return fullNameOne.length - fullNameTwo.length;
	}

	@Override
	public String toString() {
		return internalName;
	}

	public enum Category {
		COMBAT("\ue19b  Combat"),
		CRYSTAL("\uf48d  Crystal"),
		BLATANT("\uf54c  Blatant"),
		RENDER("\uf1fc  Render"),
		PLAYER("\uf007  Player"),
		MISC("\uf0ca  Misc"),
		CLIENT("\uf013  Client"),
		LEGIT("  Legit"),
		ALL("  All");
		public final String name;

		Category(String name) {
			this.name = name;
		}
	}
}
