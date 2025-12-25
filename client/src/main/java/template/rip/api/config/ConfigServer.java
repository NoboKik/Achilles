/*
package template.rip.api.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.Setting;
import template.rip.module.setting.settings.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final int port;
    private boolean shouldDo;
    public ConfigServer(int port) {
        this.port = port;
        shouldDo = true;
        ConfigServer me = this;
        Runnable r = () -> {
            while (shouldDo()) {
                try {
                    me.start();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(r);
        serverThread.setUncaughtExceptionHandler((t, e) -> {});
        serverThread.start();
    }
    private boolean shouldDo() {
        return shouldDo;
    }
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            switch (inputLine) {
                case "Stop" : {
                    out.println("Okay");
                    stop();
                    continue;
                }
                case "Config" : {
                    out.println(new Gson().toJson(getStuff(What.Config)));
                    continue;
                }
                case "Info" : {
                    out.println(new Gson().toJson(getStuff(What.Info)));
                    continue;
                }
            }
            String[] str = inputLine.split("/");
            boolean validOperation = str.length == 3;
            if (validOperation) {
                Module module = */
/*Template.moduleManager.getModule(str[0])*//*
null;
                if (module == null) {
                    out.println(str[0] + " is not a valid module");
                    continue;
                } else {
                    Setting setting;
                    if (str[1].equalsIgnoreCase("extended")) {
                        boolean bool = Boolean.parseBoolean(str[2]);
                        boolean now = module.showOptions();
                        module.setShowOptions(bool);
                        if (now == module.showOptions()) {
                            out.println("Nothing to change");
                            continue;
                        }
                    } else if (str[1].equalsIgnoreCase("enabled")) {
                        boolean bool = Boolean.parseBoolean(str[2]);
                        boolean now = module.isEnabled();
                        module.setEnabled(bool);
                        if (now == module.isEnabled()) {
                            out.println("Nothing to change");
                            continue;
                        }
                    } else if ((setting = module.getSetting(str[1])) != null) {
                        String value = str[2];
                        if (value.equals("Reset")) {
                            setting.reset();
                        } else {
                            if (setting instanceof BooleanSetting) {
                                ((BooleanSetting) setting).setEnabled(Boolean.parseBoolean(value));
                            } else if (setting instanceof KeybindSetting) {
                                String[] strings = value.split(",");
                                if (strings.length != 2) {
                                    out.println("Unable to split string for KeyBindSetting, got: " + strings.length + " expected: 2");
                                    continue;
                                }

                                ((KeybindSetting) setting).setHold(Boolean.parseBoolean(strings[0]));
                                try {
                                    int i = Integer.parseInt(strings[1]);
                                    ((KeybindSetting) setting).setKeyCode(i);
                                } catch (NumberFormatException e) {
                                    out.println("Failed to parse number for KeyBindSetting: " + strings[1]);
                                    continue;
                                }

                            } else if (setting instanceof ModeSetting<?>) {
                                ((ModeSetting<?>) setting).setMode(Integer.parseInt(value));
                            } else if (setting instanceof NumberSetting) {
                                try {
                                    double i = Double.parseDouble(value);
                                    ((NumberSetting) setting).setValue(i);
                                } catch (NumberFormatException e) {
                                    out.println("Failed to parse number for NumberSetting: " + value);
                                    continue;
                                }
                            } else if (setting instanceof StringSetting) {
                                ((StringSetting) setting).setContent(value);
                            } else if (setting instanceof ColorSetting) {
                                String[] strings = value.split(",");
                                if (strings.length != 2) {
                                    out.println("Unable to split string for ColorSetting, got: " + strings.length + " expected: 2");
                                    continue;
                                }
                                int color = ((ColorSetting) setting).getColor().getRGB();
                                boolean bl = Boolean.parseBoolean(strings[1]);
                                try {
                                    color = Integer.parseInt(strings[0]);
                                    ((ColorSetting) setting).setColor(new JColor(color, true), bl);
                                } catch (NumberFormatException e) {
                                    out.println("Failed to parse number for ColorSetting: " + strings[0]);
                                    ((ColorSetting) setting).setColor(new JColor(color, true), bl);
                                    continue;
                                }

                            } else if (setting instanceof MinMaxNumberSetting) {
                                String[] strings = value.split(",");
                                if (strings.length != 2) {
                                    out.println("Unable to split string for MinMaxNumberSetting, got: " + strings.length + " expected: 2");
                                    continue;
                                }
                                boolean toBreak = false;
                                try {
                                    double d = Double.parseDouble(strings[0]);
                                    ((MinMaxNumberSetting) setting).setMinValue(d);
                                } catch (NumberFormatException e) {
                                    out.println("Failed to parse min number for MinMaxNumberSetting: " + strings[0]);
                                    toBreak = true;
                                }
                                try {
                                    double d = Double.parseDouble(strings[1]);
                                    ((MinMaxNumberSetting) setting).setMaxValue(d);
                                } catch (NumberFormatException e) {
                                    out.println("Failed to parse max number for MinMaxNumberSetting: " + strings[1]);
                                    toBreak = true;
                                }
                                if (toBreak) {
                                    continue;
                                }
                            }*/
