package template.rip.gui.windowgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.ToolTipHolder;
import template.rip.api.util.*;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.LegitMenu;
import template.rip.gui.utils.Renderable;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;

import java.util.ArrayList;

public class ModulesMenu implements Renderable {

    private static ModulesMenu instance;
    public Module.Category selectedCategory = Module.Category.COMBAT;
    public float scrollY = 0;
    public float scrollUntil = 0;

    public static ModulesMenu getInstance() {
        if (instance == null) {
            instance = new ModulesMenu();
        }
        return instance;
    }

    public static void toggleVisibility() {
        if (ImguiLoader.isRendered(getInstance())) {
            ImguiLoader.queueRemove(getInstance());
        } else {
            ImguiLoader.addRenderable(getInstance());
        }
    }

    @Override
    public String getName() {
        return Template.name;
    }

    @Override
    public void render() {
        pre();
        if (LegitMenu.getInstance().isOn) return;

        // Render other menus
        MainMenu.getInstance().render();
        LegitModulesMenu.getInstance().render();
        ConfigMenu.getInstance().render();

        if (!MainMenu.getInstance().selectedSection.equals("\uF0CA Modules")) return;

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoCollapse;
        //imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
        ImGui.getStyle().setFramePadding(4, 6);
        ImGui.getStyle().setButtonTextAlign(0.05f, 0.5f);
        ImGui.getStyle().setWindowPadding(16, 16);
        ImGui.getStyle().setWindowRounding(16f);
        ImGui.setNextWindowSize(630f, 430f, 0);
        ImGui.begin(getName(), imGuiWindowFlags);

        ImGui.getStyle().setWindowRounding(4f);
        ImGui.getStyle().setWindowPadding(6, 6);
        ImGui.getStyle().setFramePadding(0f, 0f);
        ImGui.getStyle().setCellPadding(0f, 0f);
        ImGui.getStyle().setItemSpacing(8, 4);
        ImGui.getStyle().setItemInnerSpacing(4, 4);
        ImGui.getStyle().setWindowBorderSize(1f);
        ImGui.getStyle().setFrameBorderSize(0f);

        //float posX = (float) (MinecraftClient.getInstance().getWindow().getWidth() / 2 - 330);
        //float posY = (float) (MinecraftClient.getInstance().getWindow().getHeight() / 2 - 250);
        float posX = MainMenu.getInstance().getPos().x + 800 - 630;
        float posY = MainMenu.getInstance().getPos().y + 500 - 430;
        ImGui.setWindowPos(posX, posY);

        if (scrollUntil > ImGui.getScrollMaxY()) {
            scrollUntil = ImGui.getScrollMaxY();
        } else if (scrollUntil < 0) {
            scrollUntil = 0;
        }

        scrollY = (float) (scrollY + 0.2 * (scrollUntil - scrollY));
        ImGui.setScrollY(scrollY);

        ImGui.setCursorPos(15, 60);

        //ImGui.columns(2, "CategoriesColumns", false);
        //ImGui.setColumnOffset(1, 320);
        float leftX = ImGui.getCursorPosX();
        float rightX = ImGui.getCursorPosX() + 293 + 10;
        float initY = ImGui.getCursorPosY();

        ArrayList<Module> left = new ArrayList<>();
        ArrayList<Module> right = new ArrayList<>();

        ArrayList<Module> all = new ArrayList<>();

        int i = 0;
        if (selectedCategory != Module.Category.ALL) {
            for (Module module : Template.moduleManager.getModulesByCategory(selectedCategory)) {
                if (i % 2 == 0) left.add(module);
                if (i % 2 == 1) right.add(module);

                all.add(module);
                i++;
            }
        } else {
            for (Module module : Template.moduleManager.getModules()) {
                if (i % 2 == 0) left.add(module);
                if (i % 2 == 1) right.add(module);

                all.add(module);
                i++;
            }
        }
        ImGui.setCursorPosY(initY);
        AchillesSettingsModule m = Template.moduleManager.getModule(AchillesSettingsModule.class);
        if (false/*m.menuModulesMode.is(AchillesSettingsModule.menuModulesModeEnum.Tabs)*/) {
            for (Module module : left) {
                ImGui.pushID(module.getName());
                ImGui.setCursorPosX(leftX);

                if (module.isEnabled()) {
                    //float[] color = JColor.getGuiColor().getFloatColor();
                    //float[] dColor = JColor.getGuiColor().jDarker().getFloatColor();
//
                    //ImGui.pushStyleColor(ImGuiCol.Button, dColor[0], dColor[1], dColor[2], 0.50f);
                    //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.65f);
                    //ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.75f);
                    ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.8f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.9f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 1.0f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.3f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.4f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 0.5f);
                }

                ImGui.pushFont(ImguiLoader.poppins24);
                ImVec2 prevPos = ImGui.getCursorPos();
                boolean isToggled = ImGui.button("##", 296f, 50f);
                ImVec2 postPos = ImGui.getCursorPos();
                ImGui.popStyleColor(3);
                ImGui.popFont();

                if (isToggled) {
                    module.toggle();
                }

                if (ImGui.isItemHovered()) {
                    ToolTipHolder.setToolTip(module.getDescription().getContent());

                    if (ImGui.isMouseClicked(1)) {
                        module.toggleShowOptions();
                    }
                }

                if (module.isEnabled()) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.84f, 0.96f, 1.00f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.42f, 0.44f, 0.53f, 1.00f);
                }

