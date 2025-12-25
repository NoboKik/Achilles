package template.rip.api.util;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Pair;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import template.rip.api.blockesp.WorldRenderContext;
import template.rip.api.object.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static template.rip.Template.mc;

public class RenderUtils {

    public static void n() {
        ImGui.getBackgroundDrawList().addRect(-1, -1, -2, -2, 0);
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static class Render2D {

        public static void renderQuad(MatrixStack matrices, Color c, double x1, double y1, double x2, double y2) {
            double x11 = x1;
            double x21 = x2;
            double y11 = y1;
            double y21 = y2;
            int color = c.getRGB();
            double j;
            if (x11 < x21) {
                j = x11;
                x11 = x21;
                x21 = j;
            }

            if (y11 < y21) {
                j = y11;
                y11 = y21;
                y21 = j;
            }
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float f = (float) (color >> 24 & 255) / 255.0F;
            float g = (float) (color >> 16 & 255) / 255.0F;
            float h = (float) (color >> 8 & 255) / 255.0F;
            float k = (float) (color & 255) / 255.0F;
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            setupRender();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            bufferBuilder.vertex(matrix, (float) x11, (float) y21, 0.0F).color(g, h, k, f);
            bufferBuilder.vertex(matrix, (float) x21, (float) y21, 0.0F).color(g, h, k, f);
            bufferBuilder.vertex(matrix, (float) x21, (float) y11, 0.0F).color(g, h, k, f);
            bufferBuilder.vertex(matrix, (float) x11, (float) y11, 0.0F).color(g, h, k, f);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            endRender();
        }

        public static void renderCircle(MatrixStack matrices, Color c, double originX, double originY, double rad, int segments) {
            int segments1 = MathHelper.clamp(segments, 4, 360);
            int color = c.getRGB();

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float f = (float) (color >> 24 & 255) / 255.0F;
            float g = (float) (color >> 16 & 255) / 255.0F;
            float h = (float) (color >> 8 & 255) / 255.0F;
            float k = (float) (color & 255) / 255.0F;
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);;
            setupRender();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            for (int i = 0; i < 360; i += Math.min(360 / segments1, 360 - i)) {
                double radians = Math.toRadians(i);
                double sin = Math.sin(radians) * rad;
                double cos = Math.cos(radians) * rad;
                bufferBuilder.vertex(matrix, (float) (originX + sin), (float) (originY + cos), 0).color(g, h, k, f);
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            endRender();
        }

        public static void draw(DrawContext context, Color color, template.rip.api.object.Rectangle rectangle) {
            renderQuad(context.getMatrices(), color, rectangle.x, rectangle.y, rectangle.z, rectangle.w);
        }

        public static void line(Vec2f from, Vec2f to, Color c, int alpha, float width, DrawContext context) {
            MatrixStack matstack = context.getMatrices();

            float x1 = from.x;
            float y1 = from.y;
            float x2 = to.x;
            float y2 = to.y;

            Matrix4f posMat = matstack.peek().getPositionMatrix();
            Tessellator tessy = Tessellator.getInstance();
            BufferBuilder buffy = tessy.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            buffy.vertex(posMat, x1, y1, 0).color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
            buffy.vertex(posMat, x2, y2, 0).color(c.getRed(), c.getGreen(), c.getBlue(), alpha);

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            float lineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);

            BufferRenderer.drawWithGlobalProgram(buffy.end());

            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            GL11.glLineWidth(lineWidth);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);

        }
    }

    public static class Render3D {

        public static void renderCircle(Color one, Color two, Vec3d origin, double rad, int segments, float width, float widthIncrement, WorldRenderContext context) {
            int segments1 = MathHelper.clamp(segments, 4, 360);

            Camera cam = context.camera();

            MatrixStack matstack = new MatrixStack();

            matstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
            matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));

            Vec3d targetpos = origin.subtract(cam.getPos());
            matstack.translate(targetpos.x, targetpos.y, targetpos.z);

            Matrix4f posMat = matstack.peek().getPositionMatrix();
            Tessellator tessy = Tessellator.getInstance();
            BufferBuilder buffy = tessy.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

            for (float j = 0; j < width; j += widthIncrement) {
                float delta = j / width;
                int r = MathHelper.lerp(delta, one.getRed(), two.getRed());
                int g = MathHelper.lerp(delta, one.getGreen(), two.getGreen());
                int b = MathHelper.lerp(delta, one.getBlue(), two.getBlue());
                int a = MathHelper.lerp(delta, one.getAlpha(), two.getAlpha());
                for (int i = 0; i < 360; i += Math.min(360 / segments1, 360 - i)) {
                    double radians = Math.toRadians(i);
                    double sin = Math.sin(radians) * rad;
                    double cos = Math.cos(radians) * rad;
                    buffy.vertex(posMat, (float) sin, width - j, (float) cos).color(r, g, b, a);
                }
            }

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            float lineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);

            BufferRenderer.drawWithGlobalProgram(buffy.end());

            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            GL11.glLineWidth(lineWidth);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }

        public static void renderLine(MatrixStack matrices, Color color, Vec3d start, Vec3d end) {
            Matrix4f s = matrices.peek().getPositionMatrix();
            genericAABBRender(
                    ShaderProgramKeys.POSITION_COLOR,
                    s,
                    start,
                    end.subtract(start),
                    color,
                    (buffer, x, y, z, x1, y1, z1, red, green, blue, alpha, matrix) -> {
                        buffer.vertex(matrix, x, y, z).color(red, green, blue, alpha);
                        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);
                    }
            );
        }

        public static void renderLineTo(Vec3d from, Vec3d to, Color color, float width, WorldRenderContext context) {
            renderLineTo(from, to, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), width, context);
        }

        public static void renderLineTo(Vec3d from, Vec3d to, int red, int green, int blue, int alpha, float width, WorldRenderContext context) {
            Camera cam = context.camera();

            MatrixStack matstack = new MatrixStack();

            matstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
            matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));

            Vec3d targetpos = from.subtract(cam.getPos());
            matstack.translate(targetpos.x, targetpos.y, targetpos.z);

            to = to.subtract(from);
            from = Vec3d.ZERO;

            float x1 = (float) from.x;
            float y1 = (float) from.y;
            float z1 = (float) from.z;
            float x2 = (float) to.x;
            float y2 = (float) to.y;
            float z2 = (float) to.z;

            Matrix4f posMat = matstack.peek().getPositionMatrix();
            Tessellator tessy = Tessellator.getInstance();
            BufferBuilder buffy = tessy.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            buffy.vertex(posMat, x1, y1, z1).color(red, green, blue, alpha);
            buffy.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha);

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            float lineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);

            BufferRenderer.drawWithGlobalProgram(buffy.end());

            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            GL11.glLineWidth(lineWidth);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }

        public static void renderBox(Vec3d from, Vec3d to, int red, int green, int blue, int alpha , WorldRenderContext context) {
           renderBox(new Box(from, to), red, green, blue, alpha, context);
        }

        public static void renderBox(Vec3d from, Vec3d to, Color color, int alpha, WorldRenderContext context) {
            renderBox(new Box(from, to), color, alpha, context);
        }

        public static void renderBox(Box box, Color color, int alpha, WorldRenderContext context) {
            renderBox(box, color.getRed(), color.getGreen(), color.getBlue(), alpha, context);
        }

        public static void renderBox(Box box, int red, int green, int blue, int alpha, WorldRenderContext context) {
            renderBox(box, red, green, blue, alpha, true, context);
        }

        public static void renderBox(Box box, int red, int green, int blue, int alpha, boolean fullCube, WorldRenderContext context) {
            // copy and pasted from my optimal aim mod, I skidded from myself
            Camera cam = context.camera();

            MatrixStack matstack = new MatrixStack();

            matstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
            matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));

            Vec3d targetpos = new Vec3d(box.minX, box.minY, box.minZ).subtract(cam.getPos());
            matstack.translate(targetpos.x, targetpos.y, targetpos.z);

            box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

            float x1 = (float) box.minX;
            float y1 = (float) box.minY;
            float z1 = (float) box.minZ;
            float x2 = (float) box.maxX;
            float y2 = (float) box.maxY;
            float z2 = (float) box.maxZ;

            Matrix4f posMat = matstack.peek().getPositionMatrix();
            Tessellator tessy = Tessellator.getInstance();
            BufferBuilder buffy = tessy.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            //up
            buffy.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha);
            buffy.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha);
            buffy.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha);
            buffy.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha);

            if (fullCube) {
                //north
                buffy.vertex(posMat, x1, y1, z1).color(red, green, blue, alpha);
                buffy.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y1, z1).color(red, green, blue, alpha);

                //west
                buffy.vertex(posMat, x1, y1, z1).color(red, green, blue, alpha);
                buffy.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha);
                buffy.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha);
                buffy.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha);

                //down
                buffy.vertex(posMat, x1, y1, z1).color(red, green, blue, alpha);
                buffy.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y1, z1).color(red, green, blue, alpha);

                //east
                buffy.vertex(posMat, x2, y1, z1).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha);

                //south
                buffy.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha);
                buffy.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha);
                buffy.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha);
            }

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.depthFunc(GL11.GL_ALWAYS);

            BufferRenderer.drawWithGlobalProgram(buffy.end());

            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }

        public static void drawPlayer(AbstractClientPlayerEntity player, Vec3d at, MatrixStack matrices, float alpha) {
            /*PlayerEntityModel<PlayerEntity> modelBase = new PlayerEntityModel<>(new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer).getPart(EntityModelLayers.PLAYER), false);
            modelBase.getHead().scale(new Vector3f(-0.3f, -0.3f, -0.3f));

            Vec3d vec = at.subtract(mc.getEntityRenderDispatcher().camera.getPos());

            matrices.push();
            matrices.translate((float) vec.x, (float) vec.y, (float) vec.z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) ((180 - player.bodyYaw) * Math.PI / 180)));

            matrices.scale(-1.0F, -1.0F, 1.0F);
            matrices.scale(1.6f, 1.8f, 1.6f);
            matrices.translate(0.0F, -1.501F, 0.0F);

            modelBase.animateModel(player, player.limbAnimator.getPos(), player.limbAnimator.getSpeed(), mc.getRenderTickCounter().getTickDelta(false));
            modelBase.setAngles(player, player.limbAnimator.getPos(), player.limbAnimator.getSpeed(), player.age, player.headYaw - player.bodyYaw, player.getPitch());

            EntityRendererFactory.Context context = new EntityRendererFactory.Context(mc.getEntityRenderDispatcher(), mc.getItemRenderer(), mc.getBlockRenderManager(), mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(), mc.getEntityModelLoader(), mc.textRenderer);
//          boolean slim = !player.getModel().equalsIgnoreCase("default");
            PlayerEntityRenderer per = new PlayerEntityRenderer(context, false);

            VertexConsumerProvider.Immediate vcp = mc.getBufferBuilders().getEntityVertexConsumers();
            RenderLayer renderLayer = per.getRenderLayer(player, true, true, false);
            VertexConsumer vertexConsumer = vcp.getBuffer(renderLayer);

            modelBase.render(matrices, vertexConsumer, 16, 0);

            vcp.draw();*/
        }

        private static void genericAABBRender(ShaderProgramKey shader, Matrix4f stack, Vec3d start, Vec3d dimensions, Color color, RenderAction action) {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float alpha = color.getAlpha() / 255f;
            Vec3d end = start.add(dimensions);
            float x1 = (float) start.x;
            float y1 = (float) start.y;
            float z1 = (float) start.z;
            float x2 = (float) end.x;
            float y2 = (float) end.y;
            float z2 = (float) end.z;
            useBuffer(shader, bufferBuilder -> action.run(bufferBuilder, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, stack));
        }

        private static void useBuffer(ShaderProgramKey shader, Consumer<BufferBuilder> runner) {
            Tessellator t = Tessellator.getInstance();
            BufferBuilder bb = t.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            runner.accept(bb);

            setupRender();
            RenderSystem.setShader(shader);
            BufferRenderer.drawWithGlobalProgram(bb.end());
            endRender();
        }

        public static Pair<Rectangle, Boolean> twoDeePosition(Entity entity, Matrix4f position, Matrix4f proj) {
            Vec3d prevPos = new Vec3d(entity.lastRenderX, entity.lastRenderY, entity.lastRenderZ);
            Vec3d interp = prevPos.add((entity.getPos().subtract(prevPos)).multiply(mc.getRenderTickCounter().getTickDelta(false)));
            Box box = entity.getBoundingBox().offset(interp.subtract(entity.getPos()));
            return twoDeePosition(box, position, proj);
        }

        // context.matrixStack().peek().getPositionMatrix(), context.projectionMatrix()
        public static Pair<Rectangle, Boolean> twoDeePosition(Box boundingBox, Matrix4f position, Matrix4f proj) {
            List<Vec3d> corners = Arrays.asList(
                    new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                    new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                    new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                    new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),

                    new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                    new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                    new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                    new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ)
            );

            Rectangle rectangle = null;

            boolean visible = false;

            for (Vec3d corner : corners) {
                Pair<Vec3d, Boolean> projection = project(position, proj, corner);
                if (projection.getRight()) visible = true;
                Vec3d projected = projection.getLeft();
                if (rectangle == null)
                    rectangle = new Rectangle(projected.x, projected.y, projected.x, projected.y);
                else {
                    if (rectangle.x > projected.x)
                        rectangle.x = projected.x;

                    if (rectangle.y > projected.y)
                        rectangle.y = projected.y;

                    if (rectangle.z < projected.x)
                        rectangle.z = projected.x;

                    if (rectangle.w < projected.y)
                        rectangle.w = projected.y;
                }
            }

            return new Pair<>(rectangle, visible);
        }

        static Pair<Vec3d, Boolean> project(Matrix4f modelView, Matrix4f projection, Vec3d vector)  {
            Vec3d camPos = vector.subtract(mc.gameRenderer.getCamera().getPos());
            Vector4f vec1 = new Vector4f((float) camPos.x, (float) camPos.y, (float) camPos.z, 1F).mul(modelView);
            Vector4f screenPos = vec1.mul(projection);

            double newW = 1.0 / screenPos.w * 0.5;

            Vec3d position = new Vec3d(screenPos.x * newW + 0.5, screenPos.y * newW + 0.5, screenPos.z * newW + 0.5);
            position = new Vec3d(position.x * mc.getWindow().getScaledWidth(), (1.0 - position.y) * mc.getWindow().getScaledHeight(), position.z);

            if (screenPos.w <= 0.0) {
                position = new Vec3d(mc.getWindow().getScaledWidth() - position.x, mc.getWindow().getScaledHeight() - position.y, screenPos.z);
            }

            return new Pair<>(position, (screenPos.w > 0.0));
        }

        public static void renderPrediction(Pair<List<Vec3d>, HitResult> pair, boolean renderEnd, Color color, int alpha, WorldRenderContext context) {
            List<Vec3d> vecs = pair.getLeft();
            HitResult hit = pair.getRight();
            Vec3d last = null;
            Color color1 = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

            for (Vec3d vec3d : vecs) {
                if (last != null)
                    RenderUtils.Render3D.renderLineTo(last, vec3d, color1, 1f, context);
                last = vec3d;
            }

            if (renderEnd) {
                if (hit instanceof EntityHitResult result && (result.getEntity() != mc.player || !mc.options.getPerspective().isFirstPerson())) {
                    RenderUtils.Render3D.renderBox(result.getEntity().getBoundingBox(), color, alpha, context);
                }
                if (hit instanceof BlockHitResult result) {
                    BlockPos pos = result.getBlockPos();
                    VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
                    if (!shape.isEmpty()) {
                        Box box = shape.offset(pos.getX(), pos.getY(), pos.getZ()).getBoundingBox();
                        RenderUtils.Render3D.renderBox(box, color, alpha, context);
                    }
                }
            }
        }

        interface RenderAction {
            void run(BufferBuilder buffer, float x, float y, float z, float x1, float y1, float z1, float red, float green, float blue, float alpha, Matrix4f matrix);
        }
    }

    public static class ImGuiHelper {

        private static final HashMap<String, Integer> images = new HashMap<>();

        public static int bindImage(String resourcesPath) {
            if (images.containsKey(resourcesPath)) {
                return images.get(resourcesPath);
            }

            BufferedImage image;

            try {
                image = ImageIO.read(RenderUtils.class.getClassLoader().getResource(resourcesPath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
            Color c;

            for (int y1 = 0; y1 < image.getHeight(); y1++) {
                for (int x1 = 0; x1 < image.getWidth(); x1++) {
                    c = new Color(image.getRGB(x1, y1));
                    buffer.put((byte) c.getRed());     // Red component
                    buffer.put((byte) c.getGreen());      // Green component
                    buffer.put((byte) c.getBlue());               // Blue component
                    buffer.put((byte) c.getAlpha());    // Alpha component. Only for RGBA
                }
            }

            buffer.flip();

            int textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

            //Setup wrap mode
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            //Setup texture scaling filtering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            //Send texel data to OpenGL
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

            images.put(resourcesPath, textureID);

            return textureID;
        }
    }

    public static void renderHotbarItem(DrawContext context, float x, float y, float f, LivingEntity entity, ItemStack stack, int seed, float scale) {
        if (!stack.isEmpty()) {
            drawItem(context, entity, mc.world, stack, (int) x, (int) y, seed, -1, scale);
            drawItemCount(context, mc.textRenderer, stack, (int) x, (int) y, null, scale);
        }
    }

    private static void drawItem(DrawContext context, @Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z, float scale) {
        if (!stack.isEmpty()) {
            mc.getItemModelManager().update(context.itemRenderState, stack, ModelTransformationMode.GUI, false, world, entity, seed);
            //BakedModel bakedModel = mc.getItemRenderer().getModel(stack, world, entity, seed);
            context.getMatrices().push();
            context.getMatrices().translate((float)(x + 8), (float)(y + 8), (float)(150 + (context.itemRenderState.hasDepth() ? z : 0)));

            try {
                context.getMatrices().multiplyPositionMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
                context.getMatrices().scale(16.0F*scale, 16.0F*scale, 16.0F*scale);
                boolean bl = !context.itemRenderState.isSideLit();
                if (bl) {
                    context.draw();
                    DiffuseLighting.disableGuiDepthLighting();
                }
                context.itemRenderState.render(context.getMatrices(), context.vertexConsumers, 0xF000F0, OverlayTexture.DEFAULT_UV);
                //mc.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, context.getMatrices(), context.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
                context.draw();
                if (bl) {
                    DiffuseLighting.enableGuiDepthLighting();
                }
            } catch (Throwable var12) {
                CrashReport crashReport = CrashReport.create(var12, "Rendering item");
                CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
                crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
                crashReportSection.add("Item Damage", () -> String.valueOf(stack.getDamage()));
//              crashReportSection.add("Item NBT", () -> return String.valueOf(stack.getNbt()));
                crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
                throw new CrashException(crashReport);
            }

            context.getMatrices().pop();
        }
    }

    public static void drawItemCount(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride, float scale) {
        if (!stack.isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().scale(scale, scale, scale);
            if (stack.getCount() != 1 || countOverride != null) {
                String string = countOverride == null ? String.valueOf(stack.getCount()) : countOverride;
                context.getMatrices().translate(0.0F, 0.0F, 200.0F);
                context.drawText(textRenderer, string, x + 19 - 2 - textRenderer.getWidth(string), y + 6 + 3, 16777215, true);
            }
            context.getMatrices().pop();
        }
    }

    public static void drawItemInSlot(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride, float scale) {
        if (!stack.isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().scale(scale, scale, scale);
            if (stack.getCount() != 1 || countOverride != null) {
                String string = countOverride == null ? String.valueOf(stack.getCount()) : countOverride;
                context.getMatrices().translate(0.0F, 0.0F, 200.0F);
                context.drawText(textRenderer, string, x + 19 - 2 - textRenderer.getWidth(string), y + 6 + 3, 16777215, true);
            }

            int k;
            int l;
            if (stack.isItemBarVisible()) {
                int i = stack.getItemBarStep();
                int j = stack.getItemBarColor();
                k = x + 2;
                l = y + 13;
                context.fill(RenderLayer.getGuiOverlay(), k, l, k + 13, l + 2, -16777216);
                context.fill(RenderLayer.getGuiOverlay(), k, l, k + i, l + 1, j | -16777216);
            }

            ClientPlayerEntity clientPlayerEntity = mc.player;
            float f = clientPlayerEntity == null ? 0.0F : clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack, mc.getRenderTickCounter().getTickDelta(false));
            if (f > 0.0F) {
                k = y + MathHelper.floor(16.0F * (1.0F - f));
                l = k + MathHelper.ceil(16.0F * f);
                context.fill(RenderLayer.getGuiOverlay(), x, k, x + 16, l, Integer.MAX_VALUE);
            }

            context.getMatrices().pop();
        }
    }

    public static ImVec2 calcTextSize(String[] strings) {
        float width = 0;
        float height = 0;
        for (String str : strings) {
            ImVec2 vec1 = ImGui.calcTextSize(str);
            height = Math.max(height, vec1.y);
            width += vec1.x;
        }
        return new ImVec2(width, height);
    }

    public static ImVec2 calcTextSizeWithNewLines(String[] strings) {
        float[] space = new float[]{0};
        float yOffset = strings.length < 1 ? 0 : ImGui.calcTextSize(strings[0]).y;
        int line = 0;
        for (String s : strings) {
            int newLineIndex = s.indexOf("\n");
            String[] c = s.split("\n");
            if (c.length == 1) {
                if (newLineIndex == 0) {
                    line++;
                    float[] replacement = new float[space.length + 1];
                    System.arraycopy(space, 0, replacement, 0, space.length);
                    space = replacement;
                    yOffset += ImGui.calcTextSize(c[0]).y;
                }
                space[line] += ImGui.calcTextSize(c[0]).x;
                if (newLineIndex != 0 && newLineIndex != -1) {
                    line++;
                    float[] replacement = new float[space.length + 1];
                    System.arraycopy(space, 0, replacement, 0, space.length);
                    space = replacement;
                    yOffset += ImGui.calcTextSize(c[0]).y;
                }
            } else {
                for (int i = 0; i < c.length; i++) {
                    if (i != c.length - 1) {
                        float[] replacement = new float[space.length + 1];
                        System.arraycopy(space, 0, replacement, 0, space.length);
                        space = replacement;
                        line++;
                        space[line] = ImGui.calcTextSize(c[i]).x;
                        yOffset += ImGui.calcTextSize(c[i]).y;
                    } else {
                        space[line] += ImGui.calcTextSize(c[i]).x;
                    }
                }

            }
        }
        float width = 0;
        for (float s : space) {
            if (s > width) {
                width = s;
            }
        }
        return new ImVec2(width, yOffset);
    }

    public static void drawTexts(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            ImGui.text(strings[i]);
            if (i != strings.length - 1) {
                ImGui.sameLine(0, 0);
            }
        }
    }
}
