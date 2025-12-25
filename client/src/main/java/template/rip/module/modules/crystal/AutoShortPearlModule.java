package template.rip.module.modules.crystal;

import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.Pair;
import template.rip.Template;
import template.rip.api.event.events.DamageEvent;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.events.MouseUpdateEvent;
import template.rip.api.event.events.PacketEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.InvUtils;
import template.rip.api.util.MathUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class AutoShortPearlModule extends Module {

    public enum modeEnum{HurtTime, Velocity}
    public final ModeSetting<modeEnum> detectMode = new ModeSetting<>(this, modeEnum.HurtTime, "Detection Mode").setAdvanced();
    public final NumberSetting targetPitch = new NumberSetting(this, 80, 60, 90, 0.1, "Throw Pitch");
    public final NumberSetting throwChance = new NumberSetting(this, 66, 0, 100, 1, "Throw Chance");
    public final NumberSetting randomRotAmount = new NumberSetting(this, Description.of("How much to shake your camera by"), 0.5, 0.0, 5d, 0.1, "Camera shake amount").setAdvanced();
    public final BooleanSetting switchBack = new BooleanSetting(this, true, "Switch Back");

    private boolean gotHurt;
    private int lastSlot;

    public AutoShortPearlModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        gotHurt = false;
        lastSlot = -1;
    }

    @EventHandler
    private void onDamage(DamageEvent event) {
        if (!nullCheck()) {
            return;
        }

        boolean bl = false;
        if (detectMode.is(modeEnum.HurtTime)) {
            bl = event.damaged == mc.player;
        }
        if (bl && MathUtils.getRandomInt(0, 100) < throwChance.getFValue() && !mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack()) && InvUtils.hasItemInHotbar(Items.ENDER_PEARL)) {
            gotHurt = true;
            lastSlot = mc.player.getInventory().selectedSlot;
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (!nullCheck()) {
            return;
        }

        boolean bl = false;
        if (detectMode.is(modeEnum.Velocity)) {
            bl = event.packet instanceof EntityVelocityUpdateS2CPacket && ((EntityVelocityUpdateS2CPacket) event.packet).getEntityId() == mc.player.getId();
        }
        if (bl && MathUtils.getRandomInt(0, 100) < throwChance.getFValue() && !mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL.getDefaultStack()) && InvUtils.hasItemInHotbar(Items.ENDER_PEARL)) {
            gotHurt = true;
            lastSlot = mc.player.getInventory().selectedSlot;
        }
    }

    @EventHandler
    private void onMouse(MouseUpdateEvent.Pre event) {
        if (!gotHurt)
            return;
        Rotation finalRotation = RotationUtils.correctSensitivity(new Rotation(Template.rotationManager().yaw() + MathUtils.getRandomDouble(-randomRotAmount.getFValue(), randomRotAmount.getFValue()), targetPitch.value));
//        switch (rotMode.getMode()) {
//            case Normal -> {
                Pair<Double, Double> pr = RotationUtils.approximateRawCursorDeltas(RotationUtils.closestDelta(finalRotation, RotationUtils.entityRotation(mc.player)));
                mc.player.changeLookDirection(pr.getLeft(), pr.getRight());
//            }
//            case Silent -> Template.rotationManager().setRotation(finalRotation);
//        }
    }

    @EventHandler
    private void onInputs(HandleInputEvent.Pre event) {
        if (Math.abs(Template.rotationManager().pitch() - targetPitch.getFValue()) < 5 && gotHurt) {
            InvUtils.setInvSlot(InvUtils.getItemSlot(Items.ENDER_PEARL));
            mc.doItemUse();
            if (switchBack.isEnabled() && lastSlot != -1) {
                InvUtils.setInvSlot(lastSlot);
            }
            onEnable();
        }
    }
}
