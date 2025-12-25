/*
package template.rip.deprecated;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import template.rip.api.blockesp.WorldRenderContext;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.events.InvisibleEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.Rectangle;
import template.rip.api.util.CrystalUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ColorSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ESPModule extends Module {

    public enum modeEnum{Corner, Rectangle, Box, Chams*/
/*, Glow*//*
, Skeleton, None}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Corner, "Mode");

    public enum healthBarEnum{Left, Right, Off}
    public final ModeSetting<healthBarEnum> healthBar = new ModeSetting<>(this, healthBarEnum.Left, "HP ESP mode");

    public final ColorSetting friendColor = new ColorSetting(this, new JColor(0f, 1f, 1f), false, "Friend Color");
    public final ColorSetting focusColor = new ColorSetting(this, new JColor(1f, 0f, 1f), false, "Focus Color");
    public final BooleanSetting tracers = new BooleanSetting(this, true, "Tracers");
    private final NumberSetting alphaVal = new NumberSetting(this, 100d, 0d, 255d, 1d, "Alpha");
    public final BooleanSetting invisibles = new BooleanSetting(this, true, "Render invisible entities");
    private final NumberSetting renders = new NumberSetting(this, 64d, 0d, 1000d, 1d, "Max Entities rendered");
    public ESPModule() {
        super(Category.RENDER, Description.of("Extrasensory perception of other entities"), "ESP");
    }
    int green = 255;
    int red = 255;
    int blue = 255;
    int alpha = 255;
    Vec3d origin = Vec3d.ZERO;
    Matrix4f posMat;
    BufferBuilder buffy;
    public List<Entity> espEntity = new ArrayList<>();
    @Override
    public void onDisable() {
        espEntity.clear();
    }

    @EventHandler
    private void onHUDRender(HudRenderEvent event) {
        if (!nullCheck())
            return;

        if (tracers.isEnabled()) {
            for (Map.Entry<Entity, Pair<Rectangle, Boolean>> e : CrystalUtils.getEntrySet()) {
                if (mc.world.getEntityById(e.getKey().getId()) == null || !PlayerUtils.findTargets(true).contains(e.getKey())) {
                    continue;
                }

                Rectangle r = e.getValue().getLeft();
                Vec2f center = new Vec2f(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f);
                Vec2f entity = new Vec2f((float) ((r.x + r.z) / 2f), (float) ((r.y + r.w) / 2f));
                if (!e.getValue().getRight()) {
                    entity = entity.add(center.multiply(-1)).multiply(Math.max(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()));
                }
                if (e.getKey() instanceof PlayerEntity && PlayerUtils.isFocus((PlayerEntity) e.getKey())) {
                    red = focusColor.getColor().getRed();
                    green = focusColor.getColor().getGreen();
                    blue = focusColor.getColor().getBlue();
                } else if (e.getKey() instanceof PlayerEntity && PlayerUtils.isFriend((PlayerEntity) e.getKey())) {
                    red = friendColor.getColor().getRed();
                    green = friendColor.getColor().getGreen();
                    blue = friendColor.getColor().getBlue();
                } else {
                    int col = e.getKey().getTeamColorValue();
                    red = (col >> 16) & 0xFF;
                    green = (col >> 8) & 0xFF;
                    blue = col & 0xFF;
                }
                RenderUtils.Render2D.line(center, entity, new JColor(red, green, blue), alphaVal.getIValue(), 1f, event.context);
            }
        }
    }
    @EventHandler
    private void onInvisible(InvisibleEvent event) {
        if (invisibles.isEnabled())
            event.invisible = false;
    }
    @EventHandler
    private void onRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null)
            return;
        Iterable<? extends Entity> ents = PlayerUtils.findTargets(true);
        int i = 0;
        for (Entity ent : ents) {
            if (i >= renders.value)
                break;
            if (ent != mc.player && ent instanceof LivingEntity && !(ent instanceof AnimalEntity)) {
                LivingEntity le = (LivingEntity) ent;
                if (le instanceof PlayerEntity && PlayerUtils.isFocus((PlayerEntity) le)) {
                    red = focusColor.getColor().getRed();
                    green = focusColor.getColor().getGreen();
                    blue = focusColor.getColor().getBlue();
                } else if (le instanceof PlayerEntity && PlayerUtils.isFriend((PlayerEntity) le)) {
                    red = friendColor.getColor().getRed();
                    green = friendColor.getColor().getGreen();
                    blue = friendColor.getColor().getBlue();
                } else {
                    int col = le.getTeamColorValue();
                    red = (col >> 16) & 0xFF;
                    green = (col >> 8) & 0xFF;
                    blue = col & 0xFF;
                }
                alpha = alphaVal.getIValue();
                */