/* else if (setting instanceof PosSetting posSetting) {
                            if (!settingJson.isJsonObject())
                                return;

                            JsonObject posJson = settingJson.getAsJsonObject();
                            posSetting.setX(posJson.get("x").getAsFloat());
                            posSetting.setY(posJson.get("y").getAsFloat());
                        }*//*
 else if (setting instanceof RegistrySetting<?>) {
                                String[] strings = value.split(",");
                                ArrayList<String> failed = ((RegistrySetting<?>) setting).loadStrings(Arrays.asList(strings));
                                if (!failed.isEmpty()) {
                                    out.println("Failed to load: " + failed);
                                    continue;
                                }
                            } else if (setting instanceof ButtonSetting) {
                                if (Boolean.parseBoolean(value)) {
                                    ((ButtonSetting) setting).runnable.run();
                                }
                            }
                        }

                    } else {
                        out.println(str[1] + " is not a valid setting");
                        continue;
                    }
                }
            } else {
                out.println("Not enough args, got: " + str.length + " expected: 3");
                continue;
            }
            out.println("Success");
        }
        stop();
    }
    public void stop() throws IOException {
        shouldDo = false;
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
    private JsonObject getStuff(What youWant) {
        JsonObject jsonProfile = new JsonObject();
        JsonObject settingTypeHash = new JsonObject();

        for (Module module : Template.moduleManager.getModules()) {
            JsonObject moduleConfig = new JsonObject();
            JsonObject settingsInModule = new JsonObject();

            moduleConfig.addProperty("extended", module.showOptions());
            moduleConfig.addProperty("enabled", module.isEnabled());
            for (Setting setting : module.settings) {
                ConfigProfile.saveSetting(setting, moduleConfig);

                JsonObject settingInfo = new JsonObject();

                settingInfo.addProperty("type", setting.getType().name());
                settingInfo.addProperty("description", */
/*setting.getDescription()*//*
"Unsupported");
                settingInfo.addProperty("advanced", setting.advanced);

                if (setting instanceof BooleanSetting) {

                    settingInfo.addProperty("default", ((BooleanSetting) setting).defaultEnabled);

                } else if (setting instanceof KeybindSetting) {

                    settingInfo.addProperty("default", ((KeybindSetting) setting).defaultCode);
                    settingInfo.addProperty("canHold", ((KeybindSetting) setting).canHold);

                } else if (setting instanceof ModeSetting<?>) {

                    settingInfo.addProperty("default", ((ModeSetting<?>) setting).getDisplayName(((ModeSetting<?>) setting).modes.get(((ModeSetting<?>) setting).defaultIndex)));
                    JsonArray ja = new JsonArray();
                    List<String> strings = ((ModeSetting<?>) setting).modes.stream().map(((ModeSetting<?>) setting)::getDisplayName).collect(Collectors.toList());
                    for (String s : strings) {
                        ja.add(s);
                    }
                    settingInfo.add("options", ja);

                } else if (setting instanceof NumberSetting) {

                    settingInfo.addProperty("default", ((NumberSetting) setting).defaultValue);
                    settingInfo.addProperty("minimum", ((NumberSetting) setting).minimum);
                    settingInfo.addProperty("maximum", ((NumberSetting) setting).maximum);
                    settingInfo.addProperty("decimal", ((NumberSetting) setting).decimal);

                } else if (setting instanceof StringSetting) {

                    settingInfo.addProperty("default", ((StringSetting) setting).defaultContent);

                } else if (setting instanceof ColorSetting) {

                    settingInfo.addProperty("default", ((ColorSetting) setting).defaultColor.getRGB());
                    settingInfo.addProperty("defaultRainbow", ((ColorSetting) setting).defaultRainbow);
                    settingInfo.addProperty("hasAlpha", ((ColorSetting) setting).alpha);

                } else if (setting instanceof MinMaxNumberSetting) {

                    settingInfo.addProperty("defaultMin", ((MinMaxNumberSetting) setting).defaultMinValue);
                    settingInfo.addProperty("defaultMax", ((MinMaxNumberSetting) setting).defaultMaxValue);
                    settingInfo.addProperty("minimum", ((MinMaxNumberSetting) setting).minimum);
                    settingInfo.addProperty("maximum", ((MinMaxNumberSetting) setting).maximum);
                    settingInfo.addProperty("decimal", ((MinMaxNumberSetting) setting).decimal);

                } else if (setting instanceof RegistrySetting<?>) {

                    JsonArray all = new JsonArray();
                    List<String> str1 = ((RegistrySetting<?>) setting).registryToId();
                    for (String st : str1) {
                        all.add(st);
                    }
                    JsonArray selected = new JsonArray();
                    List<String> str2 = ((RegistrySetting<?>) setting).ids();
                    for (String st : str2) {
                        selected.add(st);
                    }
                    JsonArray defaults = new JsonArray();
                    List<String> str3 = ((RegistrySetting<?>) setting).defaultIds();
                    for (String st : str3) {
                        defaults.add(st);
                    }
                    settingInfo.add("all", all);
                    settingInfo.add("default", defaults);

                } else if (setting instanceof DividerSetting) {

                    List<String> names = ((DividerSetting) setting).settings.stream().map(Setting::getName).collect(Collectors.toList());
                    JsonArray ja = new JsonArray();
                    for (String s : names) {
                        ja.add(s);
                    }
                    settingInfo.add("childSettings", ja);

                }

                settingsInModule.add(setting.getName(), settingInfo);
            }

            if (jsonProfile.has(module.getName())) {
                jsonProfile.remove(module.getName());
            }

            jsonProfile.add(module.getName(), moduleConfig);

            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("description", */
/*module.getDescription()*//*
"Unsupported");
            moduleJson.addProperty("category", module.getCategory().name());
            moduleJson.add("settings", settingsInModule);
            settingTypeHash.add(module.getName(), moduleJson);
        }

        jsonProfile.add("friends", PlayerUtils.friends);
        switch (youWant) {
            case Info : return settingTypeHash;
            case Config : return jsonProfile;
        }
        return null;
    }
    private enum What{Info, Config}
}
*/
