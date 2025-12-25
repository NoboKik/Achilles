package template.rip.module.modules.blatant;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import template.rip.Template;
import template.rip.api.event.events.*;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

import static template.rip.module.modules.blatant.FireballJumpModule.ticksSinceExplosion;

public class VelocityModule extends Module {

    public enum modeEnum{None, Hypixel, Old_Grim, Reverse, Reduce_Hit};
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, Description.of("None: You will flag on any anticheat\nHypixel: Bypass mode for hypixel!\nOld Grim: 0 0 velocity for old versions of GrimAC\nReverse: Reverses the horizontal velocity sent by the server, works on Vulcan!\nReduce Hit: Abuses sprint hit slowdown"), modeEnum.None, "Bypass mode");
    public final NumberSetting velocityH = new NumberSetting(this, 1, 0, 1, 0.01, "Horizontal");
    public final NumberSetting velocityV = new NumberSetting(this, 1, 0, 1, 0.01, "Vertical");
    public final MinMaxNumberSetting reduceHits = new MinMaxNumberSetting(this, Description.of("Reduces your velocity by 40% by abusing Sprint hit slowdown\nHigh values might flag auto clicker checks!"), 1, 1, 0, 20, 1, "Reduce Hit Count");

    private boolean sendpkt = false;
    private int onGroundTicks = 0;
    private int offGroundTicks = 0;
    private int timeout = 0;
    private boolean hadVelo = false;
    private int tick = 0;

    public VelocityModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public String getSuffix() {
        return mode.is(modeEnum.None) ? " " + (int) (velocityH.getValue() * 100) + " " + (int) (velocityV.getValue() * 100) : " " + mode.getDisplayName();
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        sendpkt = false;
        hadVelo = false;
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && mode.is(modeEnum.Old_Grim)) {
            timeout = 100;
        } else {
            timeout--;
        }
    }

    @EventHandler
    private void handleInputEvents(HandleInputEvent.Pre event) {
        if (!hadVelo)
            return;

        if (!nullCheck()) {
            return;
        }
        MultiTaskModule mtm = Template.moduleManager.getModule(MultiTaskModule.class);
        boolean nonNullEnabled = mtm != null && mtm.isEnabled();
        if (mc.player.isUsingItem() && !(nonNullEnabled && mtm.attack.isEnabled())) {
            return;
        }
        if (mc.currentScreen instanceof HandledScreen) {
            return;
        }

        if (mode.is(modeEnum.Reduce_Hit) && mc.crosshairTarget instanceof EntityHitResult ehr && mc.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
            // we can only reduce if we are sprinting/have knock-back (it's how it's done in vanilla)
            if (mc.player.isSprinting() || mc.player.getMainHandStack().getEnchantments().getEnchantments().stream().anyMatch(r -> r.matchesKey(Enchantments.KNOCKBACK))) {
                if (Template.isClickSim()) Template.mouseSimulation().mouseClick(mc.options.attackKey.boundKey.getCode());
                if (Template.EVENTBUS.post(new AttackEntityEvent.Pre(ehr.getEntity())).isCancelled())
                    return;
                int count = reduceHits.getRandomInt();
                for (int i = 0; i < count; i++) {
                    // mc.interactionManager.attackEntity(player, entity)
                    mc.interactionManager.syncSelectedSlot();
                    mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(ehr.getEntity(), mc.player.isSneaking()));

                    // mc.player.attack(entity)
                    mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
                    // we go full legit if they're only doing one reduce hit
                    if (count == 1) {
                        mc.player.setSprinting(false);
                    }

                    // self-explanatory
                    mc.player.resetLastAttackedTicks();

                    // we need to do this
                    mc.player.swingHand(Hand.MAIN_HAND);
                    hadVelo = false;
                    Template.EVENTBUS.post(new AttackEntityEvent.Post(ehr.getEntity()));
                }
            }
        }
    }
    
    @EventHandler
    private void onVelocity(VelocityEvent.Pre event) {
        if (ticksSinceExplosion < 5 && Template.moduleManager.isModuleEnabled(FireballJumpModule.class)) return;
        if (mc.player != null && event.entity == mc.player) {
            switch (mode.getMode()) {
                case Old_Grim: {
                    if (timeout < 0) {
                        event.cancel();
                        sendpkt = true;
                    }
                    break;
                }
                case None: {
                    event.cancel();
                    double velX = (event.x - mc.player.getVelocity().x) * velocityH.getValue();
                    double velY = (event.y - mc.player.getVelocity().y) * velocityV.getValue();
                    double velZ = (event.z - mc.player.getVelocity().z) * velocityH.getValue();
                    mc.player.setVelocityClient(velX + mc.player.getVelocity().x, velY + mc.player.getVelocity().y, velZ + mc.player.getVelocity().z);
                    break;
                }
                case Reverse: {
                    event.cancel();
                    double velX = -(event.x - mc.player.getVelocity().x) * velocityH.getValue();
                    double velY = (event.y - mc.player.getVelocity().y) * velocityV.getValue();
                    double velZ = -(event.z - mc.player.getVelocity().z) * velocityH.getValue();
                    mc.player.setVelocityClient(velX + mc.player.getVelocity().x, velY + mc.player.getVelocity().y, velZ + mc.player.getVelocity().z);
                    break;
                }
                case Reduce_Hit: {
                    hadVelo = true;
                    tick = 0;
                    break;
                }
                case Hypixel: {
                    double v;
                    if (!mc.player.isOnGround() && offGroundTicks >= 5) {
                        v = 0;
                    } else {
                        v = 1.0;
                    }

                    event.cancel();
                    double velX = (event.x - mc.player.getVelocity().x) * velocityH.getValue();
                    double velY = (event.y - mc.player.getVelocity().y) * v;
                    double velZ = (event.z - mc.player.getVelocity().z) * velocityH.getValue();
                    mc.player.setVelocityClient(velX + mc.player.getVelocity().x, velY + mc.player.getVelocity().y, velZ + mc.player.getVelocity().z);
                    break;
                }
            }
        }
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Post event) {
        if (tick >= 3)
            hadVelo = false;
        tick++;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!nullCheck())
            return;

        if (sendpkt) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), PlayerUtils.randomPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mc.player.getBlockPos().up(), Direction.DOWN));
            sendpkt = false;
        }
        if (mc.player.isOnGround()) {
            offGroundTicks = 0;
            onGroundTicks = onGroundTicks + 1;
        } else {
            onGroundTicks = 0;
            offGroundTicks = offGroundTicks + 1;
        }
    }
}