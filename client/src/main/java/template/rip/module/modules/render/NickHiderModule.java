package template.rip.module.modules.render;

import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.StringSetting;

public class NickHiderModule extends Module {

    public final StringSetting ownNick = new StringSetting("discord.gg/achillesac", this, "Own Nick");
    public final BooleanSetting changeOwnSkin = new BooleanSetting(this, false, "Change Own Skin");
    public final StringSetting ownSkinURL = new StringSetting("https://s.namemc.com/i/a0b94a937f250c98.png", this, "Own Skin URL");
    public final BooleanSetting obfTarget = new BooleanSetting(this, true, "Obfuscate First Target");
    public final StringSetting targetNick = new StringSetting("Victim", this, "First Target's Nick");
    public final BooleanSetting changeTargetSkin = new BooleanSetting(this, false, "Change Target's Skin");
    public final StringSetting targetSkinURL = new StringSetting("https://s.namemc.com/i/a0b94a937f250c98.png", this, "First Target's Skin URL");
    public final BooleanSetting obfFriends = new BooleanSetting(this, true, "Obfuscate Friends");
    public final StringSetting obfFriendsTemplate = new StringSetting("Friend", this, "Friends Nick");
    public final BooleanSetting obfOthers = new BooleanSetting(this, true, "Obfuscate others");

    public NickHiderModule(Category category, Description description, String name) {
        super(category, description, name);
        targetSkinURL.addConditionBoolean(changeTargetSkin, true);
        obfFriendsTemplate.addConditionBoolean(obfFriends, true);
        targetNick.addConditionBoolean(obfTarget, true);
        ownSkinURL.addConditionBoolean(changeOwnSkin, true);
    }
}