                ImGui.pushFont(ImguiLoader.poppins24);
                float textY = RenderUtils.calcTextSize(module.getFullName()).y;
                ImGui.setCursorPos(prevPos.x + 51, prevPos.y + 50f / 2 - textY / 2);
                RenderUtils.drawTexts(module.getFullName());
                ImGui.setCursorPos(postPos.x, postPos.y);
                ImGui.popFont();
                ImGui.popStyleColor(1);

                if (module.isEnabled()) {
                    float[] color = JColor.getGuiColor().getFloatColor();
                    ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 0.5f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.5f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.5f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Button, 0.14f, 0.16f, 0.21f, 1.0f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.14f, 0.16f, 0.21f, 1.0f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.14f, 0.16f, 0.21f, 1.0f);
                }
                ImGui.setCursorPos(prevPos.x + 8, prevPos.y + 8);
                ImGui.button("##", 35f, 35f);
                ImGui.setCursorPos(postPos.x, postPos.y);

                ImGui.popStyleColor(3);

                if (module.isEnabled()) {
                    float[] color = JColor.getGuiColor().getFloatColor();
                    ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 1f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 1f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 1f);
                    ImGui.getStyle().setFrameRounding(10f - 3);

                    ImGui.setCursorPos(prevPos.x + 8 + 3, prevPos.y + 8 + 3);
                    ImGui.button("##", 35f - 3 * 2, 35f - 3 * 2);
                    ImGui.setCursorPos(postPos.x, postPos.y);

                    ImGui.getStyle().setFrameRounding(10f);
                    ImGui.popStyleColor(3);
                }

                //if (module.isEnabled()) {
                //    float[] color = JColor.getGuiColor().getFloatColor();
//
                //    ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 1f);
                //    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 1f);
                //    ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 1f);
//
                //    ImGui.setCursorPos(prevPos.x + 10, prevPos.y + 10);
                //    ImGui.button("##", 31f, 31f);
                //    ImGui.setCursorPos(postPos.x, postPos.y);
//
                //    ImGui.popStyleColor(3);
                //}

                if (module.showOptions()) {
                    ImGui.indent(4f + 9f);
                    ImGui.pushFont(ImguiLoader.poppins18);
                    ImGui.getStyle().setFrameRounding(4f);
                    ImGui.getStyle().setFramePadding(4, 4);
                    ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
                    //ImGui.beginChild(module.getName()+"/SettingsCalc", 1, 1, false, ImGuiWindowFlags.AlwaysAutoResize);
                    //float Y = ImGui.getCursorPosY();
                    //module.renderSettings();
                    //float nextY = ImGui.getCursorPosY()-Y;
                    //ImGui.endChild();

                    float settingsHeight = module.getSettingsHeight2();

                    ImGui.setCursorPosY(ImGui.getCursorPosY() + 7f);
                    ImGui.getWindowDrawList().addRectFilled(
                            ImGui.getWindowPosX() + prevPos.x + 4f,
                            ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY() - 4f,
                            ImGui.getWindowPosX() + prevPos.x + 296f - 4f,
                            ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY() - 14f + 15 + settingsHeight,
                            ImGui.getColorU32(0.16f, 0.18f, 0.24f, 0.3f), 10f
                    );
                    module.renderSettings();
                    ImGui.getStyle().setButtonTextAlign(0.05f, 0.5f);
                    ImGui.getStyle().setFramePadding(4, 4);
                    ImGui.getStyle().setFrameRounding(10f);
                    ImGui.popFont();
                    ImGui.unindent(4f + 9f);
                    ImGui.setCursorPosY(ImGui.getCursorPosY() + 10);
                }
                ImGui.setCursorPosY(ImGui.getCursorPosY() + 3);
                ImGui.popID();
            }

            ImGui.setCursorPosY(initY);
            for (Module module : right) {
                ImGui.pushID(module.getName());
                ImGui.setCursorPosX(rightX);

                if (module.isEnabled()) {
                    //float[] color = JColor.getGuiColor().getFloatColor();
                    //float[] dColor = JColor.getGuiColor().jDarker().getFloatColor();
//
                    //ImGui.pushStyleColor(ImGuiCol.Button, dColor[0], dColor[1], dColor[2], 0.50f);
                    //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.65f);
                    //ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.75f);
                    ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.8f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.9f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 1.0f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.3f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.4f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 0.5f);
                }

                ImGui.pushFont(ImguiLoader.poppins24);
                ImVec2 prevPos = ImGui.getCursorPos();
                boolean isToggled = ImGui.button("##", 296f, 50f);
                ImVec2 postPos = ImGui.getCursorPos();
                ImGui.popStyleColor(3);
                ImGui.popFont();

                if (isToggled) {
                    module.toggle();
                }

                if (ImGui.isItemHovered()) {
                    ToolTipHolder.setToolTip(module.getDescription().getContent());

                    if (ImGui.isMouseClicked(1)) {
                        module.toggleShowOptions();
                    }
                }

                if (module.isEnabled()) {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.84f, 0.96f, 1.00f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.42f, 0.44f, 0.53f, 1.00f);
                }

                ImGui.pushFont(ImguiLoader.poppins24);
                float textY = RenderUtils.calcTextSize(module.getFullName()).y;
                ImGui.setCursorPos(prevPos.x + 51, prevPos.y + 50f / 2 - textY / 2);
                RenderUtils.drawTexts(module.getFullName());
                ImGui.setCursorPos(postPos.x, postPos.y);
                ImGui.popFont();
                ImGui.popStyleColor(1);

                if (module.isEnabled()) {
                    float[] color = JColor.getGuiColor().getFloatColor();
                    ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 0.5f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.5f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.5f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Button, 0.14f, 0.16f, 0.21f, 1.0f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.14f, 0.16f, 0.21f, 1.0f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.14f, 0.16f, 0.21f, 1.0f);
                }

                ImGui.setCursorPos(prevPos.x + 8, prevPos.y + 8);
                ImGui.button("##", 35f, 35f);
                ImGui.setCursorPos(postPos.x, postPos.y);

                ImGui.popStyleColor(3);

                if (module.isEnabled()) {
                    float[] color = JColor.getGuiColor().getFloatColor();
                    ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 1f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 1f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 1f);
                    ImGui.getStyle().setFrameRounding(10f - 3);

                    ImGui.setCursorPos(prevPos.x + 8 + 3, prevPos.y + 8 + 3);
                    ImGui.button("##", 35f - 3 * 2, 35f - 3 * 2);
                    ImGui.setCursorPos(postPos.x, postPos.y);

                    ImGui.getStyle().setFrameRounding(10f);
                    ImGui.popStyleColor(3);
                }

                //if (module.isEnabled()) {
                //    float[] color = JColor.getGuiColor().getFloatColor();