/*if (tracers.isEnabled()) {
                    RenderUtils.Render3D.renderLineTo(MathUtils.smoothVec3d(new Vec3d(mc.player.prevX, mc.player.prevY, mc.player.prevZ), mc.player.getPos(), mc.getRenderTickCounter().getTickDelta(false)), MathUtils.smoothVec3d(new Vec3d(le.prevX, le.prevY, le.prevZ), le.getPos(), mc.getRenderTickCounter().getTickDelta(false)), red, green, blue, alpha, 1f, event.context);
                }*//*

                switch (mode.getMode()) {
                    case Rectangle : drawCornerESP(le, event.context); break;
                    case Corner : drawCornerESP(le, event.context); break;
                    case Box: RenderUtils.Render3D.renderBox(le.getBoundingBox(), red, green, blue, alpha, event.context); break;
                    case Chams: {
                        if (le instanceof PlayerEntity) {
                            PlayerEntityModel<PlayerEntity> modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
                            modelPlayer.getHead().scale(new Vector3f(-0.2f, -0.2f, -0.2f));
                            renderEntity(event.context.matrixStack(), le, modelPlayer);
                        } else {
                            RenderUtils.Render3D.renderBox(le.getBoundingBox(), red, green, blue, alpha, event.context);
                        }
                        break;

                    }
                    case Skeleton: {
                        if (le instanceof PlayerEntity) {
                            PlayerEntityModel<PlayerEntity> modelPlayer = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
                            modelPlayer.getHead().scale(new Vector3f(-0.87f, -0.3f, -0.87f));
                            modelPlayer.getBodyParts().forEach(part -> part.scale(new Vector3f(-0.75f, 0, -0.75f)));
                            renderEntity(event.context.matrixStack(), le, modelPlayer);
                        } else {
                            RenderUtils.Render3D.renderBox(le.getBoundingBox(), red, green, blue, alpha, event.context);
                        }
                    }
                    default:
                }
                if (!healthBar.is(healthBarEnum.Off)) {
                    healthESP(le, event.context);
                }
                espEntity.add(le);
                i++;
            }
        }
    }
    public void healthESP(LivingEntity entity, WorldRenderContext context) {
        Camera cam = context.camera();


        float width = entity.getWidth() * 1.5f;
        float height = entity.getHeight() / 1.15f;

        origin = MathUtils.smoothVec3d( new Vec3d(entity.prevX, entity.prevY + (entity.getHeight() / 2) - height, entity.prevZ), entity.getPos().add(0.0, (entity.getHeight() / 2) - height, 0.0), mc.getRenderTickCounter().getTickDelta(false));
        Vec3d targetpos = origin.subtract(cam.getPos());

        MatrixStack matstack = new MatrixStack();
        matstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
        matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));
        matstack.translate(targetpos.x, targetpos.y, targetpos.z);
        matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360F - (cam.getYaw() + 180.0F)));
        posMat = matstack.peek().getPositionMatrix();
        Tessellator tessy = Tessellator.getInstance();
        buffy = tessy.getBuffer();

        buffy.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);


        float x;
        switch (healthBar.getMode()) {
            case Left : x = (width + 0.4f) * -1f; break;
            case Right : x = (width + 0.4f); break;
            default : x = 0.0f; break;
        }
        red = hpRatio(entity).getRight();
        green = hpRatio(entity).getLeft();
        blue = 0;
        float healthBar = height * (Math.min((entity.getHealth() + entity.getAbsorptionAmount()) / entity.getMaxHealth(), 1f) * 2f);
        height *= 2;
        renderSquare(x - 0.08f, 0, x + 0.08f, healthBar);

        red = 0;
        green = 0;
        blue = 0;

        renderSquare(x - 0.1f, 0, x - 0.08f, height);
        renderSquare(x + 0.08f, 0, x + 0.1f, height);

        renderSquare(x - 0.1f, -0.02f, x + 0.1f, 0f);
        renderSquare(x - 0.1f, height, x + 0.1f, height + 0.02f);




        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        tessy.draw();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    private Pair<Integer, Integer> hpRatio(LivingEntity ent) {
        float divide = ent.getHealth() / ent.getMaxHealth();
        int red = Math.round(255 * divide);
        int green = 255 - Math.round(255 * divide);
        if (ent.getAbsorptionAmount() > 0f) {
            red = 255;
            green = 255;
        }
        return new Pair<>(red, green);
    }
    // from augustus 2.6, fake 2d corner esp
    public void drawCornerESP(LivingEntity entity, WorldRenderContext context) {
        Camera cam = context.camera();

        origin = MathUtils.smoothVec3d( new Vec3d(entity.prevX, entity.prevY + (entity.getHeight() / 2), entity.prevZ), entity.getPos().add(0.0, entity.getHeight() / 2, 0.0), mc.getRenderTickCounter().getTickDelta(false));
        Vec3d targetpos = origin.subtract(cam.getPos());

        MatrixStack matstack = new MatrixStack();
        matstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
        matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));
        matstack.translate(targetpos.x, targetpos.y, targetpos.z);
        matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360F - (cam.getYaw() + 180.0F)));
        posMat = matstack.peek().getPositionMatrix();
        Tessellator tessy = Tessellator.getInstance();
        buffy = tessy.getBuffer();

        buffy.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        float width = entity.getWidth() * 1.5f;
        float height = entity.getHeight() / 1.15f;

        if (mode.is(modeEnum.Rectangle)) {
            renderSquare(-width, -height, width, -height + 0.1f);
            renderSquare(-width, height, width, height - 0.1f);
            renderSquare(width, -height, width + 0.1f, height);
            renderSquare(-width, -height, -width - 0.1f, height);
        }
        if (mode.is(modeEnum.Corner)) {
            renderSquare(width, height - 0.1f, width - 0.6f, height);
            renderSquare(-width, height - 0.1f, -width + 0.6f, height);
            renderSquare(-width, height - 0.1f, -width + 0.6f, height);
            renderSquare(-width, height, -width + 0.1f, height - 0.6f);
            renderSquare(width, height, width - 0.1f, height - 0.6f);
            renderSquare(width, -height, width - 0.6f, -height + 0.1f);
            renderSquare(-width, -height, -width + 0.6f, -height + 0.1f);
            renderSquare(-width, -height + 0.1f, -width + 0.1f, -height + 0.6f);
            renderSquare(width, -height + 0.1f, width - 0.1f, -height + 0.6f);
        }
        green = 0;
        blue = 0;
        red = 0;
        if (mode.is(modeEnum.Rectangle)) {
            renderSquare(-width - 0.13f, -height, width + 0.13f, -height - 0.03f);
            renderSquare(-width - 0.13f, height, width + 0.13f, height + 0.03f);
            renderSquare(width + 0.1f, -height, width + 0.13f, height);
            renderSquare(-width - 0.1f, -height, -width - 0.13f, height);
        }
        if (mode.is(modeEnum.Corner)) {
            renderSquare(width, height, width - 0.6f, height + 0.03f);
            renderSquare(-width, height, -width + 0.6f, height + 0.03f);
            renderSquare(-width - 0.03f, height + 0.03f, -width, height - 0.6f);
            renderSquare(width + 0.03f, height + 0.03f, width, height - 0.6f);
            renderSquare(width + 0.03f, -height, width - 0.6f, -height - 0.03f);
            renderSquare(-width - 0.03f, -height, -width + 0.6f, -height - 0.03f);
            renderSquare(-width - 0.03f, -height, -width, -height + 0.6f);
            renderSquare(width + 0.03f, -height, width, -height + 0.6f);
        }


        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        tessy.draw();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    public void renderSquare(float minx, float miny, float maxx, float maxy) {
        buffy.vertex(posMat, maxx, miny, 0).color(red, green, blue, alpha);
        buffy.vertex(posMat, minx, miny, 0).color(red, green, blue, alpha);
        buffy.vertex(posMat, minx, maxy, 0).color(red, green, blue, alpha);
        buffy.vertex(posMat, maxx, maxy, 0).color(red, green, blue, alpha);
    }

    // from thunder hack cuz i ain't doing allat
    private void renderEntity(MatrixStack matrices, LivingEntity entity, BipedEntityModel<PlayerEntity> modelBase) {
        Vec3d vec = entity.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false)).subtract(mc.getEntityRenderDispatcher().camera.getPos());

        entity.setPos(entity.getPos().x, entity.getPos().y, entity.getPos().z);

        matrices.push();
        matrices.translate((float) vec.x, (float) vec.y, (float) vec.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) ((180 - entity.bodyYaw) * Math.PI / 180)));
        prepareScale(matrices);

        modelBase.animateModel((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), mc.getRenderTickCounter().getTickDelta(false));
        modelBase.setAngles((PlayerEntity) entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());

        RenderSystem.enableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        modelBase.render(matrices, buffer, 10, 0, red / 255f, green / 255f, blue / 255f, alpha / 255f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        tessellator.draw();
        matrices.pop();

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

    }

    private static void prepareScale(MatrixStack matrixStack) {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
    }




}
*/
