package template.rip.module.modules.legit;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.text.WordUtils;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.MathUtils;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.utils.Renderable;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.NumberSetting;

public class CoordsModule extends Module implements Renderable {

    public final ColorSetting background = new ColorSetting(this, new JColor(0f, 0f, 0f, 0.75f), true, "Background Color");
    public final ColorSetting text = new ColorSetting(this, new JColor(1f, 1f, 1f), false, "Text Color");
    public final NumberSetting scale = new NumberSetting(this, 1, 0.5, 2, 0.1, "Scale");
    public final NumberSetting width = new NumberSetting(this, 200, 150, 300, 1, "Width");
    public final NumberSetting height = new NumberSetting(this, 150, 100, 200, 1, "Height");
    public final NumberSetting roundedCorners = new NumberSetting(this, 0, 0, 16, 1, "Rounded Corners");
    public final BooleanSetting biome = new BooleanSetting(this, true, "Biome");
    public final BooleanSetting direction = new BooleanSetting(this, true, "Direction");

    private boolean firstFrame = true;

    public CoordsModule(Category category, Description description, String name) {
        super(category, description, name);
        toggleVisibility();
    }

    public void toggleVisibility() {
        ImguiLoader.addRenderable(this);
    }

    @Override
    public void render() {
        if (!Template.displayRender() || !AchillesMenu.isClientEnabled()) return;
        if (!this.isEnabled()) {
            firstFrame = true;
            return;
        }
        //if (!isRenderable()) return;

        ImFont font = ImguiLoader.poppins32;
        font.setScale(scale.getFValue());

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoScrollbar;
        float[] c;
        if (!Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class)) {
            ImGui.getStyle().setWindowBorderSize(0);
            imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
            ImGui.pushStyleColor(ImGuiCol.Border, 0f,0f,0f,0f);
            c = background.getColor().getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.WindowBg, c[0], c[1], c[2], c[3]);
        } else {
            ImGui.getStyle().setWindowBorderSize(1);
            ImGui.pushStyleColor(ImGuiCol.Border, 1f, 1f, 1f, 1f);
            c = background.getColor().jBrighter().getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.WindowBg, c[0], c[1], c[2], c[3]);
        }
        c = text.getColor().getFloatColor();
        ImGui.pushStyleColor(ImGuiCol.Text, c[0], c[1], c[2], c[3]);

        if (this.updatedPos.x != 0) {
            this.position.x = this.position.x + this.updatedPos.x;
            this.updatedPos.x = 0;
            ImGui.setNextWindowPos(this.position.x, this.position.y);
        }
        if (this.updatedPos.y != 0) {
            this.position.y = this.position.y + this.updatedPos.y;
            this.updatedPos.y = 0;
            ImGui.setNextWindowPos(this.position.x, this.position.y);
        }
        if (firstFrame || reloadPosition || !Template.shouldMove()) {
            ImGui.setNextWindowPos(super.position.x, super.position.y);
            reloadPosition = false;
        }
        ImGui.setNextWindowSize(width.getFValue() * scale.getFValue(), height.getFValue() * scale.getFValue());
        ImGui.pushFont(font);
        ImGui.getStyle().setWindowRounding(0);
        ImGui.getStyle().setWindowMinSize(1, 1);
        ImGui.getStyle().setWindowRounding(roundedCorners.getFValue() * scale.getFValue());
        ImGui.begin(this.getName(), imGuiWindowFlags);
        if (mc.player != null && mc.world != null) {
            String dirString = "";
            if (mc.player.getHorizontalFacing().getAxis() == Direction.Axis.X) {
                if (mc.player.getHorizontalFacing().getDirection() == Direction.AxisDirection.POSITIVE) {
                    dirString = "X+";
                } else if (mc.player.getHorizontalFacing().getDirection() == Direction.AxisDirection.NEGATIVE) {
                    dirString = "X-";
                }
            } else if (mc.player.getHorizontalFacing().getAxis() == Direction.Axis.Z) {
                if (mc.player.getHorizontalFacing().getDirection() == Direction.AxisDirection.POSITIVE) {
                    dirString = "Z+";
                } else if (mc.player.getHorizontalFacing().getDirection() == Direction.AxisDirection.NEGATIVE) {
                    dirString = "Z-";
                }
            }

            String nameString = "";
            if (mc.player.getHorizontalFacing().getName().equals("east")) nameString = "E";
            if (mc.player.getHorizontalFacing().getName().equals("west")) nameString = "W";
            if (mc.player.getHorizontalFacing().getName().equals("south")) nameString = "S";
            if (mc.player.getHorizontalFacing().getName().equals("north")) nameString = "N";

            float nameWidth = ImGui.calcTextSize(nameString).x;
            float dirWidth = ImGui.calcTextSize(dirString).x;

            ImVec2 oldPos;
            oldPos = ImGui.getCursorPos();
            ImGui.setCursorPosX(width.getFValue() * scale.getFValue() - 4 - dirWidth);
            if (direction.isEnabled()) ImGui.text(dirString);
            ImGui.setCursorPos(oldPos.x, oldPos.y);

            ImGui.text(String.format(" X: %.1f", mc.player.getPos().x));

            oldPos = ImGui.getCursorPos();
            ImGui.setCursorPosX(width.getFValue() * scale.getFValue() - 4 - nameWidth);
            if (direction.isEnabled()) ImGui.text(nameString);
            ImGui.setCursorPos(oldPos.x, oldPos.y);

            ImGui.text(String.format(" Y: %.1f", mc.player.getPos().y));
            ImGui.text(String.format(" Z: %.1f", mc.player.getPos().z));
            String biomeString = mc.world.getBiome(new BlockPos((int) mc.player.getPos().x, (int) mc.player.getPos().y, (int) mc.player.getPos().z)).getKey().get().getValue().getPath();
            biomeString = biomeString.replaceAll("_", " ");
            biomeString = WordUtils.capitalize(biomeString);
            if (biome.isEnabled())
                ImGui.text(String.format(" Biome: %s", biomeString));
        } else {
            ImVec2 oldPos;
            oldPos = ImGui.getCursorPos();
            ImGui.setCursorPosX(width.getFValue() * scale.getFValue() - 4 - ImGui.calcTextSize("??").x);
            if (direction.isEnabled()) ImGui.text("??");
            ImGui.setCursorPos(oldPos.x, oldPos.y);
            ImGui.text(" X: ???");

            oldPos = ImGui.getCursorPos();
            ImGui.setCursorPosX(width.getFValue() * scale.getFValue() - 4 - ImGui.calcTextSize("?").x);
            if (direction.isEnabled()) ImGui.text("?");
            ImGui.setCursorPos(oldPos.x, oldPos.y);

            ImGui.text(" Y: ???");
            ImGui.text(" Z: ???");
            if (biome.isEnabled())
                ImGui.text(" Biome: ???");
        }

        ImGui.popStyleColor(3);
        ImGui.popFont();
        font.setScale(1f);
        ImGui.getStyle().setWindowRounding(8);
        this.position = ImGui.getWindowPos();
        isFocused = ImGui.isWindowFocused();

        ImGui.end();
        if (firstFrame) firstFrame = false;
    }
}
