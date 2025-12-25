package template.rip.gui.screens;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import me.sootysplash.bite.BiteMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import template.rip.Template;
import template.rip.api.config.ConfigManager;
import template.rip.api.event.events.KeyPressEvent;
import template.rip.api.event.events.MousePressEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.util.GuiUtils;
import template.rip.api.util.KeyUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.KeybindSetting;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

import static template.rip.Template.mc;
import static template.rip.api.config.ConfigManager.GSON;

public class LoginScreen implements Renderable {

    private static final LoginScreen instance;

    static {
        instance = new LoginScreen();
        System.setProperty("java.awt.headless", "false");
    }

    public enum LS {Butter, Cheese} //LoginState, Butter = false, Cheese = true

    public LS state = LS.Butter;
    public ImString login = new ImString();
    public ImBoolean rememberMe = null;
    long lastPress = 0;
    long lastException = 0;
    boolean showField = false;
    boolean authenticating = false;
    boolean randomBooleanThatDoesntLikeBeingSetToTrue = false;
    String hwID = null;
    JsonArray cPUS = null;
    boolean firstFrame = true;

    public static LoginScreen getInstance() {
        return instance;
    }

    public void toggleVisibility() {
        if (ImguiLoader.isRendered(getInstance())) {
            ImguiLoader.queueRemove(getInstance());
        } else {
            ImguiLoader.addRenderable(getInstance());
        }
    }

    public boolean isRendered() {
        return ImguiLoader.isRendered(getInstance());
    }

    @Override
    public String getName() {
        return "Login";
    }

    @Override
    public void render() {
        if (state.equals(LS.Cheese)) return;
        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDecoration;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;

        ImGui.getStyle().setFramePadding(12, 12);
        ImGui.getStyle().setButtonTextAlign(0.1f, 0.5f);
        ImGui.getStyle().setWindowPadding(0, 0);
        ImGui.getStyle().setWindowRounding(16f);
        ImGui.setNextWindowPos((float) mc.getWindow().getFramebufferWidth() / 2 - 300f / 2, (float) mc.getWindow().getFramebufferHeight() / 2 - 350f / 2);
        ImGui.setNextWindowSize(300f, 355f, 0);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.13f, 0.14f, 0.18f, 1f);
        ImGui.begin(getName(), imGuiWindowFlags);
        ImGui.popStyleColor(1);
        float posX = (float) (MinecraftClient.getInstance().getWindow().getWidth() / 2 - 490);
        float posY = (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2 - 250);

        if (firstFrame) {
            ImGui.setWindowPos(posX, posY);
            firstFrame = false;
        }

        float[] color = JColor.getGuiColor().getFloatColor();

        ImGui.pushFont(ImguiLoader.poppins48);
        ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1.00f);
        String a = "Ach";
        String c = "ill";
        String s = "es";
        float width = ImGui.calcTextSize(a).x;
        width += ImGui.calcTextSize(c).x;
        width += ImGui.calcTextSize(s).x;
        ImGui.setCursorPos(300f / 2f - width / 2f, 30f);
        ImGui.text(a);
        ImGui.sameLine(0, 0);
        ImGui.text(c);
        ImGui.sameLine(0, 0);
        ImGui.text(s);
        ImGui.popFont();
        ImGui.popStyleColor(1);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 8f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 12f, 12f);

        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0.15f, 0.17f, 0.22f, 0.5f);
        ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1.00f);
        ImGui.pushStyleColor(ImGuiCol.TextDisabled, 0.83f, 0.86f, 0.94f, 0.5f);
        ImGui.pushFont(ImguiLoader.poppins24);

        String w = "Welcome! Please login.";
        width = ImGui.calcTextSize(w).x;
        ImGui.setCursorPos(300f / 2f - width / 2f, 80f);
        ImGui.text(w);
        ImGui.popFont();

        String u = "Username";

        ImGui.pushFont(ImguiLoader.poppins24);

        ImGui.setNextItemWidth(200f);

        ImGui.setCursorPos(50f, 130f);

        ImGui.inputTextWithHint("##", u, login);

        //ImGui.setNextItemWidth(200f);
        //ImGui.setCursorPos(50f, 185f);
