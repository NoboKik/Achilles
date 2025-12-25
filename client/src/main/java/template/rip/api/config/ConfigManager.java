package template.rip.api.config;

import by.radioegor146.nativeobfuscator.Native;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Native
public class ConfigManager {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Path pathProfilesFolder = null;
    public static Path pathRecordFolder = null;
    public static final Path pathConfigFolder = Paths.get(System.getProperty("user.home")).resolve("template");
    public final Map<Path, ConfigProfile> profiles = new HashMap<>();
    public final Map<String, ConfigProfile> profilesByName = new HashMap<>();
    public boolean isInit = false;
    public boolean loadingConfig;
    private String[] namesArray;
    private long lastOpenTime;

    public ConfigManager() {
        pathProfilesFolder = pathConfigFolder.resolve("profiles");
        pathRecordFolder = pathConfigFolder.resolve("record");

        File[] files = pathProfilesFolder.toFile().listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Path pathProfile = file.toPath();

                    if (pathProfile.toString().contains(".ac")) {
                        addProfile(new ConfigProfile(null, pathProfile));
                    }
                }
            }
        }
    }

    public void addProfile(ConfigProfile configProfile) {
        if (!profiles.containsKey(configProfile.getPathProfile())) {
            profiles.put(configProfile.getPathProfile(), configProfile);
            profilesByName.put(configProfile.getName(), configProfile);

            updateNamesArray();
        } else {
            addProfile(configProfile.setPathProfile(pathProfilesFolder.resolve(configProfile.getPathProfile().getFileName().toString().replace(".ac", "1.ac"))));
        }
    }

    public void openFolder() {
        if (System.currentTimeMillis() - lastOpenTime > 1000) {
            lastOpenTime = System.currentTimeMillis();
            new Thread(() -> {
                try {
                    openFolder(pathProfilesFolder.toString());
                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                }
            }).start();
        }
    }

    public static void openFolder(String folderPath) {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;

        try {
            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("explorer.exe", folderPath);
            } else if (os.contains("mac")) {
                processBuilder = new ProcessBuilder("open", folderPath);
            } else if (os.contains("nix") || os.contains("nux")) {
                processBuilder = new ProcessBuilder("xdg-open", folderPath);
            } else {
                System.err.println("Unsupported operating system.");
                return;
            }
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshProfiles() {
        File[] files = pathProfilesFolder.toFile().listFiles();
        List<Path> newProfiles = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Path pathProfile = file.toPath();

                    if (pathProfile.toString().contains(".ac")) {
                        ConfigProfile newProfile = new ConfigProfile(null, pathProfile);
                        newProfiles.add(pathProfile);

                        if (!profiles.containsKey(pathProfile)) {
                            addProfile(newProfile);
                        }
                    }
                }
            }
        }

        List<ConfigProfile> profilesToDel = new ArrayList<>();

        for (ConfigProfile profile : profiles.values()) {
            if (!newProfiles.contains(profile.getPathProfile())) {
                profilesToDel.add(profile);
            }
        }

        for (ConfigProfile profileToDel : profilesToDel) {
            removeProfile(profileToDel);
        }
    }

    public void removeProfile(ConfigProfile configProfile) {
        profiles.remove(configProfile.getPathProfile());
        profilesByName.remove(configProfile.getName());

        updateNamesArray();
    }

    private void updateNamesArray() {
        ArrayList<String> set = new ArrayList<>(profilesByName.keySet());
        String[] array = new String[set.size()];
        for (int i = 0; i < set.size(); i++) {
            String str = set.get(i);
            array[i] = str;
        }
        namesArray = array;
    }

    public void loadDefault() {
        loadingConfig = true;

        ConfigProfile defaultConfig = getProfile("Default");

        if (namesArray == null || defaultConfig == null) {
            Path defaultProfilePath = pathProfilesFolder.resolve("default.ac");
            defaultConfig = new ConfigProfile("Default", defaultProfilePath);

            if (!profiles.containsKey(defaultProfilePath)) {
                addProfile(defaultConfig);
            }
        }

        defaultConfig.loadProfile();

        loadingConfig = false;
        isInit = true;
        printModuleInfo();
    }

    private void printModuleInfo() {
        /*new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            for (Module.Category category : Module.Category.values()) {
                JsonArray ja = new JsonArray();
                for (Module module : Template.moduleManager.getModulesByCategory(category)) {
                    StringBuilder nameSb = new StringBuilder();
                    for (String n : module.getFullName()) {
                        nameSb.append(n);
                    }
                    StringBuilder descSb = new StringBuilder();
                    for (String n : module.getDescription().getContent()) {
                        descSb.append(n);
                    }
                    JsonObject jo = new JsonObject();
                    jo.addProperty("name", nameSb.toString());
                    jo.addProperty("description", descSb.toString());
                    ja.add(jo);
                }
                try {
                    Files.writeString(Paths.get(category.name().toLowerCase() + "_module.json"), new Gson().toJson(ja));
                } catch (Exception ignored) {
                }
            }
        }).start();*/
    }

    @Nullable
    public ConfigProfile getProfile(String name) {
        for (ConfigProfile profile : profiles.values()) {
            if (name.equals(profile.getName())) {
                return profile;
            }
        }

        return null;
    }

    @Nullable
    public ConfigProfile getProfile(StringBuilder path) {
        for (ConfigProfile profile : profiles.values()) {
            if (profile.getPathProfile().toString().equalsIgnoreCase(path.toString())) {
                return profile;
            }
        }

        return null;
    }

    public List<ConfigProfile> getProfiles() {
        return new ArrayList<>(profiles.values());
    }

    public void saveDefault() {
        try {
            Files.createDirectories(pathConfigFolder);

            ConfigProfile defaultProfile = getProfile("Default");
            if (defaultProfile == null) {
                ConfigProfile defaultConfig = new ConfigProfile("Default", pathProfilesFolder.resolve("default.ac"));
                addProfile(defaultConfig);
            }

            defaultProfile.saveProfile();
            defaultProfile.saveProfile();
        } catch (IOException ignored) {
        }
    }
}