//
                //    ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 1f);
                //    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 1f);
                //    ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 1f);
//
                //    ImGui.setCursorPos(prevPos.x + 10, prevPos.y + 10);
                //    ImGui.button("##", 31f, 31f);
                //    ImGui.setCursorPos(postPos.x, postPos.y);
//
                //    ImGui.popStyleColor(3);
                //}

                if (module.showOptions()) {
                    ImGui.indent(310 + 4f + 9f);
                    ImGui.pushFont(ImguiLoader.poppins18);
                    ImGui.getStyle().setFrameRounding(4f);
                    ImGui.getStyle().setFramePadding(4, 4);
                    ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);
                    //ImGui.beginChild(module.getName()+"/SettingsCalc", 1, 1, false, ImGuiWindowFlags.AlwaysAutoResize);
                    //float Y = ImGui.getCursorPosY();
                    //module.renderSettings();
                    //float nextY = ImGui.getCursorPosY()-Y;
                    //ImGui.endChild();

                    float settingsHeight = module.getSettingsHeight2();

                    ImGui.setCursorPosY(ImGui.getCursorPosY() + 7f);
                    ImGui.getWindowDrawList().addRectFilled(
                            ImGui.getWindowPosX() + prevPos.x + 4f,
                            ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY() - 4f,
                            ImGui.getWindowPosX() + prevPos.x + 296f - 4f,
                            ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY() - 14f + 15 + settingsHeight,
                            ImGui.getColorU32(0.16f, 0.18f, 0.24f, 0.3f), 10f
                    );
                    module.renderSettings();
                    ImGui.getStyle().setButtonTextAlign(0.05f, 0.5f);
                    ImGui.getStyle().setFramePadding(4, 6);
                    ImGui.getStyle().setFrameRounding(10f);
                    ImGui.popFont();
                    ImGui.unindent(310 + 4f + 9f);
                    ImGui.setCursorPosY(ImGui.getCursorPosY() + 10);
                }
                ImGui.setCursorPosY(ImGui.getCursorPosY() + 3);
                ImGui.popID();
            }
        } else if (true /*m.menuModulesMode.is(AchillesSettingsModule.menuModulesModeEnum.Rows)*/) {
            for (Module module : all) {
                ImGui.pushID(module.getName());
                ImGui.setCursorPosX(leftX);

                if (module.isEnabled()) {
                    //float[] color = JColor.getGuiColor().getFloatColor();
                    //float[] dColor = JColor.getGuiColor().jDarker().getFloatColor();
//
                    //ImGui.pushStyleColor(ImGuiCol.Button, dColor[0], dColor[1], dColor[2], 0.50f);
                    //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.65f);
                    //ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.75f);
                    ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.8f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.9f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 1.0f);
                } else {
                    ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.3f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.4f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 0.5f);
                }

                ImGui.pushFont(ImguiLoader.poppins24);
                ImVec2 prevPos = ImGui.getCursorPos();
                boolean isToggled = ImGui.button("##", 600f, 60f);
                ImVec2 postPos = ImGui.getCursorPos();
                ImGui.popStyleColor(3);
                ImGui.popFont();

                if (isToggled && ImGui.getMousePosY() > ImGui.getWindowPosY() + 50) {
                    module.toggle();
                    AnimationUtil.hookPress(String.format("Module/%s", module.getName()), module.isEnabled());
                }

                if (ImGui.isItemHovered()) {
                    ToolTipHolder.setToolTip(module.getDescription().getContent());

                    if (ImGui.isMouseClicked(1)) {
                        module.toggleShowOptions();
                    }
                    AnimationUtil.hookHover(String.format("Module/%s", module.getName()), true);
                } else {
                    AnimationUtil.hookHover(String.format("Module/%s", module.getName()), false);
                }

                if (module.isEnabled()) {
                    //ImGui.pushStyleColor(ImGuiCol.Text, 0.80f, 0.84f, 0.96f, 1.00f);
                    float[] color = UI.blendColors(new JColor(0.42f, 0.44f, 0.53f, 1.00f),
                            new JColor(0.80f, 0.84f, 0.96f, 1.00f),
                            EasingUtil.easeOutSine(AnimationUtil.getRawPressPercentage(String.format("Module/%s", module.getName()), 150))
                    ).getFloatColorWAlpha();
                    ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
                } else {
                    //ImGui.pushStyleColor(ImGuiCol.Text, 0.42f, 0.44f, 0.53f, 1.00f);
                    float[] color = UI.blendColors(new JColor(0.80f, 0.84f, 0.96f, 1.00f),
                            new JColor(0.42f, 0.44f, 0.53f, 1.00f),
                            EasingUtil.easeInSine(AnimationUtil.getRawPressPercentage(String.format("Module/%s", module.getName()), 150))
                    ).getFloatColorWAlpha();
                    ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1f);
                }

                ImGui.pushFont(ImguiLoader.poppins24);
                float textY = RenderUtils.calcTextSize(module.getFullName()).y;
                ImGui.setCursorPos(prevPos.x + 60, prevPos.y + 40f / 2 - textY / 2);
                RenderUtils.drawTexts(module.getFullName());
                ImGui.setCursorPos(postPos.x, postPos.y);
                ImGui.popFont();
                ImGui.popStyleColor(1);

                ImGui.pushFont(ImguiLoader.poppins18);
                ImGui.pushStyleColor(ImGuiCol.Text, 0.32f, 0.34f, 0.43f, 1.00f);
                ImGui.setCursorPos(prevPos.x + 60, prevPos.y + 83f / 2 - textY / 2);

                float preCalc = ImGui.calcTextSize(" ").y;
                float startX = ImGui.getCursorPosX();
                float startY = ImGui.getCursorPosY();
                String[] strs = module.getDescription().getContent();
                for (int k = 0; k < strs.length; k++) {
                    String s = strs[k];
                    int newLineIndex = s.indexOf("\n");
                    String[] c = s.split("\n");
                    if (c.length == 1) {
                        if (newLineIndex == 0) {
                            startY += preCalc;
                            ImGui.text("\n");
                            ImGui.setCursorPos(startX, startY);
                        }
                        ImGui.text(s);
                        if (newLineIndex != 0 && newLineIndex != -1) {
                            startY += preCalc;
                            ImGui.text("\n");
                            ImGui.setCursorPos(startX, startY);
                            continue;
                        }
                    } else {
                        for (int j = 0; j < c.length; j++) {
                            ImGui.text(c[j]);
                            if (j != c.length - 1) {
                                startY += preCalc;
                                ImGui.text("\n");
                                ImGui.setCursorPos(startX, startY);
                            }
                        }
                    }
                    if (k != strs.length - 1) {
                        ImGui.sameLine(0, 0);
                    }
                }

                ImGui.setCursorPos(postPos.x, postPos.y);
                ImGui.popFont();
                ImGui.popStyleColor(1);

                ImGui.pushFont(ImguiLoader.fontAwesome28);
                ImGui.pushStyleColor(ImGuiCol.Text, 0.32f, 0.34f, 0.43f, 1.00f);
                ImGui.setCursorPos(prevPos.x + 585, prevPos.y + 16);
                ImGui.text("\uF142");
                ImGui.setCursorPos(postPos.x, postPos.y);
                ImGui.popFont();
                ImGui.popStyleColor(1);

                if (!KeyUtils.getKeyName(module.keybind.getCode()).equalsIgnoreCase("None")) {
                    String key = KeyUtils.getKeyName(module.keybind.getCode());

                    ImGui.pushFont(ImguiLoader.poppins32);
                    ImGui.pushStyleColor(ImGuiCol.Text,          0.32f, 0.34f, 0.43f, 1.00f);
                    ImGui.pushStyleColor(ImGuiCol.Button,        0.70f, 0.74f, 0.86f, 0.05f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.70f, 0.74f, 0.86f, 0.05f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive,  0.70f, 0.74f, 0.86f, 0.05f);
                    ImGui.pushStyleVar(ImGuiStyleVar.ButtonTextAlign, 0.5f, 0.5f);
                    float width = 40f;
                    if (ImGui.calcTextSize(key).x>width) width=ImGui.calcTextSize(key).x+24;
                    ImGui.setCursorPos(prevPos.x + 575f - width , prevPos.y + 10);
                    ImGui.button(key, width, 40);
                    ImGui.setCursorPos(postPos.x, postPos.y);
                    ImGui.popFont();
                    ImGui.popStyleVar(1);
                    ImGui.popStyleColor(4);
                }

                if (module.isEnabled()) {
                    //float[] color = JColor.getGuiColor().getFloatColor();
                    float[] color = UI.blendColors(new JColor(0.14f, 0.16f, 0.21f, 1.0f), JColor.getGuiColor(), EasingUtil.easeOutSine(AnimationUtil.getRawPressPercentage("Module/"+module.getName(), 150))).getFloatColorWAlpha();
                    ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 0.5f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.5f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.5f);
                } else {
                    float[] color = UI.blendColors(JColor.getGuiColor(), new JColor(0.14f, 0.16f, 0.21f, 1.0f), EasingUtil.easeInSine(AnimationUtil.getRawPressPercentage("Module/"+module.getName(), 150))).getFloatColorWAlpha();
                    //ImGui.pushStyleColor(ImGuiCol.Button, 0.14f, 0.16f, 0.21f, 1.0f);
                    //ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.14f, 0.16f, 0.21f, 1.0f);
                    //ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.14f, 0.16f, 0.21f, 1.0f);
                    ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], 0.5f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.5f);
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], 0.5f);
                }
                ImGui.setCursorPos(prevPos.x + 10, prevPos.y + 10);
                ImGui.button("##", 40f, 40f);
                ImGui.setCursorPos(postPos.x, postPos.y);

                ImGui.popStyleColor(3);

                float[] color = UI.blendColors(new JColor(JColor.getGuiColor()).setAlpha(0), JColor.getGuiColor(), EasingUtil.easeInSine(AnimationUtil.getRawPressPercentage("Module/" + module.getName(), 100))).getFloatColorWAlpha();
                if (!module.isEnabled()) {
                    color = UI.blendColors(JColor.getGuiColor(), new JColor(JColor.getGuiColor()).setAlpha(0), EasingUtil.easeInSine(AnimationUtil.getRawPressPercentage("Module/" + module.getName(), 100))).getFloatColorWAlpha();
                }
                ImGui.pushStyleColor(ImGuiCol.Button, color[0], color[1], color[2], color[3]);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], color[3]);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, color[0], color[1], color[2], color[3]);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 10f-3);

                ImGui.setCursorPos(prevPos.x + 10 + 3, prevPos.y + 10 + 3);
                ImGui.button("##", 40f - 3 * 2, 40f - 3 * 2);
                ImGui.setCursorPos(postPos.x, postPos.y);

                ImGui.popStyleVar(1);
                ImGui.popStyleColor(3);

                if (module.showOptions()) {
                    ImGui.indent(4f + 9f);
                    ImGui.pushFont(ImguiLoader.poppins18);
                    ImGui.getStyle().setFrameRounding(4f);
                    ImGui.getStyle().setFramePadding(4, 4);
                    ImGui.getStyle().setButtonTextAlign(0.5f, 0.5f);

                    float settingsHeight = module.getSettingsHeight();

                    ImGui.setCursorPosY(ImGui.getCursorPosY() + 7f);
                    ImGui.getWindowDrawList().addRectFilled(
                            ImGui.getWindowPosX() + prevPos.x + 4f,
                            ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY() - 4f,
                            ImGui.getWindowPosX() + prevPos.x + 296f + 304f - 4f,
                            ImGui.getWindowPosY() + ImGui.getCursorPosY() - ImGui.getScrollY() - 14f + 15 + settingsHeight,
                            ImGui.getColorU32(0.16f, 0.18f, 0.24f, 0.3f), 10f
                    );
                    module.renderSettings();
                    ImGui.getStyle().setButtonTextAlign(0.05f, 0.5f);
                    ImGui.getStyle().setFramePadding(4, 6);
                    ImGui.getStyle().setFrameRounding(10f);
                    ImGui.popFont();
                    ImGui.unindent(4f + 9f);
                    ImGui.setCursorPosY(ImGui.getCursorPosY() + 10);
                }
                ImGui.setCursorPosY(ImGui.getCursorPosY() + 3);
                ImGui.popID();
            }
        }

        // Categories
        ImGui.setCursorPos(15, 15 + ImGui.getScrollY());

        ImGui.getWindowDrawList().addRectFilled(
                ImGui.getCursorScreenPosX() - 14,
                ImGui.getCursorScreenPosY() - 14,
                ImGui.getCursorScreenPosX() + 630 - 15,
                ImGui.getCursorScreenPosY() + 40,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, 1.00f), 16f,
                ImDrawFlags.RoundCornersLeft
        );

        for (Module.Category category : Module.Category.values()) {
            ImGui.getStyle().setFramePadding(0f, 0f);
            float[] color = JColor.getGuiColor().getFloatColor();
            if (category.name.contains("Legit")) continue;
            ImGui.pushID(category.name);

            String text = category.name.substring(2);

            if (selectedCategory == category) {
                ImGui.pushStyleColor(ImGuiCol.Button,        color[0], color[1], color[2], 0.9f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, color[0], color[1], color[2], 0.8f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive,  color[0], color[1], color[2], 0.7f);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button, 0.16f, 0.18f, 0.24f, 0.3f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.16f, 0.18f, 0.24f, 0.4f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.16f, 0.18f, 0.24f, 0.5f);
            }
            ImVec2 prevPos = ImGui.getCursorPos().clone();
            ImGui.pushFont(ImguiLoader.poppins20);
            float sizeX = ImGui.calcTextSize(text).x;
            ImGui.getStyle().setFrameRounding(8f);
            ImVec2 prePos = ImGui.getCursorPos().clone();
            ImGui.button("##", sizeX + 14, 30f);
            if (ImGui.isMouseDown(0) && (ImGui.getMousePos().x > ImGui.getWindowPosX() + prePos.x && ImGui.getMousePos().x < ImGui.getWindowPosX() + prePos.x + sizeX + 14)
                    && (ImGui.getMousePos().y > ImGui.getWindowPosY() + prePos.y - ImGui.getScrollY() && ImGui.getMousePos().y < ImGui.getWindowPosY() + prePos.y - ImGui.getScrollY() + 30)) {
                if (selectedCategory != category) AnimationUtil.hookPress("ModuleCategory", true);
                selectedCategory = category;
                ModulesMenu.getInstance().scrollY = 0;
                ModulesMenu.getInstance().scrollUntil = 0;
            }
            ImVec2 postPos = ImGui.getCursorPos().clone();
            ImGui.sameLine(0,0);
            ImVec2 linePos = ImGui.getCursorPos().clone();
            if (selectedCategory == category) {
                if (ImGui.isItemHovered())
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.8f);
                else
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 1f);
            } else {
                if (ImGui.isItemHovered())
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.4f);
                else
                    ImGui.pushStyleColor(ImGuiCol.Text, 0.83f, 0.86f, 0.94f, 0.5f);
            }
            ImGui.setCursorPos(prevPos.x + 5, prevPos.y + 5);
            ImGui.text(text);
            ImGui.setCursorPos(linePos.x + 5, linePos.y);
            ImGui.getStyle().setFrameRounding(10f);
            ImGui.popFont();
            ImGui.popStyleColor(4);

            ImGui.popID();
        }

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX(),
                ImGui.getWindowPosY() + 414,
                ImGui.getWindowPosX() + 16,
                ImGui.getWindowPosY() + 414 + 16,
                ImGui.getColorU32(0.21f, 0.24f, 0.31f, 1.00f), 0f
        );
        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 1,
                ImGui.getWindowPosY() + 414 - 1,
                ImGui.getWindowPosX() + 16,
                ImGui.getWindowPosY() + 414 - 1 + 16,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, 1.00f), 0f
        );

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 614,
                ImGui.getWindowPosY(),
                ImGui.getWindowPosX() + 614 + 16,
                ImGui.getWindowPosY() + 16,
                ImGui.getColorU32(0.21f, 0.24f, 0.31f, 1.00f), 0f
        );
        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 614 - 1,
                ImGui.getWindowPosY() + 1,
                ImGui.getWindowPosX() + 614 - 1 + 16,
                ImGui.getWindowPosY() + 16,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, 1.00f), 0f
        );

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 15,
                ImGui.getWindowPosY() + 1,
                ImGui.getWindowPosX() + ImGui.getWindowSizeX() - 15,
                ImGui.getWindowPosY() + ImGui.getWindowSizeY() - 1,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, EasingUtil.easeInQuad(1f-AnimationUtil.getRawPressPercentage("CategorySwitch", 300))), 0f
        );

        ImGui.getForegroundDrawList().addRectFilled(
                ImGui.getWindowPosX() + 15,
                ImGui.getWindowPosY() + 60,
                ImGui.getWindowPosX() + ImGui.getWindowSizeX() - 15,
                ImGui.getWindowPosY() + ImGui.getWindowSizeY() - 1,
                ImGui.getColorU32(0.12f, 0.13f, 0.17f, EasingUtil.easeInQuad(1f-AnimationUtil.getRawPressPercentage("ModuleCategory", 300))), 0f
        );

        //ImGui.setCursorPos(0, 414);
        //ImGui.button("##", 16, 16);
        //ImGui.setCursorPos(614, 0);
        //ImGui.button("##", 16, 16);
        ImGui.end();
    }

    public void pre() {
        float[][] colors = ImGui.getStyle().getColors();
        float[] color = JColor.getGuiColor().getFloatColor();

        colors[ImGuiCol.Text]                   = new float[]{0.83f, 0.86f, 0.94f, 1.00f};
        colors[ImGuiCol.WindowBg]               = new float[]{0.11f, 0.12f, 0.16f, 0.5f};
        colors[ImGuiCol.ChildBg]                = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
        colors[ImGuiCol.PopupBg]                = new float[]{0.13f, 0.14f, 0.19f, 0.94f};
        colors[ImGuiCol.Border]                 = new float[]{0.21f, 0.24f, 0.31f, 1.00f};
        colors[ImGuiCol.FrameBg]                = new float[]{color[0], color[1], color[2], 0.54f};
        colors[ImGuiCol.FrameBgHovered]         = new float[]{color[0], color[1], color[2], 0.40f};
        colors[ImGuiCol.FrameBgActive]          = new float[]{color[0], color[1], color[2], 0.67f};
        colors[ImGuiCol.Button]                 = new float[]{color[0], color[1], color[2], 0.59f};
        colors[ImGuiCol.ButtonHovered]          = new float[]{color[0], color[1], color[2], 0.9f};
        colors[ImGuiCol.ButtonActive]           = new float[]{color[0], color[1], color[2], 1.00f};
        colors[ImGuiCol.TitleBg]                = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
        colors[ImGuiCol.TitleBgActive]          = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
        colors[ImGuiCol.TitleBgCollapsed]       = new float[]{0.13f, 0.14f, 0.19f, 0.5f};
        colors[ImGuiCol.ScrollbarBg]            = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
        colors[ImGuiCol.ScrollbarGrab]          = new float[]{0.18f, 0.21f, 0.27f, 0.00f};
        colors[ImGuiCol.ScrollbarGrabHovered]   = new float[]{0.22f, 0.25f, 0.33f, 0.00f};
        colors[ImGuiCol.ScrollbarGrabActive]    = new float[]{0.25f, 0.29f, 0.37f, 0.00f};

        ImGui.getStyle().setColors(colors);

        ImGui.getStyle().setWindowRounding(8);
        ImGui.getStyle().setFrameRounding(4);
        ImGui.getStyle().setGrabRounding(4);
        ImGui.getStyle().setPopupRounding(4);
        ImGui.getStyle().setScrollbarRounding(4);
        ImGui.getStyle().setTabRounding(4);
        ImGui.getStyle().setWindowTitleAlign(0.5f, 0.5f);
        ImGui.getStyle().setScrollbarSize(1);
    }

    /*
    @Override
    public Theme getTheme() {
        return new Theme() {
            @Override
            public void preRender() {
                float[][] colors = ImGui.getStyle().getColors();

                float[] color = JColor.getGuiColor().getFloatColor();
                float[] bColor = JColor.getGuiColor().jBrighter().getFloatColor();
                float[] dColor = JColor.getGuiColor().jDarker().getFloatColor();

                colors[ImGuiCol.Text]                   = new float[]{0.83f, 0.86f, 0.94f, 1.00f};
                //colors[ImGuiCol.TextDisabled]           = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
                colors[ImGuiCol.WindowBg]               = new float[]{0.11f, 0.12f, 0.16f, 0.5f};
                //colors[ImGuiCol.ChildBg]                = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
                //colors[ImGuiCol.PopupBg]                = new float[]{0.13f, 0.14f, 0.19f, 0.94f};
                colors[ImGuiCol.Border]                 = new float[]{0.21f, 0.24f, 0.31f, 1.00f};
                //colors[ImGuiCol.BorderShadow]           = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
                colors[ImGuiCol.FrameBg]                = new float[]{color[0], color[1], color[2], 0.54f};
                colors[ImGuiCol.FrameBgHovered]         = new float[]{color[0], color[1], color[2], 0.40f};
                colors[ImGuiCol.FrameBgActive]          = new float[]{color[0], color[1], color[2], 0.67f};
                colors[ImGuiCol.TitleBg]                = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
                colors[ImGuiCol.TitleBgActive]          = new float[]{0.13f, 0.14f, 0.19f, 0.95f};
                colors[ImGuiCol.TitleBgCollapsed]       = new float[]{0.13f, 0.14f, 0.19f, 0.5f};
                //colors[ImGuiCol.MenuBarBg]              = new float[]{0.13f, 0.14f, 0.19f, 1.00f};
                colors[ImGuiCol.ScrollbarBg]            = new float[]{0.13f, 0.14f, 0.19f, 0.00f};
                colors[ImGuiCol.ScrollbarGrab]          = new float[]{0.18f, 0.21f, 0.27f, 0.00f};
                colors[ImGuiCol.ScrollbarGrabHovered]   = new float[]{0.22f, 0.25f, 0.33f, 0.00f};
                colors[ImGuiCol.ScrollbarGrabActive]    = new float[]{0.25f, 0.29f, 0.37f, 0.00f};
                //colors[ImGuiCol.CheckMark]              = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
                //colors[ImGuiCol.SliderGrab]             = new float[]{color[0], color[1], color[2], 0.9f};
                //colors[ImGuiCol.SliderGrabActive]       = new float[]{color[0], color[1], color[2], 0.95f};
                //colors[ImGuiCol.Button]                 = new float[]{color[0], color[1], color[2], 0.59f};
                //colors[ImGuiCol.ButtonHovered]          = new float[]{color[0], color[1], color[2], 0.9f};
                //colors[ImGuiCol.ButtonActive]           = new float[]{color[0], color[1], color[2], 1.00f};
                //colors[ImGuiCol.Header]                 = new float[]{color[0], color[1], color[2], 0.9f};
                //colors[ImGuiCol.HeaderHovered]          = new float[]{color[0], color[1], color[2], 0.95f};
                //colors[ImGuiCol.HeaderActive]           = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
                //colors[ImGuiCol.Separator]              = new float[]{0.18f, 0.21f, 0.27f, 1.00f};
                //colors[ImGuiCol.SeparatorHovered]       = new float[]{0.81f, 0.25f, 0.33f, 1.00f};
                //colors[ImGuiCol.SeparatorActive]        = new float[]{0.74f, 0.22f, 0.30f, 1.00f};
                //colors[ImGuiCol.ResizeGrip]             = new float[]{color[0], color[1], color[2], 0.59f};
                //colors[ImGuiCol.ResizeGripHovered]      = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
                //colors[ImGuiCol.ResizeGripActive]       = new float[]{color[0], color[1], color[2], 1.00f};
                //colors[ImGuiCol.Tab]                    = new float[]{dColor[0], dColor[1], dColor[2], 0.86f};
                //colors[ImGuiCol.TabHovered]             = new float[]{color[0], color[1], color[2], 0.80f};
                //colors[ImGuiCol.TabActive]              = new float[]{bColor[0], bColor[1], bColor[2], 1.00f};
                //colors[ImGuiCol.TabUnfocused]           = new float[]{0.15f, 0.18f, 0.25f, 1.00f};
                //colors[ImGuiCol.TabUnfocusedActive]     = new float[]{0.56f, 0.21f, 0.26f, 0.67f};
                //colors[ImGuiCol.DockingPreview]         = new float[]{0.91f, 0.26f, 0.36f, 0.67f};
                //colors[ImGuiCol.DockingEmptyBg]         = new float[]{0.20f, 0.20f, 0.20f, 1.00f};
                //colors[ImGuiCol.PlotLines]              = new float[]{0.61f, 0.61f, 0.61f, 1.00f};
                //colors[ImGuiCol.PlotLinesHovered]       = new float[]{1.00f, 0.43f, 0.35f, 1.00f};
                //colors[ImGuiCol.PlotHistogram]          = new float[]{0.90f, 0.70f, 0.00f, 1.00f};
                //colors[ImGuiCol.PlotHistogramHovered]   = new float[]{1.00f, 0.60f, 0.00f, 1.00f};
                //colors[ImGuiCol.TableHeaderBg]          = new float[]{0.19f, 0.19f, 0.20f, 1.00f};
                //colors[ImGuiCol.TableBorderStrong]      = new float[]{0.31f, 0.31f, 0.35f, 1.00f};
                //colors[ImGuiCol.TableBorderLight]       = new float[]{0.23f, 0.23f, 0.25f, 1.00f};
                //colors[ImGuiCol.TableRowBg]             = new float[]{0.00f, 0.00f, 0.00f, 0.00f};
                //colors[ImGuiCol.TableRowBgAlt]          = new float[]{1.00f, 1.00f, 1.00f, 0.06f};
                //colors[ImGuiCol.TextSelectedBg]         = new float[]{0.26f, 0.59f, 0.98f, 0.35f};
                //colors[ImGuiCol.DragDropTarget]         = new float[]{1.00f, 1.00f, 0.00f, 0.90f};
                //colors[ImGuiCol.NavHighlight]           = new float[]{0.26f, 0.59f, 0.98f, 1.00f};
                //colors[ImGuiCol.NavWindowingHighlight]  = new float[]{1.00f, 1.00f, 1.00f, 0.70f};
                //colors[ImGuiCol.NavWindowingDimBg]      = new float[]{0.80f, 0.80f, 0.80f, 0.20f};
                //colors[ImGuiCol.ModalWindowDimBg]       = new float[]{0.80f, 0.80f, 0.80f, 0.35f};

                ImGui.getStyle().setColors(colors);

                ImGui.getStyle().setWindowRounding(8);
                ImGui.getStyle().setFrameRounding(4);
                ImGui.getStyle().setGrabRounding(4);
                ImGui.getStyle().setPopupRounding(4);
                ImGui.getStyle().setScrollbarRounding(4);
                ImGui.getStyle().setTabRounding(4);
                ImGui.getStyle().setWindowTitleAlign(0.5f, 0.5f);
                ImGui.getStyle().setScrollbarSize(1);

                if (ImguiLoader.getFontAwesome16() != null) {
                    ImGui.pushFont(ImguiLoader.getFontAwesome16());
                }
            }

            @Override
            public void postRender() {
                if (ImguiLoader.getFontAwesome16() != null) {
                    ImGui.popFont();
                }
            }
        };
    }
    */
}