//
        //ImGui.inputTextWithHint("###", p, password, ImGuiInputTextFlags.Password);

        ImGui.setCursorPos(100f, 300f);
        ImGui.setNextItemWidth(100f);

        if (hwID == null || cPUS == null) {
            String hwid;
            JsonArray cpus = new JsonArray();

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(System.getProperty("os.name"));
            stringBuilder.append(System.getProperty("os.arch"));
            stringBuilder.append(System.getProperty("os.version"));

            try {
                Process process = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
                process.getOutputStream().close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line.trim());
                }
            } catch (Exception ignored) {
            }

            UUID uuid = UUID.nameUUIDFromBytes(stringBuilder.toString().getBytes());

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] key = digest.digest("ECRHRQV2XPH2BN3QF".getBytes(StandardCharsets.UTF_8));

                SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

                byte[] encryptedBytes = cipher.doFinal(uuid.toString().getBytes(StandardCharsets.UTF_8));

                hwid = Base64.getEncoder().encodeToString(encryptedBytes);
            } catch (Exception e) {
                hwid = null;
            }

            try {
                Process process = Runtime.getRuntime().exec("systeminfo");
                process.getOutputStream().close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                ArrayList<String> lines = new ArrayList<>(50);
                while ((line = reader.readLine()) != null) {
                    lines.add(line.trim());
                }

                ArrayList<String> processors = new ArrayList<>();
                String p = "Processor(s):";
                String b = "BIOS Version:";
                boolean inProcessSection = false;

                for (String lined : lines) {
                    if (lined.startsWith(b)) {
                        inProcessSection = false;
                    }
                    if (inProcessSection) {
                        String trimmed = lined.substring(5).trim();
                        processors.add(trimmed);
                    }
                    if (lined.startsWith(p)) {
                        inProcessSection = true;
                    }
                }

                for (String pc : processors) {
                    cpus.add(pc);
                }
            } catch (Throwable e) {
                try {

                    String fileContents = Files.readString(Paths.get("/proc/cpuinfo"));
                    String mn = "model name\t:";
                    HashSet<String> processorCores = new HashSet<>();
                    for (String line : fileContents.split("\n")) {
                        if (line.startsWith(mn)) {
                            processorCores.add(line.substring(mn.length() + 1).trim());
                        }
                    }

                    for (String pc : processorCores) {
                        cpus.add(pc);
                    }
                } catch (Throwable ex) {
                    cpus.add("Unknown cpu=" + ex.getLocalizedMessage());
                }
            }

            cPUS = cpus;
            hwID = hwid;
        } else {

            ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0f, 0f, 0f, 0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0f, 0f, 0f, 0f);

            if (!showField) {
                String text = System.currentTimeMillis() - lastException < 2500 ? "Error! Use Fallback!" : (System.currentTimeMillis() - lastPress < 2500 ? "Copied!" : "Copy HWID");
                ImGui.setCursorPos(300f / 2 - ImGui.calcTextSize(text).x / 2, 285f);
                if (ImGui.button("##CopyHWID", ImGui.calcTextSize(text).x, ImGui.calcTextSize(text).y)) {
                    try {
                        GLFW.glfwSetClipboardString(0, hwID);
                        lastPress = System.currentTimeMillis();
                    } catch (Exception e) {
                        lastException = System.currentTimeMillis();
                    }
                }
                if (ImGui.isItemHovered())
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.2f);
                else if (ImGui.isItemHovered())
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.3f);
                else
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.4f);
                ImGui.setCursorPos(300f / 2 - ImGui.calcTextSize(text).x / 2, 285f);
                ImGui.text(text);
                ImGui.popStyleColor(1);

                text = "Fallback";

                ImGui.setCursorPos(300f / 2 - ImGui.calcTextSize(text).x / 2, 315f);
                if (ImGui.button("##Fallback", ImGui.calcTextSize(text).x, ImGui.calcTextSize(text).y)) {
                    showField = true;
                }
                if (ImGui.isItemHovered())
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.2f);
                else if (ImGui.isItemHovered())
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.3f);
                else
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.4f);
                ImGui.setCursorPos(300f / 2 - ImGui.calcTextSize(text).x / 2, 315f);
                ImGui.text(text);
                ImGui.popStyleColor(1);
            } else {
                ImGui.setCursorPos(50f, 275f);
                ImString is = new ImString(hwID);
                ImGui.setNextItemWidth(200f);
                ImGui.inputText("##HWID", is);
            }

            ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.7f);
            ImGui.setCursorPos(50f, 185f);
            String rmb = "Remember Me";
            ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 1f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.8f);
            ImGui.pushStyleColor(ImGuiCol.WindowBg, color[0], color[1], color[2], 0.7f);

            if (rememberMe == null) {
                try {
                    Path remember = ConfigManager.pathConfigFolder.resolve("login.json");
                    if (!Files.isRegularFile(remember))
                        throw new RuntimeException();

                    String read = new String(Files.readAllBytes(remember), StandardCharsets.UTF_8);

                    if (read.isEmpty())
                        throw new RuntimeException();

                    String decode = Template.decrypt(read, "J50n1Mag3");
//                    System.out.println(decode);
                    while (decode.charAt(decode.length() - 1) == '1') {
                        decode = decode.substring(0, decode.length() - 1);
                    }
//                    System.out.println(decode);
                    JsonObject prof = GSON.fromJson(decode, JsonObject.class);

                    if (prof.get("true") == null)
                        throw new RuntimeException();

                    login.set(prof.get("true").getAsString());
                    rememberMe = new ImBoolean(true);
                } catch (Exception ignored) {
                    rememberMe = new ImBoolean();
                }
            }

            float framePaddingY = ImGui.getStyle().getFramePaddingY();
            ImGui.getStyle().setFramePadding(ImGui.getStyle().getFramePaddingX(), 0.2f);
            ImGui.checkbox("##" + rmb, rememberMe);
            ImGui.getStyle().setFramePadding(ImGui.getStyle().getFramePaddingX(), framePaddingY);
            ImGui.pushFont(ImguiLoader.fontAwesome12);
            ImGui.setCursorPos(80f, 187.5f);
            ImGui.text(rmb);
            ImGui.popFont();
            ImGui.popStyleColor(7);
        }

        ImGui.setCursorPos(50f, 215f);
        ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
        ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 1f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.8f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.7f);
        ImGui.pushFont(ImguiLoader.poppins32);
        String l = "Login";
        if (ImGui.button(l, 200f, 50f)) {
            new LoginThread(this);
        }
