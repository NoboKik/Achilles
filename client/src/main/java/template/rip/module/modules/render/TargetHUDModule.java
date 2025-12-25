package template.rip.module.modules.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.joml.Matrix4f;
import template.rip.Template;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Blur;
import template.rip.api.object.Description;
import template.rip.api.object.Rectangle;
import template.rip.api.util.*;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.gui.utils.Theme;
import template.rip.module.Module;
import template.rip.module.modules.client.AchillesSettingsModule;
import template.rip.module.setting.settings.*;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class TargetHUDModule extends Module implements Renderable {

    public enum lol{L, W, Equal, Losing, Winning, NA}
    public final DividerSetting generalDivider = new DividerSetting(this, false, "General");
    public final BooleanSetting targetReq = new BooleanSetting(this, false, "Only with a target");

    public enum targetFocusEnum{Left, Right, Off}
    public final ModeSetting<targetFocusEnum> targetFocus = new ModeSetting<>(this, targetFocusEnum.Off, "Target Focus");

    public final NumberSetting targetOffsetX = new NumberSetting(this, 10, -100, 100, 1, "X Target Offset");
    public final NumberSetting targetOffsetY = new NumberSetting(this, 10, -100, 100, 1, "Y Target Offset");
    public final BooleanSetting winLos = new BooleanSetting(this, true, "W/L");
    //public final BooleanSetting winLosDisplay = new BooleanSetting("W/L Top Display", this, true);
    public final BooleanSetting winLosShort = new BooleanSetting(this, true, "Short");
    public final BooleanSetting hpNumber = new BooleanSetting(this, true, "HP Number");
    public final BooleanSetting hurtTime = new BooleanSetting(this, false, "Hurt Time");
    public final BooleanSetting distance = new BooleanSetting(this, false, "Distance");
    public final BooleanSetting ping = new BooleanSetting(this, false, "Ping");
    public final BooleanSetting totemPops = new BooleanSetting(this, false, "Totem Pops");
    //public final BooleanSetting pingDisplayAtTop = new BooleanSetting("Ping Top Display", this, true);
    public final BooleanSetting tiers = new BooleanSetting(this, false, "MCTiers");

    public final DividerSetting windowDivider = new DividerSetting(this, false, "Window");
    public final BooleanSetting blur = new BooleanSetting(this, false, "Blur");
    public final NumberSetting blurRadius = new NumberSetting(this, 10, 0, 30, 1, "Blur Radius");
    public final NumberSetting rounding = new NumberSetting(this, 8, 0, 20, 1, "Rounding");
    public final NumberSetting padding = new NumberSetting(this, 10, 0, 30, 1, "Padding");
    public final NumberSetting width = new NumberSetting(this, 300, 200, 500, 1, "Width");
    public final NumberSetting height = new NumberSetting(this, 80, 50, 150, 1, "Height");
    public final NumberSetting globalScale = new NumberSetting(this, 1, 0.1, 2, 0.01, "Global Scale");
    public final NumberSetting textOffsetTop = new NumberSetting(this, 0, -10, 100, 1, "Text Offset Top");
    public final NumberSetting textOffsetBottom = new NumberSetting(this, 40, 0, 100, 1, "Text Offset Bottom");
    public final BooleanSetting boldName = new BooleanSetting(this, false, "Name Bold");
    public final BooleanSetting namePrefix = new BooleanSetting(this, false, "Name Prefix");
    public final BooleanSetting bigName = new BooleanSetting(this, false, "Bigger Name");
    public final BooleanSetting bottomBold = new BooleanSetting(this, false, "Bottom Text Bold");
    public final BooleanSetting bottomTextSmall = new BooleanSetting(this, false, "Smaller Bottom Text");

    public final DividerSetting headDivider = new DividerSetting(this, false, "Head");
    public final NumberSetting headSize = new NumberSetting(this, 60, 50, 100, 1, "Head Size");
    public final BooleanSetting hurtRepeat = new BooleanSetting(this, false, "Hurt Repeat");
    public final BooleanSetting hurtEasing = new BooleanSetting(this, false, "Hurt Easing");
    public final NumberSetting hurtDuration = new NumberSetting(this, 0, 0, 1, 0.01, "Hurt Duration");

    public final DividerSetting hpBarDivider = new DividerSetting(this, false, "Health Bar");
    public final BooleanSetting health = new BooleanSetting(this, true, "Health Bar Enabled");
    public final BooleanSetting hpBottom = new BooleanSetting(this, false, "Health at bottom");
    public final NumberSetting hpOffset = new NumberSetting(this, 15, 0, 100, 1, "Health Offset");
    public final NumberSetting healthBarHeight = new NumberSetting(this, 10, 0, 30, 1, "Health Bar Height");
    public final NumberSetting barRounding = new NumberSetting(this, 5, 0, 15, 1, "Health Bar Rounding");
    public final BooleanSetting hpAnimate = new BooleanSetting(this, true, "HP Animate");
    public final NumberSetting hpAnimationDuration = new NumberSetting(this, 500, 0, 1000, 1, "HP Duration");
    public enum hpAnimationEasingEnum{EaseOutQuart, EaseOutCubic, EaseInQuart, EaseInCubic, EaseLinear}
    public final ModeSetting<hpAnimationEasingEnum> hpAnimationEasing = new ModeSetting<>(this, hpAnimationEasingEnum.EaseOutQuart, "HP Easing");
    public final BooleanSetting waveHP = new BooleanSetting(this, false, "HP Wave");
    public final NumberSetting waveHpIndex = new NumberSetting(this, 50, 1, 200, 1, "HP Wave Index");
    public final DividerSetting colorsDivider = new DividerSetting(this, false, "Colors");
    public final ColorSetting windowBg = new ColorSetting(this, new JColor(0f,0f,0f, 0.3f), true, "Background");
    public final ColorSetting textColor = new ColorSetting(this, new JColor(1f,1f,1f, 1.00f), true, "Text");
    public final ColorSetting hpBar = new ColorSetting(this, new JColor(0f,1f,0f, 1f), true, "HP Bar");
    public final ColorSetting hpBg = new ColorSetting(this, new JColor(0f,0f,0f, 0.5f), true, "HP Background");
    public final ColorSetting hurtColor = new ColorSetting(this, new JColor(1f,0f,0f, 0.5f), true, "Hurt Color");
    public final ColorSetting nameColor = new ColorSetting(this, new JColor(1f,1f,1f, 1.00f), true, "Name Color");
    public final BooleanSetting hpGradient = new BooleanSetting(this, false, "HP Bar Gradient");
    public final ColorSetting hpBar1 = new ColorSetting(this, new JColor(0f,1f,0f, 0.5f), true, "HP Bar Col #1");
    public final ColorSetting hpBar2 = new ColorSetting(this, new JColor(0f,1f,0f, 0.5f), true, "HP Bar Col #2");
    public final DividerSetting glowDivider = new DividerSetting(this, false, "Glow");
    public final BooleanSetting glowEnabled = new BooleanSetting(this, false, "Glow Enabled");
    public final ColorSetting glowColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Glow Color");
    public final NumberSetting glowSize = new NumberSetting(this, 30, 0, 60, 1, "Glow's Size");
    public final BooleanSetting glowFill = new BooleanSetting(this, false, "Glow's Fill");
    public final BooleanSetting waveGlow = new BooleanSetting(this, false, "Wave Glow");
    public final NumberSetting waveGlowIndex = new NumberSetting(this, 50, 1, 200, 1, "Wave Glow Index");

    public final DividerSetting waveDivider = new DividerSetting(this, false, "Wave");
    public final NumberSetting waveSpeed = new NumberSetting(this, 1, 0, 10, 1, "Wave Speed");
    public final BooleanSetting rainbowEnabled = new BooleanSetting(this, false, "Rainbow Enabled");
    public final NumberSetting rainbowSaturation = new NumberSetting(this, 0.8, 0, 1, 0.01, "Rainbow Saturation");
    public final NumberSetting rainbowBrightness = new NumberSetting(this, 1, 0, 1, 0.01, "Rainbow Brightness");

    public final DividerSetting gradientDivider = new DividerSetting(this, false, "Gradient");
    public final BooleanSetting gradientEnabled = new BooleanSetting(this, false, "Gradient Enabled");
    public final ColorSetting gradientColor1 = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Gradient Color 1");
    public final ColorSetting gradientColor2 = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Gradient Color 2");

    public final DividerSetting animationDivider = new DividerSetting(this, false, "Animation");
    public final BooleanSetting animationEnabled = new BooleanSetting(this, false, "Animation Enabled");
    public final NumberSetting animationDuration = new NumberSetting(this, 500, 0, 1000, 1, "Animation Duration");
    public enum animationEasingEnum{EaseOutBack, EaseOutQuart, EaseOutCubic}
    public final ModeSetting<animationEasingEnum> animationEasing = new ModeSetting<>(this, animationEasingEnum.EaseOutBack, "Animation Easing");

    private boolean firstFrame = true;
    private LivingEntity targetPlayer;
    private LivingEntity lastNonNullTarget;
    private long loseTime = 0L;
    float lastScale, lastx, lasty;
    private double lastHealth = 0f;
    private double currentHealth = 20f;
    private long healthChange = System.currentTimeMillis();
    private Pair<Rectangle, Boolean> entity = null;
    private Entity lastEntity = null;
    private long showUpTime = 0;
    private boolean isHeadNull = true;

    public TargetHUDModule(Category category, Description description, String name) {
        super(category, description, name);
        mergeDividers();
    }

    @Override
    public void onEnable() {
        targetPlayer = null;
        ImguiLoader.addRenderable(this);
    }

    @Override
    public void onDisable() {
        ImguiLoader.queueRemove(this);
        targetPlayer = null;
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Post event) {
        // handle targets
        if (targetPlayer != null) {
            lastNonNullTarget = targetPlayer;
        }
        LivingEntity lastTarget = targetPlayer;
        targetPlayer = PlayerUtils.findFirstLivingTargetOrNull();
        if (lastTarget != null && targetPlayer == null) {
            loseTime = System.currentTimeMillis();
        }

        // health animation code
        if (targetPlayer == null) {
            lastHealth = 0f;
            currentHealth = 0f;
            healthChange = System.currentTimeMillis();
        } else {
            if (targetPlayer.getHealth() != currentHealth) {
                lastHealth = currentHealth;
                currentHealth = targetPlayer.getHealth();
                healthChange = System.currentTimeMillis();
            }
        }
    }

    @EventHandler
    private void onRender(HudRenderEvent event) {
        if (!Template.displayRender() || !nullCheck()) {
            isHeadNull = true;
            return;
        }

        LivingEntity target = null;
        if (targetPlayer != null) target = targetPlayer;
        if (System.currentTimeMillis() - loseTime < animationDuration.getIValue()) target = lastNonNullTarget;
        if (animationEnabled.isEnabled()) {
            if (System.currentTimeMillis() - loseTime < animationDuration.getIValue()) target = null;
            else if (System.currentTimeMillis() - showUpTime < animationDuration.getIValue()) target = null;
        }

        if (target == null) {
            isHeadNull = true;
            return;
        }
//        PlayerEntity pe = (PlayerEntity) target; // what made you think this was a good idea

        Identifier id = target instanceof PlayerEntity ? PlayerUtils.getOrCreateHead(((PlayerEntity) target).getGameProfile().getId().toString()) : PlayerUtils.fallbackTexture;

        isHeadNull = id == null;
        if (id == null) return;

        ProjectionType projectionType = RenderSystem.getProjectionType();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), 0, 1000, 21000), ProjectionType.ORTHOGRAPHIC);

        Matrix4f positionMatrix = event.context.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        int xW = headSize.getIValue();
        int yH = (int) (headSize.getIValue() * lastScale);

        buffer.vertex(positionMatrix, lastx, lasty, 0).texture(0f, 0f);
        buffer.vertex(positionMatrix, lastx, lasty + yH, 0).texture(0f, 1f);
        buffer.vertex(positionMatrix, lastx + xW, lasty + yH, 0).texture(1f, 1f);
        buffer.vertex(positionMatrix, lastx + xW, lasty, 0).texture(1f, 0f);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderTexture(0, id);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, (float) (mc.getWindow().getFramebufferWidth() / mc.getWindow().getScaleFactor()), (float) (mc.getWindow().getFramebufferHeight() / mc.getWindow().getScaleFactor()), 0, 1000, 21000), projectionType);
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null)
            return;
        entity = null;
        LivingEntity ent = targetPlayer;
        if (ent != mc.player && ent instanceof LivingEntity && !(ent instanceof AnimalEntity)) {
            if (lastEntity != ent) showUpTime = System.currentTimeMillis();
            lastEntity = ent;
            entity = RenderUtils.Render3D.twoDeePosition(ent, event.context.positionMatrix(), event.context.projectionMatrix());
        }
        if (ent == null) {
            lastEntity = null;
        }
    }

    @Override
    public void render() {
        if (!Template.displayRender()) return;
        if (!Template.moduleManager.isModuleEnabled(this.getClass())) {
            firstFrame = true;
            return;
        }

        LivingEntity target = null;

        if (!animationEnabled.isEnabled()) target = targetPlayer;
        if (targetPlayer != null) target = targetPlayer;
        if (System.currentTimeMillis()-loseTime < animationDuration.getIValue()) target = lastNonNullTarget;

        if ((target == null && targetReq.isEnabled()) || !nullCheck())
            return;

        float progress = System.currentTimeMillis()-showUpTime;
        if (targetPlayer == null && target != null) progress = System.currentTimeMillis()-loseTime;
        if (progress > animationDuration.getFValue()) progress = animationDuration.getFValue();
        float scale = progress/animationDuration.getFValue();
        if (targetPlayer == null && target != null) scale = 1f - scale;
        if (animationEasing.is(animationEasingEnum.EaseOutBack)) scale = EasingUtil.easeOutBack(scale);
        if (animationEasing.is(animationEasingEnum.EaseOutQuart)) scale = EasingUtil.easeOutQuart(scale);
        if (animationEasing.is(animationEasingEnum.EaseOutCubic)) scale = EasingUtil.easeOutCubic(scale);

        if (scale < 0.2f) scale = 0.2f;
        if (scale > 1.2f) scale = 1.2f;

        if (!animationEnabled.isEnabled()) scale = 1f;

        scale = scale*globalScale.getFValue();

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
        if (Template.moduleManager.isModuleDisabled(AchillesSettingsModule.class)) imGuiWindowFlags |= ImGuiWindowFlags.NoMove;

        float[] bg = windowBg.getColor().getFloatColorWAlpha();
        float[] text = textColor.getColor().getFloatColorWAlpha();
        ImGui.pushStyleColor(ImGuiCol.WindowBg, bg[0], bg[1], bg[2], bg[3]);
        ImGui.pushStyleColor(ImGuiCol.Text, text[0], text[1], text[2], text[3]);
        ImGui.getStyle().setItemSpacing(0f, 0f);
        ImGui.getStyle().setItemInnerSpacing(0f, 0f);

        boolean did = false;
        if (tiers.isEnabled() && target instanceof PlayerEntity) {
            HashMap<String, String> testHash = PlayerUtils.processTiers(((PlayerEntity) target).getGameProfile().getName());
            if (testHash != null && !testHash.isEmpty()) {
                did = true;

                float length = 0;
                length += Math.round((testHash.size()) / 2.0);
                length *= 21f;
                length = Math.max(length, 0);

                ImGui.setNextWindowSize(width.getFValue() * scale, (height.getFValue() + length) * scale);
            }
        }

        if (!did) {
            ImGui.setNextWindowSize(width.getFValue()*scale, height.getFValue()*scale);
        }

        ImGui.getStyle().setWindowRounding(rounding.getFValue());

        float widthDiff = width.getFValue() - width.getFValue()*scale;
        float heightDiff = height.getFValue() - height.getFValue()*scale;

        if (this.updatedPos.x != 0) {
            super.position.x = super.position.x + this.updatedPos.x;
            this.updatedPos.x = 0;
            ImGui.setNextWindowPos(super.position.x, super.position.y);
        }
        if (this.updatedPos.y != 0) {
            super.position.y = super.position.y + this.updatedPos.y;
            this.updatedPos.y = 0;
            ImGui.setNextWindowPos(super.position.x, super.position.y);
        }
        if (firstFrame || reloadPosition || !Template.shouldMove()) {
            ImGui.setNextWindowPos(super.position.x, super.position.y);
            reloadPosition = false;
        }

        if (entity != null && !targetFocus.is(targetFocusEnum.Off)) {
            float scaleFactor = (float) (mc.getWindow().getScaleFactor());
            float x1 = 0;
            float y1 = 0;

            if (targetFocus.is(targetFocusEnum.Left)) {
                x1 = (float) entity.getLeft().x * scaleFactor - targetOffsetX.getFValue() - width.getFValue();
                y1 = (float) entity.getLeft().y * scaleFactor - targetOffsetY.getFValue();
            } else if (targetFocus.is(targetFocusEnum.Right)) {
                x1 = (float) entity.getLeft().z * scaleFactor - targetOffsetX.getFValue();
                y1 = (float) entity.getLeft().y * scaleFactor - targetOffsetY.getFValue();
            }

            if (x1 < 0) x1 = 0;
            if (y1 < 0) y1 = 0;

            if (x1 > 0 && y1 > 0) ImGui.setNextWindowPos(x1, y1);
        }
        if (scale != 1f && targetFocus.is(targetFocusEnum.Off)) ImGui.setNextWindowPos(super.position.x + widthDiff / 2, super.position.y + heightDiff / 2);

        ImGui.begin(getName(), imGuiWindowFlags);

        ImGui.setCursorPos(padding.getFValue()*scale, padding.getFValue()*scale);
        if (!isHeadNull) {
            cropHead(() ->
                            ImGui.getBackgroundDrawList().addRectFilled(
                                    ImGui.getWindowPosX(),
                                    ImGui.getWindowPosY(),
                                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                                    ImGui.getWindowPosY() + ImGui.getWindowHeight(),
                                    windowBg.getColor().getU32(),
                                    rounding.getFValue()
                            ),
                    scale,
                    0
            );
        } else {
            ImGui.getBackgroundDrawList().addRectFilled(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY(),
                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                    ImGui.getWindowPosY() + ImGui.getWindowHeight(),
                    windowBg.getColor().getU32(),
                    rounding.getFValue()
            );
        }
            /*Rectangle rect = new Rectangle(
                    ImGui.getCursorScreenPosX(),
                    ImGui.getCursorScreenPosY(),
                    ImGui.getCursorScreenPosX() + headSize.getFValue(),
                    ImGui.getCursorScreenPosY() + headSize.getFValue() * scale
            );
            ImGui.getBackgroundDrawList().pushClipRect(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY(),
                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                    ImGui.getWindowPosY() + padding.getFValue() * scale
            );
            ImGui.getBackgroundDrawList().addRectFilled(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY(),
                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                    ImGui.getWindowPosY() + ImGui.getWindowHeight(),
                    windowBg.getColor().getU32(),
                    rounding.getFValue()
            );
            ImGui.getBackgroundDrawList().popClipRect();
            ImGui.getBackgroundDrawList().pushClipRect(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY() + padding.getFValue() * scale,
                    ImGui.getWindowPosX() + padding.getFValue() * scale,
                    ImGui.getWindowPosY() + padding.getFValue() * scale + headSize.getFValue() * scale
            );
            ImGui.getBackgroundDrawList().addRectFilled(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY(),
                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                    ImGui.getWindowPosY() + ImGui.getWindowHeight(),
                    windowBg.getColor().getU32(),
                    rounding.getFValue()
            );
            ImGui.getBackgroundDrawList().popClipRect();
            ImGui.getBackgroundDrawList().pushClipRect(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY() + padding.getFValue() * scale + headSize.getFValue() * scale,
                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                    ImGui.getWindowPosY() + ImGui.getWindowHeight()
            );
            ImGui.getBackgroundDrawList().addRectFilled(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY(),
                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                    ImGui.getWindowPosY() + ImGui.getWindowHeight(),
                    windowBg.getColor().getU32(),
                    rounding.getFValue()
            );
            ImGui.getBackgroundDrawList().popClipRect();

            ImGui.getBackgroundDrawList().pushClipRect(
                    ImGui.getWindowPosX() + padding.getFValue() * scale + headSize.getFValue() * scale,
                    ImGui.getWindowPosY() + padding.getFValue() * scale,
                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                    ImGui.getWindowPosY() + padding.getFValue() * scale + headSize.getFValue() * scale
            );
            ImGui.getBackgroundDrawList().addRectFilled(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY(),
                    ImGui.getWindowPosX() + ImGui.getWindowWidth(),
                    ImGui.getWindowPosY() + ImGui.getWindowHeight(),
                    windowBg.getColor().getU32(),
                    rounding.getFValue()
            );
            ImGui.getBackgroundDrawList().popClipRect();
             */

        if (blur.isEnabled()) {
            new Blur((int) (ImGui.getWindowPosX()), (int) (ImGui.getWindowPosY()), (int) (ImGui.getWindowSizeX()), (int) (ImGui.getWindowSizeY()), blurRadius.getFValue());
        }
        ImFont font18 = UI.getFont(18);
        font18.setScale(scale);
        ImFont boldFont18 = UI.getFont(18, true);
        boldFont18.setScale(scale);

        ImFont font24 = UI.getFont(24);
        font24.setScale(scale);
        ImFont boldFont24 = UI.getFont(24, true);
        boldFont24.setScale(scale);

        ImFont font32 = UI.getFont(32);
        font32.setScale(scale);
        ImFont boldFont32 = UI.getFont(32, true);
        boldFont32.setScale(scale);

        ImGui.pushFont(font24);

        if (gradientEnabled.isEnabled()) {
            float[] c1 = gradientColor1.getColor().getFloatColorWAlpha();
            float[] c2 = gradientColor2.getColor().getFloatColorWAlpha();

            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY(),
                    ImGui.getWindowPosX()+ImGui.getWindowSizeX(),
                    ImGui.getWindowPosY()+rounding.getFValue(),
                    ImGui.getColorU32(c1[0], c1[1], c1[2], c1[3]),
                    rounding.getFValue(), ImDrawFlags.RoundCornersTop
            );
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY()+ImGui.getWindowSizeY()-rounding.getFValue(),
                    ImGui.getWindowPosX()+ImGui.getWindowSizeX(),
                    ImGui.getWindowPosY()+ImGui.getWindowSizeY(),
                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                    rounding.getFValue(), ImDrawFlags.RoundCornersBottom
            );
            ImGui.getWindowDrawList().addRectFilledMultiColor(
                    ImGui.getWindowPosX(),
                    ImGui.getWindowPosY()+rounding.getFValue(),
                    ImGui.getWindowPosX()+ImGui.getWindowSizeX(),
                    ImGui.getWindowPosY()+ImGui.getWindowSizeY()-rounding.getFValue(),
                    ImGui.getColorU32(c1[0], c1[1], c1[2], c1[3]),
                    ImGui.getColorU32(c1[0], c1[1], c1[2], c1[3]),
                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3]),
                    ImGui.getColorU32(c2[0], c2[1], c2[2], c2[3])
            );
        }

        ImGui.setCursorPos(0, 0);
        ImVec2 pos = ImGui.getCursorPos();
        ImGui.setCursorPos(pos.x, pos.y);

        if (mc.player != null && target != null) {
            ImGui.setCursorPos(pos.x + padding.getFValue()*scale, pos.y + padding.getFValue()*scale);

            ImGui.setCursorPosX(ImGui.getCursorPosX());
            ImGui.setCursorPosY(ImGui.getCursorPosY());

            lasty = ImGui.getCursorScreenPosY();
            lastx = ImGui.getCursorScreenPosX();
            lastScale = scale;

            float[] hurtC = hurtColor.getColor().getFloatColorWAlpha();
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getWindowPosX()+pos.x+padding.getFValue()*scale,
                    ImGui.getWindowPosY()+pos.y+padding.getFValue()*scale,
                    ImGui.getWindowPosX()+pos.x+padding.getFValue()*scale+headSize.getFValue()*scale,
                    ImGui.getWindowPosY()+pos.y+padding.getFValue()*scale+headSize.getFValue()*scale,
                    ImGui.getColorU32(hurtC[0], hurtC[1], hurtC[2], hurtC[3]*(float)htRatioDivide(target))
            );

            ImGui.setCursorPos(scale*(padding.getFValue()+headSize.getFValue()+padding.getFValue()), scale*(padding.getFValue()+textOffsetTop.getFValue()));
            if (namePrefix.isEnabled()) {
                ImGui.text("Name: ");
                ImGui.sameLine(0,0);
            }
            if (boldName.isEnabled()) {
                if (bigName.isEnabled()) ImGui.pushFont(boldFont32);
                else ImGui.pushFont(boldFont24);
            } else {
                if (bigName.isEnabled()) ImGui.pushFont(font32);
                else ImGui.pushFont(font24);
            }
            float[] nameC = nameColor.getColor().getFloatColorWAlpha();
            ImGui.textColored(nameC[0], nameC[1], nameC[2], nameC[3], target.getName().getString());
            ImGui.popFont();
            ImGui.setCursorPos(pos.x + scale*(padding.getFValue()+headSize.getFValue()+padding.getFValue()), scale*(padding.getFValue()+headSize.getFValue()-textOffsetBottom.getFValue()));

            if (bottomBold.isEnabled()) {
                if (bottomTextSmall.isEnabled()) ImGui.pushFont(boldFont18);
                else ImGui.pushFont(boldFont24);
            } else {
                if (bottomTextSmall.isEnabled()) ImGui.pushFont(font18);
                else ImGui.pushFont(font24);
            }

            if (winLos.isEnabled()) {
                lol logic = lol.NA;
                if (mc.player != null) {
                    float logicTarget = target.getHealth() + target.getAbsorptionAmount() / target.getMaxHealth();
                    float us = mc.player.getHealth() + mc.player.getAbsorptionAmount() / mc.player.getMaxHealth();
                    if (logicTarget == us)
                        logic = winLosShort.isEnabled() ? lol.NA : lol.Equal;

                    if (logicTarget > us)
                        logic = winLosShort.isEnabled() ? lol.L : lol.Losing;
                    else if (logicTarget < us)
                        logic = winLosShort.isEnabled() ? lol.W : lol.Winning;
                }

                String winString = String.format("%s", logic.name());

                int r = 255;
                int g = 255;
                int b = 255;

                if (logic == lol.Winning || logic == lol.W) {
                    r = 0;
                    b = 0;
                } else if (logic == lol.Losing || logic == lol.L) {
                    g = 0;
                    b = 0;
                }

                if (logic != lol.NA) {
                    ImGui.textColored(r, g, b, 1f, winString);
                    ImGui.sameLine(0, 0);
                    ImGui.text(" ");
                    ImGui.sameLine(0, 0);
                }
            }
            if (hpNumber.isEnabled()) {
                DecimalFormat df = new DecimalFormat("#.#");
                String jesusChrist = df.format(target.getHealth()).replace(",", ".");
                String hpString = "HP: "+jesusChrist;

                ImGui.text(hpString);
                ImGui.sameLine(0, 0);
                ImGui.text(" ");
                ImGui.sameLine(0, 0);
            }

            if (hurtTime.isEnabled()) {

                String hurtString = String.format("Hurt: %d", target.hurtTime);

                ImGui.text(hurtString);

                ImGui.sameLine(0, 0);
                ImGui.text(" ");
                ImGui.sameLine(0, 0);
            }

            if (distance.isEnabled()) {
                DecimalFormat df = new DecimalFormat("#.#");
                String jesusChrist = df.format(target.distanceTo(mc.player)).replace(",", ".");
                String distString = "Dist: " + jesusChrist;

                ImGui.text(distString);

                ImGui.sameLine(0, 0);
                ImGui.text(" ");
                ImGui.sameLine(0, 0);
            }

            if (ping.isEnabled()) {
                if (mc.getNetworkHandler() == null) return;

                PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(target.getUuid());

                String pingString = String.format("Ping: %dms", playerListEntry != null ? playerListEntry.getLatency() : '0');

                ImGui.text(pingString);
                ImGui.sameLine(0, 0);
                ImGui.text(" ");
                ImGui.sameLine(0, 0);
            }

            if (totemPops.isEnabled()) {
                String totemString = String.format("Pops: %d", CrystalUtils.totemPops(target));

                ImGui.text(totemString);

                ImGui.sameLine(0, 0);
                ImGui.text(" ");
                ImGui.sameLine(0, 0);
            }
            ImGui.popFont();

            if (health.isEnabled()) {
                float health = target.getHealth();
                if (hpAnimate.isEnabled() && targetPlayer != null) {
                    float percentDuration = (System.currentTimeMillis() - healthChange) / hpAnimationDuration.getFValue();
                    if (percentDuration > 1f) percentDuration = 1f;
                    if (hpAnimationEasing.is(hpAnimationEasingEnum.EaseOutCubic))
                        percentDuration = EasingUtil.easeOutCubic(percentDuration);
                    if (hpAnimationEasing.is(hpAnimationEasingEnum.EaseOutQuart))
                        percentDuration = EasingUtil.easeOutQuart(percentDuration);
                    if (hpAnimationEasing.is(hpAnimationEasingEnum.EaseInCubic))
                        percentDuration = EasingUtil.easeInCubic(percentDuration);
                    if (hpAnimationEasing.is(hpAnimationEasingEnum.EaseInQuart))
                        percentDuration = EasingUtil.easeInQuart(percentDuration);
                    health = (float) (lastHealth - (lastHealth - currentHealth) * percentDuration);
                }

                float[] hp1 = hpBg.getColor().getFloatColorWAlpha();
                float[] hp2 = hpBar.getColor().getFloatColorWAlpha();

                float height = healthBarHeight.getFValue()*scale;
                ImGui.setCursorPos(pos.x + padding.getFValue()*scale, ImGui.getWindowSizeY() - height-padding.getFValue() * scale);
                if (!hpBottom.isEnabled())
                    ImGui.setCursorPos(pos.x + (padding.getFValue() + headSize.getFValue() + padding.getFValue()) * scale, (padding.getFValue() + headSize.getFValue() - hpOffset.getFValue()) * scale);
                float width = ImGui.getWindowWidth()-padding.getFValue() * 2 * scale;
                if (!hpBottom.isEnabled())
                    width = ImGui.getWindowWidth()-padding.getFValue() * 2 * scale - headSize.getFValue() * scale - padding.getFValue() * scale;

                ImGui.pushStyleColor(ImGuiCol.Button,        hp1[0], hp1[1], hp1[2], hp1[3]);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hp1[0], hp1[1], hp1[2], hp1[3]);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive,  hp1[0], hp1[1], hp1[2], hp1[3]);
                ImGui.button("##", width, height);
                ImGui.popStyleColor(3);

                ImGui.setCursorPos(pos.x + padding.getFValue()*scale, ImGui.getWindowSizeY()-height-padding.getFValue()*scale);
                if (!hpBottom.isEnabled())
                    ImGui.setCursorPos(pos.x + (padding.getFValue()+headSize.getFValue()+padding.getFValue())*scale, (padding.getFValue()+headSize.getFValue()-hpOffset.getFValue())*scale);

                ImGui.pushStyleColor(ImGuiCol.Button,        hp2[0], hp2[1], hp2[2], hp2[3]);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hp2[0], hp2[1], hp2[2], hp2[3]);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive,  hp2[0], hp2[1], hp2[2], hp2[3]);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, barRounding.getFValue());
                if (hpGradient.isEnabled()) {
                    JColor hpBarOne = hpBar1.getColor();
                    JColor hpBarTwo = hpBar2.getColor();

                    if (waveHP.isEnabled()) {
                        // Wave
                        JColor colorOne;
                        JColor colorTwo;

                        colorOne = new JColor(ColorUtil.interpolateColorsBackAndForth((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 1, UI.getColorOne(), UI.getColorTwo(), false));
                        colorTwo = new JColor(ColorUtil.interpolateColorsBackAndForth((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), waveHpIndex.getIValue(), UI.getColorOne(), UI.getColorTwo(), false));

                        if (rainbowEnabled.isEnabled()) {
                            colorOne = new JColor(ColorUtil.rainbow((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), 1, rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), 1f));
                            colorTwo = new JColor(ColorUtil.rainbow((int) (waveSpeed.getMaximum() + 1) - waveSpeed.getIValue(), waveHpIndex.getIValue(), rainbowSaturation.getFValue(), rainbowBrightness.getFValue(), 1f));
                        }
                        hpBarOne = colorOne;
                        hpBarTwo = colorTwo;
                    }

                    JColor gradient2 = UI.blendColors(hpBarOne, hpBarTwo, (health / target.getMaxHealth()));
                    
                    ImGui.getWindowDrawList().addRectFilled(
                            ImGui.getCursorScreenPosX(),
                            ImGui.getCursorScreenPosY(),
                            ImGui.getCursorScreenPosX()+barRounding.getFValue(),
                            ImGui.getCursorScreenPosY()+height,
                            hpBarOne.getU32(),
                            barRounding.getFValue(),
                            ImDrawFlags.RoundCornersLeft
                    );
                    ImGui.getWindowDrawList().addRectFilledMultiColor(
                            ImGui.getCursorScreenPosX()+barRounding.getFValue(),
                            ImGui.getCursorScreenPosY(),
                            ImGui.getCursorScreenPosX()+(width * (health / target.getMaxHealth()))-barRounding.getFValue(),
                            ImGui.getCursorScreenPosY()+height,
                            hpBarOne.getU32(),
                            gradient2.getU32(),
                            gradient2.getU32(),
                            hpBarOne.getU32()
                    );
                    ImGui.getWindowDrawList().addRectFilled(
                            ImGui.getCursorScreenPosX()+(width * (health / target.getMaxHealth()))-barRounding.getFValue(),
                            ImGui.getCursorScreenPosY(),
                            ImGui.getCursorScreenPosX()+(width * (health / target.getMaxHealth())),
                            ImGui.getCursorScreenPosY()+height,
                            gradient2.getU32(),
                            barRounding.getFValue(),
                            ImDrawFlags.RoundCornersRight
                    );
                } else {
                    ImGui.button("###", width * (health / target.getMaxHealth()), height);
                }
                ImGui.popStyleVar();
                ImGui.popStyleColor(3);
            }

            if (tiers.isEnabled() && target instanceof PlayerEntity) {
                boolean right = false;
                int y = (int) (80 * scale);
                HashMap<String, String> stringStringHashMap = PlayerUtils.processTiers(((PlayerEntity) target).getGameProfile().getName());

                if (stringStringHashMap != null) {
                    for (Map.Entry<String, String> tiers : stringStringHashMap.entrySet()) {
                        String tierString = String.format("%s %s", tiers.getKey(), tiers.getValue());

                        ImGui.setCursorPos(pos.x + (15*scale), pos.y + y);

                        if (!right) {
                            float windowWidth = ImGui.getWindowSize().x;
                            float textSize = ImGui.calcTextSize(tierString).x + (15 * scale);

                            ImGui.setCursorPosX(windowWidth - textSize);
                            right = true;
                        } else {
                            right = false;
                            y += (int) (20*scale);
                        }

                        ImGui.text(tierString);
                    }
                }
            }
        }

        // Glow
        if (glowEnabled.isEnabled()) {
            float[] glowColorF = glowColor.getColor().getFloatColorWAlpha();
            float alpha = glowColorF[3];

            if (waveGlow.isEnabled()) {
                //float[] glowColorOneF = colorOne.getFloatColorWAlpha();
                //float[] glowColorTwoF = colorTwo.getFloatColorWAlpha();
                //GuiUtils.drawGradientWindowShadow(
                //        new JColor(glowColorOneF[0], glowColorOneF[1], glowColorOneF[2], glowColorOneF[3]),
                //        new JColor(glowColorTwoF[0], glowColorTwoF[1], glowColorTwoF[2], glowColorTwoF[3]),
                //        glowSize.getFValue(), glowFill.isEnabled()
                //);
                if (rainbowEnabled.isEnabled()) {
                    cropHead(() ->  GuiUtils.drawRainbowWindowShadow(
                            rainbowSaturation.getFValue(),
                            rainbowBrightness.getFValue(),
                            waveSpeed.getFValue(),
                            waveGlowIndex.getFValue(),
                            glowSize.getFValue(),
                            glowFill.isEnabled()
                    ), scale, glowSize.getFValue());
                    //GuiUtils.drawRainbowWindowShadow(
                    //        rainbowSaturation.getFValue(),
                    //        rainbowBrightness.getFValue(),
                    //        waveSpeed.getFValue(),
                    //        waveGlowIndex.getFValue(),
                    //        glowSize.getFValue(),
                    //        glowFill.isEnabled()
                    //);
                } else {
                    cropHead(() ->  GuiUtils.drawWaveWindowShadow(
                            waveSpeed.getFValue(),
                            waveGlowIndex.getFValue(),
                            glowSize.getFValue(),
                            glowFill.isEnabled(),
                            alpha,
                            0
                    ), scale, glowSize.getFValue());
                    //GuiUtils.drawWaveWindowShadow(
                    //        waveSpeed.getFValue(),
                    //        waveGlowIndex.getFValue(),
                    //        glowSize.getFValue(),
                    //        glowFill.isEnabled(),
                    //        alpha,
                    //        0
                    //);
                }
            } else {
                GuiUtils.drawWindowShadow(
                        ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], alpha),
                        ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], 0f),
                        glowSize.getFValue(), glowFill.isEnabled()
                );
            }
        }

        if (targetFocus.is(targetFocusEnum.Off) && scale == 1f) super.position = ImGui.getWindowPos();
        isFocused = ImGui.isWindowFocused();

        ImGui.popFont();
        font18.setScale(1f);
        font24.setScale(1f);
        font32.setScale(1f);
        boldFont18.setScale(1f);
        boldFont24.setScale(1f);
        boldFont32.setScale(1f);
        ImGui.end();
        if (firstFrame) firstFrame = false;
        ImGui.popStyleColor(2);
        ImGui.getStyle().setItemSpacing(8f, 4f);
        ImGui.getStyle().setItemInnerSpacing(4f, 4f);
        ImGui.getStyle().setWindowRounding(8);
    }

    private double htRatioDivide(LivingEntity ent) {
        double percent = (double) ent.hurtTime / (double) ent.maxHurtTime;
        percent = percent*hurtDuration.getFValue();

        if (hurtRepeat.isEnabled()) {
            if (hurtEasing.isEnabled()) {
                percent = percent * 2;
                if (percent >= 1f) percent = EasingUtil.easeInQuad((float) (2f - percent));
                else percent = EasingUtil.easeOutQuad((float) (percent));
            } else {
                percent = percent * 2;
                if (percent >= 1f) percent = 2f - percent;
                return percent;
            }
        }
        return percent;
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
            colors[ImGuiCol.WindowBg] = new float[]{0.13f, 0.14f, 0.19f, 0.8f};
            colors[ImGuiCol.ChildBg] = new float[]{0.14f, 0.16f, 0.22f, 1.00f};
            colors[ImGuiCol.PopupBg] = new float[]{0.13f, 0.14f, 0.19f, 0.94f};
            colors[ImGuiCol.Border] = new float[]{0.21f, 0.24f, 0.31f, 0.00f};
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

            ImGui.getStyle().setButtonTextAlign(0, 0.5f);

            ImGui.getStyle().setWindowPadding(0f, 0f);
            ImGui.getStyle().setFramePadding(0f, 0f);
            ImGui.getStyle().setCellPadding(0f, 0f);

            ImGui.getStyle().setItemSpacing(8f, 4f);
            ImGui.getStyle().setItemInnerSpacing(4f, 4f);

            if (ImguiLoader.poppins24 != null) {
                ImGui.pushFont(ImguiLoader.poppins24);
            }
        }

        @Override
        public void postRender() {
            if (ImguiLoader.poppins24 != null) {
                ImGui.popFont();
            }
        }
    };

    public void cropHead(Runnable run, float scale, float offset) {
        ImGui.getBackgroundDrawList().pushClipRect(
                ImGui.getWindowPosX()-offset,
                ImGui.getWindowPosY()-offset,
                ImGui.getWindowPosX()+ImGui.getWindowWidth()+offset,
                ImGui.getWindowPosY()+padding.getFValue()*scale
        );
        run.run();
        ImGui.getBackgroundDrawList().popClipRect();
        ImGui.getBackgroundDrawList().pushClipRect(
                ImGui.getWindowPosX()-offset,
                ImGui.getWindowPosY()+padding.getFValue()*scale,
                ImGui.getWindowPosX()+padding.getFValue()*scale,
                ImGui.getWindowPosY()+padding.getFValue()*scale+headSize.getFValue()*scale
        );
        run.run();
        ImGui.getBackgroundDrawList().popClipRect();
        ImGui.getBackgroundDrawList().pushClipRect(
                ImGui.getWindowPosX()-offset,
                ImGui.getWindowPosY()+padding.getFValue()*scale+headSize.getFValue()*scale,
                ImGui.getWindowPosX()+ImGui.getWindowWidth()+offset,
                ImGui.getWindowPosY()+ImGui.getWindowHeight()+offset
        );
        run.run();
        ImGui.getBackgroundDrawList().popClipRect();

        ImGui.getBackgroundDrawList().pushClipRect(
                ImGui.getWindowPosX()+padding.getFValue()*scale+headSize.getFValue()*scale,
                ImGui.getWindowPosY()+padding.getFValue()*scale,
                ImGui.getWindowPosX()+ImGui.getWindowWidth()+offset,
                ImGui.getWindowPosY()+padding.getFValue()*scale+headSize.getFValue()*scale
        );
        run.run();
        ImGui.getBackgroundDrawList().popClipRect();
    }
}
