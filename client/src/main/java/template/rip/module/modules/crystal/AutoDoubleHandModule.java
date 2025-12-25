package template.rip.module.modules.crystal;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.SetScreenEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.DamageUtils;
import template.rip.api.util.InvUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.List;

public class AutoDoubleHandModule extends Module {

    private final BooleanSetting checkPlayersLook = new BooleanSetting(this, true, "Check Players Look");
    private final BooleanSetting predictCrystals = new BooleanSetting(this, true, "Predict Crystals");
    private final BooleanSetting predictSword = new BooleanSetting(this, true, "Predict Sword");
    private final NumberSetting predictMultiply = new NumberSetting(this, 1, 0, 3, 0.1, "Damage multiplier").setAdvanced();
    private final BooleanSetting doubleHandAfterPop = new BooleanSetting(this, true, "DHand After Pop");
    private final BooleanSetting switchOnOpenInv = new BooleanSetting(this, false, "Totem on Inventory");
    private final BooleanSetting notWhileShielding = new BooleanSetting(this, false, "Shield Check");
    public final NumberSetting slotToSwitch = new NumberSetting(this, 5, 1, 9, 1, "Totem Slot for Inventory");
    private final MinMaxNumberSetting delay = new MinMaxNumberSetting(this, 100, 200, 0, 500, 1, "Delay");
    private final MinMaxNumberSetting cooldown = new MinMaxNumberSetting(this, 200, 400, 0, 750, 1, "Cooldown");

    private boolean needToDHand;
    private long cooldownClock, delayClock;

    public AutoDoubleHandModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        needToDHand = false;
        cooldownClock = System.currentTimeMillis() + cooldown.getRandomInt();
        delayClock = System.currentTimeMillis() + delay.getRandomInt();
    }

    private boolean willDie(double damage) {
        if (mc.player.isUsingItem() && (mc.player.getActiveItem().isOf(Items.ENCHANTED_GOLDEN_APPLE) || mc.player.getActiveItem().isOf(Items.GOLDEN_APPLE))) {
            return false;
        }
        return mc.player.getHealth() - (damage * predictMultiply.getFValue()) <= 0;
    }

    private boolean willDie(PlayerEntity player, double damage) {
        HitResult bhr = player.raycast(player.getPos().distanceTo(mc.player.getPos()), 0f, false);
        if (bhr.getPos().distanceTo(mc.player.getPos().add(0, 1, 0)) < 1.5) {
            return mc.player.getHealth() - (damage * predictMultiply.getFValue()) <= 0;
        }
        return false;
    }

    private boolean arePlayersAimingAtCrystal(EndCrystalEntity crystal) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            Vec3d start = player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
            Vec3d end = start.add(RotationUtils.getPlayerLookVec(player));
            Box box = new Box(start, end);
            List<EndCrystalEntity> crystalsInBox = mc.world.getEntitiesByClass(EndCrystalEntity.class, box, endCrystal -> crystal == endCrystal);

            if (crystalsInBox != null || crystalsInBox.isEmpty())
                return true;
        }
        return false;
    }

    private boolean isPlayerAimingAtMe(PlayerEntity player) {
        Vec3d start = player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
        Vec3d end = start.add(RotationUtils.getPlayerLookVec(player));
        Box box = new Box(start, end);
        List<PlayerEntity> playersInBox = mc.world.getEntitiesByClass(PlayerEntity.class, box, (player1) -> player1 == mc.player);

        return playersInBox != null && !playersInBox.isEmpty();
    }

    private boolean arePlayersAimingAtBlock(BlockPos blockPos) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            Vec3d start = player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false));
            Vec3d end = start.add(RotationUtils.getPlayerLookVec(player));
            BlockHitResult blockHitResult = mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

            if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK && blockHitResult.getBlockPos().equals(blockPos))
                return true;
        }
        return false;
    }

    @EventHandler
    private void onScreen(SetScreenEvent event) {
        if (event.screen instanceof InventoryScreen && mc.currentScreen == null && switchOnOpenInv.isEnabled()) {
            InvUtils.setInvSlot(slotToSwitch.getIValue() - 1);
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!doubleHandAfterPop.isEnabled()) return;

        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 35 && packet.getEntity(mc.world) instanceof ClientPlayerEntity) needToDHand = true;
        }
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (mc.currentScreen != null) return;

        if (notWhileShielding.isEnabled() && mc.player.getActiveItem().isOf(Items.SHIELD) || cooldownClock > System.currentTimeMillis()) {
            return;
        }

        boolean hand = needToDHand;

        if (!needToDHand && predictCrystals.isEnabled()) {
            List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(EndCrystalEntity.class, mc.player.getBoundingBox().expand(10), (endCrystal) -> true);

            for (EndCrystalEntity crystal : crystals) {
                if (checkPlayersLook.isEnabled()) {
                    if (!arePlayersAimingAtCrystal(crystal)) continue;
                }

                double damage = DamageUtils.crystalDamage(mc.player, crystal.getPos(), true, crystal.getBlockPos().down(), false);
                if (willDie(damage)) {
                    needToDHand = true;
                    break;
                }
            }
        }

        if (!needToDHand && predictSword.isEnabled()) {
            Entity e = PlayerUtils.findFirstTarget();
            if (e instanceof PlayerEntity player) {
                double damage = DamageUtils.getSwordDamage(player, mc.player);
                if (willDie(player, damage)) {
                    needToDHand = true;
                }
            }
        }

        if (needToDHand) {
            if (!hand) {
                delayClock = System.currentTimeMillis() + delay.getRandomInt();
            }
            if (!mc.player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                if (delayClock > System.currentTimeMillis()) {
                    return;
                }

                InvUtils.selectItemFromHotbar(Items.TOTEM_OF_UNDYING);
            }

            needToDHand = false;
            cooldownClock = System.currentTimeMillis() + cooldown.getRandomInt();
        }
    }
}
