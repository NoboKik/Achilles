package template.rip.module.setting;

import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.object.Description;
import template.rip.api.util.MathUtils;
import template.rip.gui.clickgui.SearchHolder;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;

public abstract class Setting {

	protected final String[] name;
	protected final String internalName;
	protected Module parent;
	protected float height;
	protected final Description description;
	protected long hoverTime;
	protected BooleanSetting booleanCondition = null;
	protected boolean booleanConditionValue = true;
	protected ModeSetting<?> modeConditionSetting = null;
	protected Enum<?> modeConditionValue = null;
	public boolean advanced = false;

	public Setting(Description description, String... names) {
		if (names.length == 1) {
			names = MathUtils.split(names[0]);
		}
		this.name = new String[names.length];
		System.arraycopy(names, 0, this.name, 0, names.length);
		this.description = description == null ? Description.of() : description;

		StringBuilder internalBuilder = new StringBuilder();
		for (String n : names) {
			internalBuilder.append(n.charAt(0));
		}
		this.internalName = internalBuilder.toString();
	}

	public boolean doDisplay() {
        return (booleanCondition == null || booleanCondition.isEnabled() == booleanConditionValue) && (modeConditionSetting == null || modeConditionSetting.getMode().ordinal() == modeConditionValue.ordinal()) && (!advanced || Template.isAdvanced());
    }

	public void renderIfDoDisplay() {
		if (doDisplay()) {
			render();
		}
	}

	public Setting addConditionBoolean(BooleanSetting setting, boolean b) {
		booleanCondition = setting;
		booleanConditionValue = b;
		return this;
	}

	public Setting addConditionMode(ModeSetting<?> setting, Enum<?> s) {
		modeConditionSetting = setting;
		modeConditionValue = s;
		return this;
	}

	public SettingType getType() {
		return SettingType.None;
	}

	public String[] getFullName() {
		String[] n = new String[name.length];
		System.arraycopy(name, 0, n, 0, name.length);
		return n;
	}

	public String getName() {
		return internalName;
	}

	public Description getDescription() {
		return description;
	}

	public Module getParent() {
		return parent;
	}

	public void setParent(Module parent) {
		if (this.parent == null) {
			this.parent = parent;

			parent.addSettings(this);
		}
	}

	public float getHeight() {
		return height;
	}

	public long getHoverTime() {
		return hoverTime;
	}

	public void setHoverTime(long hoverTime) {
		this.hoverTime = hoverTime;
	}

	public int getResetKey() {
		if (Template.moduleManager != null) {
			AchillesSettingsModule asm = Template.moduleManager.getModule(AchillesSettingsModule.class);
			if (asm != null) {
				return asm.keyBind.getCode();
			}
		}
		return GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
	}

	protected boolean lastIsSearched;

	public void setSearched(boolean isSearched) {
		lastIsSearched = isSearched;
	}

	public boolean prepSearch(String[] str) {
		if (!doDisplay()) {
			return (lastIsSearched = false);
		}
		return (lastIsSearched = SearchHolder.indexOfStrInValue(name, str) != -1);
	}

	public void reset() {}

	public void render() {}
}