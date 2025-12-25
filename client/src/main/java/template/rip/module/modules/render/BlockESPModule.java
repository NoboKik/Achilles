package template.rip.module.modules.render;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import template.rip.api.blockesp.BlockUtilities;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class BlockESPModule extends Module {

    public final BooleanSetting onlyAir = new BooleanSetting(this, false, "Only if exposed");
    public final RegistrySetting<Block> espBlockSelection = new RegistrySetting<>(Arrays.asList(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.COAL_BLOCK, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK, Blocks.IRON_BLOCK, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.RAW_GOLD_BLOCK, Blocks.GOLD_BLOCK, Blocks.NETHER_GOLD_ORE, Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.REDSTONE_BLOCK, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.EMERALD_BLOCK, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_BLOCK, Blocks.ANCIENT_DEBRIS, Blocks.NETHERITE_BLOCK), this, Registries.BLOCK, "ESP Blocks");

    public enum RenderMode{Cube, Square}
    public final ModeSetting<RenderMode> renderMode = new ModeSetting<>(this, Description.of("Cube: Regular old BlockESP with all block faces drawn\nSquare: A more performant method that renders less while displaying the same info"), RenderMode.Cube, "Render Mode");
    public final NumberSetting alphaVal = new NumberSetting(this, 40d, 0d, 255d, 1d, "Alpha");
    public final ColorSetting fallBackColor = new ColorSetting(this, new JColor(255, 0 ,255), false, "Fallback Color");
    public final AnyNumberSetting maxRender = new AnyNumberSetting(this, 5000d, false, "Max Block Renders");
    public final BooleanSetting donutSmpBypass = new BooleanSetting(this, false, "Donut SMP bypass");

    public BlockESPModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        BlockUtilities.enabled = true;
        BlockUtilities.blocks = new CopyOnWriteArrayList<>();
        BlockUtilities.toSearch = new CopyOnWriteArrayList<>();
        if (mc.world == null || mc.player == null)
            return;

        mc.worldRenderer.reload();
        AtomicReferenceArray<WorldChunk> chunks = mc.world.getChunkManager().chunks.chunks;
        int length = chunks.length();
        for (int index = 0; index < length; index++) {
            WorldChunk wc = chunks.get(index);
            if (wc != null) {
                BlockUtilities.toSearch.add(wc);
            }
        }
    }

    @Override
    public void onDisable() {
        BlockUtilities.blocks = new CopyOnWriteArrayList<>();
        BlockUtilities.toSearch = new CopyOnWriteArrayList<>();
        BlockUtilities.enabled = false;
    }

    @Override
    protected void enable() {
        BlockUtilities.enabled = true;
        super.enable();
    }

    @Override
    protected void disable() {
        BlockUtilities.enabled = false;
        super.disable();
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (mc.world == null)
            return;

        if (event.packet instanceof BlockUpdateS2CPacket) {
            BlockUtilities.toSearch.add(mc.world.getChunk(((BlockUpdateS2CPacket) event.packet).getPos()));
        } else if (event.packet instanceof ExplosionS2CPacket) {
            HashMap<Chunk, Boolean> toScan = new HashMap<>();
            /*for (BlockPos blockPos : ((ExplosionS2CPacket) event.packet).getAffectedBlocks()) {
                toScan.put(mc.world.getChunk(blockPos), true);
            }*/
            BlockUtilities.toSearch.addAll(toScan.keySet());
        }
    }

    @EventHandler
    private void onChunk(ChunkDataEvent event) {
        BlockUtilities.toSearch.add(event.chunk);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        BlockUtilities.alphaVal = alphaVal.getIValue();
        BlockUtilities.maxRender = maxRender.getIValue();
        BlockUtilities.fullCube = renderMode.is(RenderMode.Cube);
    }

    @EventHandler
    private void onBlockESPTick(BlockESPTicker event) {
        try {
            BlockUtilities.tick(espBlockSelection.selected, fallBackColor.getColor(), onlyAir.isEnabled());
        } catch (Exception ignored) {}
    }

    @EventHandler
    private void onWorldRender(WorldRenderEvent event) {
        BlockUtilities.render(event.context);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (donutSmpBypass.isEnabled() && event.packet instanceof ChunkDeltaUpdateS2CPacket) {
            event.cancel();
        }
    }
}
