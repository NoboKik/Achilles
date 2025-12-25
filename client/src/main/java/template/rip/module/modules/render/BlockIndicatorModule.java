package template.rip.module.modules.render;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import template.rip.Template;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.util.BlockUtils;
import template.rip.api.util.EasingUtil;
import template.rip.api.util.UI;
import template.rip.gui.ImguiLoader;
import template.rip.gui.clickgui.AchillesMenu;
import template.rip.gui.utils.Renderable;
import template.rip.module.Module;
import template.rip.module.modules.blatant.ScaffoldModule;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.function.Predicate;

public class BlockIndicatorModule extends Module implements Renderable {

    //public final PosSetting position = new PosSetting(this, 20, 20);
    public final ColorSetting background = new ColorSetting(this, new JColor(0f, 0f, 0f, 0.75f), true, "Background Color");
    public final ColorSetting text = new ColorSetting(this, new JColor(1f, 1f, 1f), false, "Text Color");
    public final BooleanSetting bold = new BooleanSetting(this, true, "Bold");
    public final BooleanSetting suffix = new BooleanSetting(this, true, "Suffix");
    public final NumberSetting scale = new NumberSetting(this, 1, 0.5, 2, 0.1, "Scale");
    public final NumberSetting padding = new NumberSetting(this, 10, 0, 20, 1, "Padding");
    public final NumberSetting height = new NumberSetting(this, 50, 32, 100, 1, "Height");
    public final BooleanSetting backgroundEnabled = new BooleanSetting(this, true, "Background");
    public final NumberSetting roundedCorners = new NumberSetting(this, 0, 0, 16, 1, "Rounded Corners");
    public final BooleanSetting animation = new BooleanSetting(this, true, "Animation");
    public final NumberSetting duration = new NumberSetting(this, 500, 0, 1000, 1, "Animation Duration");

    private boolean firstFrame = true;

    public BlockIndicatorModule(Category category, Description description, String name) {
        super(category, description, name);
        toggleVisibility();
    }

    public void toggleVisibility() {
        ImguiLoader.addRenderable(this);
    }

    public int count() {
        if (!nullCheck()) return 0;
        Predicate<Item> placeBlocks = BlockUtils.placeableBlocks();

        Inventory inv = mc.player.getInventory();
        int count = 0;
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = inv.getStack(i);
            if (placeBlocks.test(itemStack.getItem())) count = count + itemStack.getCount();
        }

        return count;
    }

    @Override
    public void render() {
        if (!Template.displayRender()) return;
        if (!AchillesMenu.isClientEnabled())
            return;

        if (!this.isEnabled()) {
            firstFrame = true;
            return;
        }

        ScaffoldModule scaff = Template.moduleManager.getModule(ScaffoldModule.class);

        float progress;
        if (animation.isEnabled()) {
            if (scaff.isEnabled()) {
                long elapsed = System.currentTimeMillis() - ScaffoldModule.lastEnable;
                progress = elapsed / duration.getFValue();
                if (progress > 1f) progress = 1f;
            } else {
                long elapsed = System.currentTimeMillis() - ScaffoldModule.lastDisable;
                progress = 1f - (elapsed / duration.getFValue());
                if (progress < 0f) progress = 0f;
            }
            progress = EasingUtil.easeInSine(progress);
        } else {
            progress = scaff.isEnabled() ? 1 : 0;
        }

        ImFont font = UI.getFont(32, false);
        font.setScale(scale.getFValue());

        ImFont boldFont = UI.getFont(32, bold.isEnabled());
        boldFont.setScale(scale.getFValue());

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoScrollbar;
        if (!backgroundEnabled.isEnabled()) imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
        float[] c;
        if (!Template.moduleManager.isModuleEnabled(AchillesSettingsModule.class)) {
            imGuiWindowFlags |= ImGuiWindowFlags.NoMove;
            ImGui.getStyle().setWindowBorderSize(0);
            ImGui.pushStyleColor(ImGuiCol.Border, 0f,0f,0f,0f);
            c = background.getColor().getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.WindowBg, c[0], c[1], c[2], c[3]*progress);
        } else {
            ImGui.getStyle().setWindowBorderSize(1);
            //ImGui.pushStyleColor(ImGuiCol.Border, 1f, 1f, 1f, 1f);
            ImGui.pushStyleColor(ImGuiCol.Border, 0f,0f,0f,0f);
            c = background.getColor().jBrighter().getFloatColor();
            ImGui.pushStyleColor(ImGuiCol.WindowBg, c[0], c[1], c[2], c[3]*progress);
        }
        c = text.getColor().getFloatColor();
        ImGui.pushStyleColor(ImGuiCol.Text, c[0], c[1], c[2], c[3]*progress);

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

        ImGui.pushFont(font);
        String text = count() + " blocks";
        if (!suffix.isEnabled()) text = count() + "";

        ImGui.getStyle().setWindowMinSize(1, 1);
        ImGui.setNextWindowSize(padding.getFValue() * scale.getFValue() * 2 + ImGui.calcTextSize(text).x, height.getFValue() * scale.getFValue());
        ImGui.getStyle().setWindowRounding(0);
        ImGui.getStyle().setWindowMinSize(1, 1);
        ImGui.getStyle().setWindowRounding(roundedCorners.getFValue() * scale.getFValue());
        ImGui.begin(this.getName(), imGuiWindowFlags);

        String count = count()+"";
        String suffix = " blocks";
        if (!this.suffix.isEnabled()) suffix = "";

        float windowWidth  = ImGui.getWindowSize().x;
        float windowHeight = ImGui.getWindowSize().y;
        float textWidth    = ImGui.calcTextSize(text).x;
        float textHeight   = ImGui.calcTextSize(text).y;

        ImGui.setCursorPos((windowWidth - textWidth) * 0.5f, (windowHeight - textHeight) * 0.5f);

        ImGui.pushFont(boldFont);
        ImGui.text(count);
        ImGui.popFont();

        ImGui.sameLine(0,0);

        ImGui.pushFont(font);
        ImGui.text(suffix);
        ImGui.popFont();

        ImGui.popStyleColor(3);
        font.setScale(1f);
        ImGui.getStyle().setWindowRounding(8);
        this.position.x = ImGui.getWindowPosX();
        this.position.y = ImGui.getWindowPosY();
        isFocused = ImGui.isWindowFocused();

        ImGui.end();
        ImGui.popFont();
        if (firstFrame) firstFrame = false;
    }
}