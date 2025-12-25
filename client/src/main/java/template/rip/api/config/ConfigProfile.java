package template.rip.api.config;

import me.sootysplash.bite.BiteArray;
import me.sootysplash.bite.BiteMap;
import me.sootysplash.bite.TypeObject;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.UI;
import template.rip.gui.windowgui.MainMenu;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.modules.client.ConfigModule;
import template.rip.module.modules.client.InterfaceModule;
import template.rip.module.setting.Setting;
import template.rip.module.setting.settings.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static template.rip.api.config.ConfigManager.pathProfilesFolder;

public class ConfigProfile {

    private String name;
    private Path pathProfile;
    private BiteMap profile;

    public ConfigProfile(String name, Path pathProfile) {
        this.name = name;
        this.pathProfile = pathProfile;

        try {
            if (name == null || name.isEmpty()) {
                if (Files.isRegularFile(pathProfile)) {
                    this.profile = BiteMap.fromBytes(Files.readAllBytes(pathProfile));

                    if (profile.size() == 0) return;

                    TypeObject profileName = this.profile.get("profileName");

                    if (profileName == null) return;

                    this.name = profileName.getCharSequence().toString();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public String getName() {
        return name;
    }

    public Path getPathProfile() {
        return pathProfile;
    }

    public ConfigProfile setPathProfile(Path pathProfile) {
        this.pathProfile = pathProfile;
        return this;
    }

    public void loadProfile(boolean loadVisuals) {
        try {
            if (!Files.isRegularFile(pathProfile))
                return;

            AchillesSettingsModule achillesSettings = Template.moduleManager.getModule(AchillesSettingsModule.class);

            for (Module module : Template.moduleManager.getModules()) {
                if (module instanceof ConfigModule) continue;

                switch (module.getCategory()) {
                    case LEGIT, RENDER: {
                        if (!loadVisuals) continue;
                        break;
                    }
                    default: {
                        boolean isInterface = module.getClass().equals(InterfaceModule.class);

                        if (!loadVisuals) {
                            if (isInterface) continue;
                            break;
                        } else {
                            if (module != achillesSettings && !isInterface) continue;
                        }
                    }
                }

                TypeObject moduleJson = profile.get(module.getName());
                if (moduleJson == null || !moduleJson.isNest())
                    continue;
                BiteMap moduleConfig = moduleJson.getNest();

                TypeObject enabledJson = moduleConfig.get("enabled");
                if (enabledJson != null && enabledJson.isBoolean()) {
                    module.setEnabled(enabledJson.getBoolean());
                }
                try {
                    moduleConfig.get("extended").getBoolean();
                } catch (Exception ignored) {}
                for (Setting setting : module.settings) {
                    TypeObject settingJson = moduleConfig.get(setting.getName());
                    if (settingJson == null)
                        continue;

                    if (module == achillesSettings) {
                        switch (setting.getType()) {
                            case KeyBind, Boolean: {
                                if (loadVisuals) continue;
                                break;
                            }
                            case Mode: {
                                if (setting == achillesSettings.moveFixMode) {
                                    if (loadVisuals) continue;
                                } else {
                                    if (!loadVisuals) continue;
                                }
                            }
                            default: if (!loadVisuals) continue;
                        }
                    }

                    try {
                        loadSetting(setting, settingJson);
                    } catch (Exception ignored) {}
                }
                if (module.getCategory() == Module.Category.RENDER || module.getCategory() == Module.Category.LEGIT) {
                    TypeObject positionXJson = moduleConfig.get("posX");
                    TypeObject positionYJson = moduleConfig.get("posY");
                    if (positionXJson != null)
                        module.position.x = positionXJson.getFloat();
                    if (positionYJson != null)
                        module.position.y = positionYJson.getFloat();
                    module.reloadPosition = true;
                }
            }

            TypeObject friendsJson = this.profile.get("friends");
            if (friendsJson != null && friendsJson.isNest())
                PlayerUtils.friends = friendsJson.getNest();
        } catch (Exception ignored) {
        }
        if (Template.configManager().isInit) {
            if (Template.moduleManager.getModule(AchillesSettingsModule.class).mode.getMode() == AchillesSettingsModule.modeEnum.ClickGUI
                    && !Template.moduleManager.isModuleEnabled(ConfigModule.class)) {
                Template.moduleManager.getModule(ConfigModule.class).setEnabled(true);
            }
            if (Template.moduleManager.getModule(AchillesSettingsModule.class).mode.getMode() == AchillesSettingsModule.modeEnum.Menu
                    && !Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class)) {
                Template.moduleManager.getModule(AchillesSettingsModule.class).setEnabled(true);
                MainMenu.getInstance().selectedSection = "\uF013 Config";
            }
        } else {
            Template.moduleManager.getModule(ConfigModule.class).setEnabled(false);
            Template.moduleManager.getModule(AchillesSettingsModule.class).setEnabled(true);
        }
        UI.color1 = InterfaceModule.instance.color1.getColor();
        UI.color2 = InterfaceModule.instance.color2.getColor();
    }

    public void loadProfile() {
        loadProfile(true);
        loadProfile(false);
    }

    public void saveProfile() {
        try {
            Files.createDirectories(pathProfilesFolder);

            boolean isProfileSaved = profile != null;

            if (!isProfileSaved) {
                profile = BiteMap.newInstance();

                profile.add("profileName", this.name);
            }

            for (Module module : Template.moduleManager.getModules()) {
                BiteMap moduleConfig = BiteMap.newInstance();

                moduleConfig.add("enabled", module.isEnabled());
                moduleConfig.add("extended", module.showOptions());
                for (Setting setting : module.settings) {
                    saveSetting(setting, moduleConfig);
                }

                if (module.getCategory() == Module.Category.RENDER || module.getCategory() == Module.Category.LEGIT) {
                    moduleConfig.add("posX", module.position.x);
                    moduleConfig.add("posY", module.position.y);
                }

                if (profile.has(module.getName())) {
                    profile.remove(module.getName());
                }

                profile.add(module.getName(), moduleConfig);
            }

            if (!isProfileSaved) {
                profile.add("friends", PlayerUtils.friends);
            }

            Files.write(pathProfile, profile.getBytes());
        } catch (Exception ignored) {
        }
    }

    public static void saveSetting(Setting setting, BiteMap toJsonObject) {
        if (setting instanceof BooleanSetting) {
            toJsonObject.add(setting.getName(), ((BooleanSetting) setting).isEnabled());
        } else if (setting instanceof KeybindSetting) {
            BiteMap keybindJson = BiteMap.newInstance();
            keybindJson.add("keyCode", ((KeybindSetting) setting).getCode());
            keybindJson.add("hold", ((KeybindSetting) setting).isHold());

            toJsonObject.add(setting.getName(), keybindJson);
        } else if (setting instanceof ModeSetting<?>) {
            toJsonObject.add(setting.getName(), ((ModeSetting<?>) setting).index);
        } else if (setting instanceof NumberSetting) {
            toJsonObject.add(setting.getName(), ((NumberSetting) setting).getValue());
        } else if (setting instanceof StringSetting) {
            toJsonObject.add(setting.getName(), ((StringSetting) setting).getContent());
        } else if (setting instanceof ColorSetting) {
            BiteMap colorJson = BiteMap.newInstance();
            colorJson.add("color", ((ColorSetting) setting).getValue().getRGB());
            colorJson.add("rainbow", ((ColorSetting) setting).isRainbow());

            toJsonObject.add(setting.getName(), colorJson);
        } else if (setting instanceof MinMaxNumberSetting) {
            BiteMap minMaxNumberJson = BiteMap.newInstance();
            minMaxNumberJson.add("min", ((MinMaxNumberSetting) setting).getMinValue());
            minMaxNumberJson.add("max", ((MinMaxNumberSetting) setting).getMaxValue());

            toJsonObject.add(setting.getName(), minMaxNumberJson);
        } else if (setting instanceof PosSetting) {
            BiteMap posJson = BiteMap.newInstance();
            posJson.add("x", ((PosSetting) setting).getX());
            posJson.add("y", ((PosSetting) setting).getY());

            toJsonObject.add(setting.getName(), posJson);
        } else if (setting instanceof RegistrySetting<?>) {
            BiteMap registryJson = BiteMap.newInstance();

            List<String> indexes = ((RegistrySetting<?>) setting).ids();
            BiteArray array = BiteArray.newInstance();
            for (String str : indexes) {
                array.add(str);
            }

            registryJson.add("selectedStrings", array);
            toJsonObject.add(setting.getName(), registryJson);
        }
    }

    public static void loadSetting(Setting setting, TypeObject settingJson) {
        if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).setEnabled(settingJson.getBoolean());
        } else if (setting instanceof KeybindSetting) {
            BiteMap keybindJson = settingJson.getNest();

            ((KeybindSetting) setting).setHold(keybindJson.get("hold").getBoolean());
            ((KeybindSetting) setting).setKeyCode(keybindJson.get("keyCode").getInteger());
        } else if (setting instanceof ModeSetting<?>) {
            if (settingJson.isCharSequence()) {
                ((ModeSetting<?>) setting).setMode(settingJson.getCharSequence());
            } else {
                ((ModeSetting<?>) setting).setMode(settingJson.getInteger());
            }
        } else if (setting instanceof NumberSetting) {
            ((NumberSetting) setting).value = settingJson.getDouble();
        } else if (setting instanceof StringSetting) {
            ((StringSetting) setting).setContent(settingJson.getCharSequence().toString());
        } else if (setting instanceof ColorSetting) {
            if (!settingJson.isNest())
                return;

            BiteMap colorJson = settingJson.getNest();
            ((ColorSetting) setting).setColor(new JColor(colorJson.get("color").getInteger(), true), colorJson.get("rainbow").getBoolean());
        } else if (setting instanceof MinMaxNumberSetting) {
            if (!settingJson.isNest())
                return;

            BiteMap minMaxNumberJson = settingJson.getNest();
            ((MinMaxNumberSetting) setting).setMinValue(minMaxNumberJson.get("min").getDouble());
            ((MinMaxNumberSetting) setting).setMaxValue(minMaxNumberJson.get("max").getDouble());
        } else if (setting instanceof PosSetting) {
            if (!settingJson.isNest())
                return;

            BiteMap posJson = settingJson.getNest();
            ((PosSetting) setting).setX(posJson.get("x").getFloat());
            ((PosSetting) setting).setY(posJson.get("y").getFloat());
        } else if (setting instanceof RegistrySetting<?>) {
            if (!settingJson.isNest())
                return;

            BiteArray selectedStrings = settingJson.getNest().get("selectedStrings").getArray();

            ((RegistrySetting<?>) setting).loadStrings(selectedStrings.asList().stream().map(t -> t.getCharSequence().toString()).collect(Collectors.toList()));
        }
    }
}
