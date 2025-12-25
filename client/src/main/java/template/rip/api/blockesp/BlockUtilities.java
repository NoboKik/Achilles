package template.rip.api.blockesp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockUtilities {

    public static CopyOnWriteArrayList<Block> blockList = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<BlockObject> blocks = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<Chunk> toSearch = new CopyOnWriteArrayList<>();
    public static Color fallBackColor = Color.MAGENTA;
    private static long now;
    public static boolean fullCube = false;
    public static int alphaVal, maxRender;
    public static boolean enabled = false;

    public static void tick(List<Block> searchBlocks, Color fallBackColor, boolean onlyAir) {
        BlockPos origin;
        ClientPlayerEntity cpe = MinecraftClient.getInstance().player;
        if (cpe == null) {
            origin = BlockPos.ORIGIN;
        } else {
            origin = cpe.getBlockPos();
        }
        BlockUtilities.fallBackColor = fallBackColor;
        now = System.currentTimeMillis();
        blockList = new CopyOnWriteArrayList<>(searchBlocks);
//      debug("block list");

        BlockPos pos = new BlockPos(origin.getX(), 0, origin.getZ());
        Comparator<Chunk> sorter = Comparator.comparing(chunk -> pos.getManhattanDistance(new Vec3i(chunk.getPos().getCenterX(), 0, chunk.getPos().getCenterZ())));
        toSearch.sort(sorter);
//      debug("to search sort");
        ArrayList<Chunk> search = new ArrayList<>(toSearch);
        toSearch.clear();
//      debug("to search clear");
        AtomicInteger i = new AtomicInteger();
        search.forEach(chunk -> {
            if (enabled) {
                searchChunk(chunk, onlyAir);
                i.getAndIncrement();
//              debug(i + "/" + search.size() + " chunk");
            }
        });
//      debug("search search");

        blocks.forEach(bo -> {
            if (enabled) {
                bo.refresh();
            }
        });
//      debug("block refresh");
        blocks.removeIf(bo -> {
            if (!blockList.contains(bo.block)/* || bo.state.getOutlineShape(MinecraftClient.getInstance().world, bo.pos).isEmpty()*/) {
//              System.out.println(bo.block + " " + bo.pos + " " + bo.x + " " + bo.y + " " + bo.z);
                return true;
            }
            return false;
        });
//      debug("block remove");
        Vec3d player = cpe == null ? BlockPos.ORIGIN.toCenterPos() : cpe.getCameraPosVec(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        Comparator<BlockObject> comp = Comparator.comparing(bo -> player.distanceTo(bo.pos.toCenterPos()));
        blocks.sort(comp);
//      debug("block sort");
    }

    private static void debug(String part) {
        if (Boolean.FALSE) {
            System.out.println(part + " " + (System.currentTimeMillis() - now));
        }
    }

    public static void searchChunk(Chunk chunk, boolean onlyAir) {
        ChunkPos cPos = chunk.getPos();
        World world = MinecraftClient.getInstance().world;

        if (world == null) {
            return;
        }

        Iterable<BlockPos> list = BlockPos.iterate(cPos.getStartX(), world.getBottomY(), cPos.getStartZ(), cPos.getEndX(), chunk.getHeight(),  cPos.getEndZ());
        ArrayList<BlockObject> toAdd = new ArrayList<>();
        for (BlockPos bpos : list) {
            BlockObject bo = new BlockObject(bpos);
            if (blockList.contains(bo.block) && add(bo.pos, onlyAir)) {
                toAdd.add(bo);
//              System.out.println(bpos.getY() + " " + bo.pos + " " + bo.y);
            }
        }
//      debug(i +" blockpos");
        blocks.addAllAbsent(toAdd);

        ClientPlayerEntity cpe = MinecraftClient.getInstance().player;

        BlockPos player = cpe == null ? chunk.getPos().getCenterAtY(64) : cpe.getBlockPos();
        Comparator<BlockObject> comp = Comparator.comparing(bo -> player.getManhattanDistance(bo.pos));
        blocks.sort(comp);
//      debug("block sort mini");
    }

    public static void render(WorldRenderContext context) {
        if (!BlockUtilities.enabled) {
            return;
        }
        List<BlockObject> copy = new ArrayList<>(BlockUtilities.blocks);

        if (copy.isEmpty()) {
            return;
        }

        Camera cam = context.camera();

        MatrixStack matstack = new MatrixStack();

        matstack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
        matstack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));

        Vec3d targetpos = Vec3d.ZERO.subtract(cam.getPos());
        matstack.translate(targetpos.x, targetpos.y, targetpos.z);

        Matrix4f posMat = matstack.peek().getPositionMatrix();
        Tessellator tessy = Tessellator.getInstance();
        BufferBuilder buffy = tessy.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Iterator<BlockObject> iterator = copy.iterator();

        int amount = 0;
        int alpha = BlockUtilities.alphaVal;

        while (iterator.hasNext()) {
            if (amount >= BlockUtilities.maxRender) {
                break;
            }

            BlockObject esp = iterator.next();
            BlockPos pos = esp.pos;

            VoxelShape shape = esp.state.getOutlineShape(context.world(), pos);

            if (shape.isEmpty()) {
                continue;
            }

            Box box = shape.offset(esp.x, esp.y, esp.z).getBoundingBox();

            float x1 = (float) box.minX;
            float y1 = (float) box.minY;
            float z1 = (float) box.minZ;
            float x2 = (float) box.maxX;
            float y2 = (float) box.maxY;
            float z2 = (float) box.maxZ;

            //up
            buffy.vertex(posMat, x1, y2, z1).color(esp.red, esp.green, esp.blue, alpha);
            buffy.vertex(posMat, x1, y2, z2).color(esp.red, esp.green, esp.blue, alpha);
            buffy.vertex(posMat, x2, y2, z2).color(esp.red, esp.green, esp.blue, alpha);
            buffy.vertex(posMat, x2, y2, z1).color(esp.red, esp.green, esp.blue, alpha);

            if (BlockUtilities.fullCube) {

                //north
                buffy.vertex(posMat, x1, y1, z1).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x1, y2, z1).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y2, z1).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y1, z1).color(esp.red, esp.green, esp.blue, alpha);

                //west
                buffy.vertex(posMat, x1, y1, z1).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x1, y2, z1).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x1, y2, z2).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x1, y1, z2).color(esp.red, esp.green, esp.blue, alpha);

                //down
                buffy.vertex(posMat, x1, y1, z1).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x1, y1, z2).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y1, z2).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y1, z1).color(esp.red, esp.green, esp.blue, alpha);

                //east
                buffy.vertex(posMat, x2, y1, z1).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y2, z1).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y2, z2).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y1, z2).color(esp.red, esp.green, esp.blue, alpha);

                //south
                buffy.vertex(posMat, x1, y1, z2).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x1, y2, z2).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y2, z2).color(esp.red, esp.green, esp.blue, alpha);
                buffy.vertex(posMat, x2, y1, z2).color(esp.red, esp.green, esp.blue, alpha);

            }
            amount++;
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

    public static List<Block> coal() {
        return List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.COAL_BLOCK);
    }

    public static List<Block> iron() {
        return List.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.IRON_BLOCK, Blocks.RAW_IRON_BLOCK);
    }

    public static List<Block> gold() {
        return List.of(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.GOLD_BLOCK, Blocks.RAW_GOLD_BLOCK, Blocks.NETHER_GOLD_ORE);
    }

    public static List<Block> lapis() {
        return List.of(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.LAPIS_BLOCK);
    }

    public static List<Block> redstone() {
        return List.of(Blocks.REDSTONE_ORE, Blocks.REDSTONE_BLOCK, Blocks.DEEPSLATE_REDSTONE_ORE);
    }

    public static List<Block> emerald() {
        return List.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.EMERALD_BLOCK);
    }

    public static List<Block> diamond() {
        return List.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_BLOCK);
    }

    public static List<Block> netherite() {
        return List.of(Blocks.NETHERITE_BLOCK, Blocks.ANCIENT_DEBRIS);
    }

    public static List<Block> head() {
        return List.of(Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD);
    }

    public static List<Block> shulker() {
        return List.of(Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX);
    }

    public static boolean isAir(BlockPos bpos) {
        World world = MinecraftClient.getInstance().world;
        if (world == null)
            return false;

        boolean air = false;
        for (Direction dir : Direction.values()) {
            BlockState state = world.getBlockState(bpos.offset(dir));
            if (state.isAir() || state.isLiquid() || !state.isOpaque())
                air = true;
        }

        return air;
    }

    public static boolean add(BlockPos pos, boolean onlyAir) {
        return !onlyAir || isAir(pos);
    }
}