//        if (true) {
//            login();
//        }
        ImGui.popFont();
        ImGui.getStyle().setButtonTextAlign(0.1f, 0.5f);
        ImGui.popStyleColor(6);
        ImGui.popFont();
        ImGui.popStyleVar(2);

        GuiUtils.drawWindowShadow(ImGui.getColorU32(0f, 0f, 0f, 0.4f),
                ImGui.getColorU32(0f, 0f, 0f, 0f),
                5f);

        ImGui.end();
    }

    /*@EventHandler
    private void onFastTick(FastTickEvent event) {
        if (time < System.currentTimeMillis() && isLoggedIn && !loggedOut) {
            long keepAliveDelay = 60000;
            time = System.currentTimeMillis() + keepAliveDelay;
//            time = System.currentTimeMillis() + 5000;
            Thread thr = new Thread(() -> {

                // ATTENTION! Comment out the if statement below BEFORE releasing!
                *//*if (isDev) {
                    return;
                }*//*

                String hwid;
                int port = 800;
                // http://www.java2s.com/Tutorials/Java/Network/HTTP/Send_a_POST_Request_Using_a_Socket_in_Java.htm
                for (int t = 0; t < 1; t++) {
                    try {

                        StringBuilder stringBuilder = new StringBuilder();

                        stringBuilder.append(System.getProperty("os.name"));
                        stringBuilder.append(System.getProperty("os.arch"));
                        stringBuilder.append(System.getProperty("os.version"));

                        try {
                            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                            while (networkInterfaces.hasMoreElements()) {
                                NetworkInterface networkInterface = networkInterfaces.nextElement();
                                stringBuilder.append(networkInterface.toString());
                            }
                        } catch (Exception ignored) {
                        }
                        try {
                            InetAddress inetAddress = InetAddress.getLocalHost();
                            stringBuilder.append(inetAddress.getHostAddress());
                        } catch (Exception ignored) {
                        }
                        try {
                            Process process = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
                            process.getOutputStream().close();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line.trim());
                            }
                        } catch (Exception ignored) {
                        }

                        UUID uuid = UUID.nameUUIDFromBytes(stringBuilder.toString().getBytes());

                        try {
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            byte[] key = digest.digest("ECRHRQV2XPH2BN3QF".getBytes(StandardCharsets.UTF_8));

                            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

                            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

                            byte[] encryptedBytes = cipher.doFinal(uuid.toString().getBytes(StandardCharsets.UTF_8));

                            hwid = Base64.getEncoder().encodeToString(encryptedBytes);
                        } catch (Exception e) {
                            hwid = "";
                        }

                        String desktop = "Unresolved";
                        try {
                            desktop = InetAddress.getLocalHost().getHostName();
                        } catch (Exception ignored) {
                        }
                        String params = URLEncoder.encode("uuid", StandardCharsets.UTF_8)
                                + "=" + URLEncoder.encode(String.format("%s:%s", mc.getSession().getUsername(), mc.getSession().getUuid()), StandardCharsets.UTF_8);
                        params += "&" + URLEncoder.encode("username", StandardCharsets.UTF_8)
                                + "=" + URLEncoder.encode(login.get(), StandardCharsets.UTF_8);
                        params += "&" + URLEncoder.encode("hwid", StandardCharsets.UTF_8)
                                + "=" + URLEncoder.encode(hwid, StandardCharsets.UTF_8);
                        params += "&" + URLEncoder.encode("desktopName", StandardCharsets.UTF_8)
                                + "=" + URLEncoder.encode(desktop, StandardCharsets.UTF_8);

                        Socket socket = new Socket("92.63.104.240", port);
                        String path = "/hwid";

                        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()/*, StandardCharsets.UTF_8));
                        wr.write("POST " + path + " HTTP/1.0\r\n");
                        wr.write("Content-Length: " + params.length() + "\r\n");
                        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
                        wr.write("\r\n");

                        wr.write(params);
                        wr.flush();

                        BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String str = null;
                        String line;

                        while ((line = rd.readLine()) != null) {
                            str = line;
                        }


                        if (new URL("https://currentmillis.com/time/minutes-since-unix-epoch.php").getContent() instanceof InputStream is) {
                            String time = new BufferedReader(new InputStreamReader(is)).readLine();
                            time = time.substring(time.length() - 6, time.length() - 1);
                            String key = time.concat("Y?N+BL;4mx'%[>Ce@W");

                            str = Template.decrypt(str, key);
                            if ("HWID exists and matches".equals(Template.decrypt(str, key)) && !invalid) {
                                i = 0;
                                break;
                            }
                        }

                        wr.close();
                        rd.close();
                    } catch (Exception ignored) {
                    }
                    i++;


                }

                if (i > 5 && !loggedOut) {
                    Template.notificationManager().addNotification(new Notification("You have been logged out!", "Failed to authenticate after multiple attempts", keepAliveDelay / 6, Notification.Type.ERROR));
                    loggedOut = true;
                    try {
                        Thread.sleep(keepAliveDelay / 6);
                    } catch (InterruptedException ignored) {
                    }
                    AchillesMenu.stopClient();
                }

            });
            thr.start();

        }
    }*/



    @EventHandler
    private void onClick(MousePressEvent event) {
        onKeyPress(new KeyPressEvent(event.button, 0, event.action, 0));
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent event) {
        // copy to prevent async weirdness
        handlePress(event);
    }

    private void handlePress(KeyPressEvent event) {
        if (event.key == 0) {
            return;
        }

        if (!AchillesMenu.isClientEnabled()) {
            return;
        }

        Screen current = MinecraftClient.getInstance().currentScreen;
        if (current instanceof CreativeInventoryScreen) {
            return;
        }

        if (current != null && current.getFocused() instanceof TextFieldWidget) {
            return;
        }

        if (Template.moduleManager.binding) {
            return;
        }

        if (Template.moduleManager.typing && Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class)) {
            return;
        }

        if (KeyUtils.isKeyPressed(GLFW.GLFW_KEY_F3)) {
            return;
        }

        boolean keyPress = event.action == GLFW.GLFW_PRESS;
        if (state.equals(LS.Butter)) {
            try {
                Path defaultConfigPath = Paths.get(System.getProperty("user.home"))
                        .resolve("template")
                        .resolve("profiles")
                        .resolve("default.ac");

                if (Files.isRegularFile(defaultConfigPath)) {

                    BiteMap map = BiteMap.fromBytes(Files.readAllBytes(defaultConfigPath));

                    if (map.size() == 0) return;

                    BiteMap sm = map.get("Aie").getNest().get(AchillesSettingsModule.loginBind.getName()).getNest();
                    int loginBind = sm.get("keyCode").getInteger();

                    AchillesSettingsModule.loginBind.setKeyCode(loginBind);
                }
            } catch (Exception ignored) {
            }

            if (event.key == AchillesSettingsModule.loginBind.getCode() && keyPress) {
                toggleVisibility();
            }
            return;
        }

        TreeSet<Module> modules = Template.moduleManager.getModules();

        modules.stream().filter(m -> m.getKey() == event.key && (m.isHold() || keyPress)).forEach(Module::toggle);
        modules.forEach(m -> {
            if (m.isFocused) {
                if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) {
                        m.updatedPos.x = 5;
                    } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) {
                        m.updatedPos.x = -5;
                    } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_UP)) {
                        m.updatedPos.y = -5;
                    } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) {
                        m.updatedPos.y = 5;
                    }
                } else {
                    if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) {
                        m.updatedPos.x = 1;
                    } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) {
                        m.updatedPos.x = -1;
                    } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_UP)) {
                        m.updatedPos.y = -1;
                    } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) {
                        m.updatedPos.y = 1;
                    }
                }
            }
            if (m.isEnabled()) {
                m.settings.stream().filter(s -> s instanceof KeybindSetting k && k.getCode() == event.key).forEach(s -> ((KeybindSetting) s).onPress(keyPress));
            }
        });
    }

    private boolean overlay() {
        Overlay ov = mc.getOverlay();
        return ov instanceof SplashOverlay;
    }

    @Override
    public Theme getTheme() {
        return theme;
    }

    private final Theme theme = new Theme() {

        @Override
        public void preRender() {
            float[][] colors = ImGui.getStyle().getColors();

            float[] color = JColor.getGuiColor().getFloatColor();
            float[] bColor = JColor.getGuiColor().jBrighter().getFloatColor();
            float[] dColor = JColor.getGuiColor().jDarker().getFloatColor();

            colors[ImGuiCol.Text] = new float[]{0.83f, 0.86f, 0.94f, 1.00f};
            colors[ImGuiCol.TextDisabled] = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
            colors[ImGuiCol.WindowBg] = new float[]{0.13f, 0.14f, 0.18f, 1f};
            colors[ImGuiCol.ChildBg] = new float[]{0.08f, 0.09f, 0.14f, 1.00f};
            colors[ImGuiCol.PopupBg] = new float[]{0.13f, 0.14f, 0.19f, 0.94f};
            colors[ImGuiCol.Border] = new float[]{0.21f, 0.24f, 0.31f, 1.00f};
            colors[ImGuiCol.BorderShadow] = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
            colors[ImGuiCol.FrameBg] = new float[]{color[0], color[1], color[2], 0.54f};
            colors[ImGuiCol.FrameBgHovered] = new float[]{color[0], color[1], color[2], 0.40f};
            colors[ImGuiCol.FrameBgActive] = new float[]{color[0], color[1], color[2], 0.67f};
            colors[ImGuiCol.TitleBg] = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
            colors[ImGuiCol.TitleBgActive] = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
            colors[ImGuiCol.TitleBgCollapsed] = new float[]{0.13f, 0.14f, 0.19f, 0.5f};
            colors[ImGuiCol.MenuBarBg] = new float[]{0.13f, 0.14f, 0.19f, 1.00f};
            colors[ImGuiCol.ScrollbarBg] = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
            colors[ImGuiCol.ScrollbarGrab] = new float[]{0.18f, 0.21f, 0.27f, 0.00f};
            colors[ImGuiCol.ScrollbarGrabHovered] = new float[]{0.22f, 0.25f, 0.33f, 0.00f};
            colors[ImGuiCol.ScrollbarGrabActive] = new float[]{0.25f, 0.29f, 0.37f, 0.00f};
            colors[ImGuiCol.CheckMark] = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};

            colors[ImGuiCol.SliderGrab] = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.SliderGrabActive] = new float[]{color[0], color[1], color[2], 0.95f};
            colors[ImGuiCol.Button] = new float[]{color[0], color[1], color[2], 0.59f};
            colors[ImGuiCol.ButtonHovered] = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.ButtonActive] = new float[]{color[0], color[1], color[2], 1.00f};
            colors[ImGuiCol.Header] = new float[]{color[0], color[1], color[2], 0.9f};
            colors[ImGuiCol.HeaderHovered] = new float[]{color[0], color[1], color[2], 0.95f};

            colors[ImGuiCol.HeaderActive] = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
            colors[ImGuiCol.Separator] = new float[]{0.18f, 0.21f, 0.27f, 1.00f};
            colors[ImGuiCol.SeparatorHovered] = new float[]{0.81f, 0.25f, 0.33f, 1.00f};
            colors[ImGuiCol.SeparatorActive] = new float[]{0.74f, 0.22f, 0.30f, 1.00f};

            colors[ImGuiCol.ResizeGrip] = new float[]{color[0], color[1], color[2], 0.59f};
            colors[ImGuiCol.ResizeGripHovered] = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
            colors[ImGuiCol.ResizeGripActive] = new float[]{color[0], color[1], color[2], 1.00f};
            colors[ImGuiCol.Tab] = new float[]{dColor[0], dColor[1], dColor[2], 0.86f};
            colors[ImGuiCol.TabHovered] = new float[]{color[0], color[1], color[2], 0.80f};
            colors[ImGuiCol.TabActive] = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};

            colors[ImGuiCol.TabUnfocused] = new float[]{0.15f, 0.18f, 0.25f, 1.00f};
            colors[ImGuiCol.TabUnfocusedActive] = new float[]{0.56f, 0.21f, 0.26f, 0.67f};
            colors[ImGuiCol.DockingPreview] = new float[]{0.91f, 0.26f, 0.36f, 0.67f};
            colors[ImGuiCol.DockingEmptyBg] = new float[]{0.20f, 0.20f, 0.20f, 1.00f};
            colors[ImGuiCol.PlotLines] = new float[]{0.61f, 0.61f, 0.61f, 1.00f};
            colors[ImGuiCol.PlotLinesHovered] = new float[]{1.00f, 0.43f, 0.35f, 1.00f};
            colors[ImGuiCol.PlotHistogram] = new float[]{0.90f, 0.70f, 0.00f, 1.00f};
            colors[ImGuiCol.PlotHistogramHovered] = new float[]{1.00f, 0.60f, 0.00f, 1.00f};
            colors[ImGuiCol.TableHeaderBg] = new float[]{0.19f, 0.19f, 0.20f, 1.00f};
            colors[ImGuiCol.TableBorderStrong] = new float[]{0.31f, 0.31f, 0.35f, 1.00f};
            colors[ImGuiCol.TableBorderLight] = new float[]{0.23f, 0.23f, 0.25f, 1.00f};
            colors[ImGuiCol.TableRowBg] = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
            colors[ImGuiCol.TableRowBgAlt] = new float[]{1.00f, 1.00f, 1.00f, 0.06f};
            colors[ImGuiCol.TextSelectedBg] = new float[]{0.26f, 0.59f, 0.98f, 0.35f};
            colors[ImGuiCol.DragDropTarget] = new float[]{1.00f, 1.00f, 0.00f, 0.90f};
            colors[ImGuiCol.NavHighlight] = new float[]{0.26f, 0.59f, 0.98f, 1.00f};
            colors[ImGuiCol.NavWindowingHighlight] = new float[]{1.00f, 1.00f, 1.00f, 0.70f};
            colors[ImGuiCol.NavWindowingDimBg] = new float[]{0.80f, 0.80f, 0.80f, 0.20f};
            colors[ImGuiCol.ModalWindowDimBg] = new float[]{0.80f, 0.80f, 0.80f, 0.35f};
            ImGui.getStyle().setColors(colors);

            ImGui.getStyle().setWindowRounding(8);
            ImGui.getStyle().setFrameRounding(4);
            ImGui.getStyle().setGrabRounding(4);
            ImGui.getStyle().setPopupRounding(4);
            ImGui.getStyle().setScrollbarRounding(4);
            ImGui.getStyle().setTabRounding(4);
            ImGui.getStyle().setWindowTitleAlign(0.5f, 0.5f);
            ImGui.getStyle().setScrollbarSize(1);

            if (ImguiLoader.fontAwesome16 != null) {
                ImGui.pushFont(ImguiLoader.fontAwesome16);
            }
        }

        @Override
        public void postRender() {
            if (ImguiLoader.fontAwesome16 != null) {
                ImGui.popFont();
            }
        }
    };
}