package template.rip.module.modules.render;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import template.rip.api.blockesp.WorldRenderContext;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.BlockUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.RenderUtils;
import template.rip.mixin.accessors.IClientChunkManager;
import template.rip.mixin.accessors.IClientChunkMap;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseESP extends Module {

    public final NumberSetting maxChunksSetting = new NumberSetting(this, 8, 1, 20, 1, "Chunks P/S");

    public final BooleanSetting kelpSetting = new BooleanSetting(this, true, "Kelp");
    public final NumberSetting kelpHeightSetting = new NumberSetting(this, 3, 0, 10, 1, "Kelp Height");
    public final BooleanSetting vineSetting = new BooleanSetting(this, true, "Vines");

    private final Long2ObjectMap<CheckedChunk> chunks = new Long2ObjectOpenHashMap<>();
    private final Queue<Chunk> chunkQueue = new LinkedList<>();

    // Too lazy to decrease sets for now
    private final Set<RenderBlock> kelpPositions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<RenderBlock> vinePositions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<BlockPos> duplicates = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final ExecutorService powerHack = Executors.newSingleThreadExecutor();

    public BaseESP(Category category, Description description, String name) {
        super(category, description, name);
        kelpHeightSetting.addConditionBoolean(kelpSetting, true);
    }

    @Override
    public void onDisable() {
        chunks.clear();
        chunkQueue.clear();
        kelpPositions.clear();
        vinePositions.clear();
        duplicates.clear();
    }

    @EventHandler
    private void onWorldRender(WorldRenderEvent event) {
        WorldRenderContext context = event.context;
        List<BlockPos> removed = new ArrayList<>();
        int rangeMax = 260;

        this.renderBlocks(context, kelpPositions, removed, rangeMax);
        this.renderBlocks(context, vinePositions, removed, rangeMax);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.nullCheck()) {
            return;
        }

        synchronized (chunks) {
            for (CheckedChunk tChunk : chunks.values()) {
                tChunk.checked = false;
            }

            for (Chunk chunk : this.getChunks(true)) {
                ChunkPos chunkPos = chunk.getPos();
                long key = ChunkPos.toLong(chunkPos.x, chunkPos.z);

                if (chunks.containsKey(key)) {
                    chunks.get(key).checked = true;
                } else if (!chunkQueue.contains(chunk)) {
                    chunkQueue.add(chunk);
                }
            }

            this.processChunkQueue();
            chunks.values().removeIf(tChunk -> !tChunk.checked);
        }
    }

    private void renderBlocks(WorldRenderContext context, Set<RenderBlock> set, List<BlockPos> removed, int maxDistance) {
        if (!set.isEmpty()) {
            for (RenderBlock renderBlock : set) {
                BlockPos pos = renderBlock.pos();

                if (Math.sqrt(mc.player.squaredDistanceTo(Vec3d.ofBottomCenter(pos))) >= maxDistance) {
                    removed.add(pos);
                    continue;
                }

                Color blockColor = renderBlock.blockColor();
                RenderUtils.Render3D.renderBox(new Box(MathUtils.vec3iToVec3d(pos), MathUtils.vec3iToVec3d(pos.add(1, 1, 1))), blockColor, 50, context);
            }
        }
    }

    private void processChunkQueue() {
        int maxChunksPerTick = maxChunksSetting.getIValue();
        int processed = 0;

        while (!chunkQueue.isEmpty() && processed < maxChunksPerTick) {
            Chunk chunk = chunkQueue.poll();

            if (chunk != null) {
                ChunkPos chunkPos = chunk.getPos();
                CheckedChunk tChunk = new CheckedChunk(chunkPos.x, chunkPos.z);
                chunks.put(tChunk.key(), tChunk);
                powerHack.submit(() -> this.searchChunk(chunk, chunkPos));
                processed++;
            }
        }
    }

    private void searchChunk(Chunk chunk, ChunkPos chunkPos) {
        if (mc.world == null) {
            return;
        }

        var sections = chunk.getSectionArray();
        int Y = mc.world.getBottomY();
        boolean foundExpensiveBlocks = false;
        boolean kelpEnabled = kelpSetting.isEnabled();
        int kelpHeightValue = kelpHeightSetting.getIValue();
        boolean vinesEnabled = vineSetting.isEnabled();

        for (ChunkSection section : sections) {
            if (section == null) {
                continue;
            }

            for (int z = 0; z <= 16; z++) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        if (mc.world == null) {
                            return;
                        }

                        int currentY = Y + y;
                        BlockPos pos = chunkPos.getBlockPos(x, currentY, z);
                        Block block = BlockUtils.getBlock(pos);

                        if (duplicates.contains(pos)) {
                            continue;
                        }

                        if (currentY >= 40 && currentY < 100) {
                            Block blockDownDown = BlockUtils.getBlock(pos.down(kelpHeightValue));

                            if (kelpEnabled && block == Blocks.KELP && (kelpHeightValue == 0 || blockDownDown == Blocks.KELP_PLANT) && BlockUtils.isAir(pos.up())) {
                                kelpPositions.add(new RenderBlock(pos, block, new Color(0, 200, 0)));
                                duplicates.add(pos);
                            }
                        }

                        if (vinesEnabled && currentY >= 55 && currentY < 160 && block instanceof VineBlock) {
                            int up = this.countVinesUp(pos), down = this.countVinesDown(pos);

                            // Vines are tall enough
                            if (up + down + 1 >= 20) {
                                // Let's check if there are logs around the vines to detect if they are from trees or not
                                boolean isOnTree = false;
                                for (Direction direction : Direction.values()) {
                                    // Skip the DOWN direction as vines don't attach downward
                                    if (direction == Direction.DOWN) {
                                        continue;
                                    }

                                    BlockPos adjacentPos = pos.offset(direction);
                                    BlockState adjacentState = BlockUtils.getBlockState(adjacentPos);
                                    Set<Block> TREE_BLOCKS = Set.of(Blocks.OAK_LOG, Blocks.SPRUCE_LOG,
                                            Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG,
                                            Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG, Blocks.OAK_LEAVES,
                                            Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
                                            Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES);

                                    // Check if adjacent block is a tree block and vine can connect to it
                                    if (TREE_BLOCKS.contains(adjacentState.getBlock()) && VineBlock.shouldConnectTo(mc.world, adjacentPos, direction)) {
                                        isOnTree = true;
                                        break;
                                    }
                                }

                                if (!isOnTree) {
                                    vinePositions.add(new RenderBlock(pos, block, new Color(160)));
                                    duplicates.add(pos);
                                }
                            }
                        }
                    }
                }
            }

            // 16 * 16 = 256 aka max world build height easy math bruh
            Y += 16;
        }
    }

    // Counting possible tall vines up
    private int countVinesUp(BlockPos pos) {
        int i = 0;
        while (i < 16 && BlockUtils.getBlockState(pos.up(i + 1)).isOf(Blocks.VINE)) {
            ++i;
        }
        return i;
    }

    // Counting possible tall vines down
    private int countVinesDown(BlockPos pos) {
        int i = 0;
        while (i < 16 && BlockUtils.getBlockState(pos.down(i + 1)).isOf(Blocks.VINE)) {
            ++i;
        }
        return i;
    }

    private static class CheckedChunk {

        // Slight optimization instead of 2 variables :skull:
        private final int[] coords = new int[2];
        public boolean checked;

        public CheckedChunk(int x, int z) {
            this.coords[0] = x;
            this.coords[1] = z;
            this.checked = true;
        }

        int x() {
            return this.coords[0];
        }

        int z() {
            return this.coords[1];
        }

        public long key() {
            return ChunkPos.toLong(x(), z());
        }
    }

    private Iterable<Chunk> getChunks(boolean neighborsOnly) {
        return () -> new ChunkChecker(neighborsOnly);
    }

    private static class ChunkChecker implements Iterator<Chunk> {

        private final IClientChunkMap map = (IClientChunkMap) (Object) ((IClientChunkManager) mc.world.getChunkManager()).getChunks();
        private final boolean onlyWithLoadedNeighbours;

        private int i = 0;
        private Chunk chunk;

        public ChunkChecker(boolean neighborsOnly) {
            this.onlyWithLoadedNeighbours = neighborsOnly;
            this.searchNextChunk();
        }

        private Chunk searchNextChunk() {
            Chunk lastChunk = chunk;

            chunk = null;

            while (i < map.getChunks().length()) {
                chunk = map.getChunks().get(i++);

                if (chunk != null && (!onlyWithLoadedNeighbours || this.isInRadius(chunk))) {
                    break;
                }
            }

            return lastChunk;
        }

        private boolean isInRadius(Chunk chunk) {
            int x = chunk.getPos().x;
            int z = chunk.getPos().z;
            ChunkManager man = mc.world.getChunkManager();

            return man.isChunkLoaded(x + 1, z) && man.isChunkLoaded(x - 1, z) && man.isChunkLoaded(x, z + 1) && man.isChunkLoaded(x, z - 1);
        }

        @Override
        public boolean hasNext() {
            return chunk != null;
        }

        @Override
        public Chunk next() {
            return this.searchNextChunk();
        }
    }

    private record RenderBlock(BlockPos pos, Block block, Color blockColor) {}
}
