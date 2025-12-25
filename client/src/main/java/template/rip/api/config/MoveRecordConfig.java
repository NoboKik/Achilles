package template.rip.api.config;

import me.sootysplash.bite.BiteMap;
import me.sootysplash.bite.TypeObject;
import template.rip.api.object.InputRecord;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static template.rip.api.config.ConfigManager.pathRecordFolder;

public class MoveRecordConfig {

    private final String name;
    private Path pathProfile;
    private BiteMap jsonProfile;

    public MoveRecordConfig(/*String name, Path pathProfile*/) {
//      this.name = name;
        name = "default";
//      this.pathProfile = pathProfile;
        pathProfile = ConfigManager.pathRecordFolder.resolve("default.ac");
    }

    public String getName() {
        return name;
    }

    public Path getPathProfile() {
        return pathProfile;
    }

    public MoveRecordConfig setPathProfile(Path pathProfile) {
        this.pathProfile = pathProfile;
        return this;
    }

    public HashMap<Integer, InputRecord> loadProfile() {
        HashMap<Integer, InputRecord> inputs = new HashMap<>();
        try {
            if (!Files.isRegularFile(pathProfile))
                return inputs;

            this.jsonProfile = BiteMap.fromBytes(Files.readAllBytes(pathProfile));

            int start = 0;
            TypeObject ir;
            while ((ir = jsonProfile.get(String.valueOf(start))) != null) {
                inputs.put(start, InputRecord.load(ir.getNest()));
                start++;
            }
        } catch (Exception e) {
//          e.printStackTrace();
        }
        return inputs;
    }

    public void saveProfile(HashMap<Integer, InputRecord> inputs) {
        try {
            Files.createDirectories(pathRecordFolder);

            boolean isProfileSaved = jsonProfile != null;

            if (!isProfileSaved) {
                jsonProfile = BiteMap.newInstance();

                jsonProfile.add("profileName", this.name);
            }

            int start = 0;
            InputRecord ir;
            while ((ir = inputs.get(start)) != null) {
                jsonProfile.add(String.valueOf(start), ir.toJSON());
                start++;
            }

            Files.write(pathProfile, jsonProfile.getBytes());
        } catch (Exception e) {
//          e.printStackTrace();
        }
    }
}
