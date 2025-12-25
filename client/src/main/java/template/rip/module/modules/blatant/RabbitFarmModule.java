package template.rip.module.modules.blatant;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import template.rip.Template;
import template.rip.api.event.events.InputEvent;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.events.WorldRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.rotation.Rotation;
import template.rip.api.rotation.RotationUtils;
import template.rip.api.util.MathUtils;
import template.rip.api.util.MouseSimulation;
import template.rip.api.util.PlayerUtils;
import template.rip.api.util.RenderUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.util.ArrayList;
import java.util.Comparator;

public class RabbitFarmModule extends Module {

    public final NumberSetting reach = new NumberSetting(this, 3, 0, 6, 0.1, "Reach");
    public final ColorSetting color = new ColorSetting(this, new JColor(255, 175, 175, 50), true, "Current Rabbit Render Color");
    public enum modeEnum{Normal, Silent}
    public final ModeSetting<modeEnum> rotMode = new ModeSetting<>(this, modeEnum.Silent, "Rotation Mode");
    public enum interactModeEnum{Attack, Interact, Interact_Sneak}
    public final ModeSetting<interactModeEnum> interactMode = new ModeSetting<>(this, interactModeEnum.Attack, "Action Mode");
    public final MinMaxNumberSetting aimSpeed = new MinMaxNumberSetting(this, 0.1, 0.2, 0, 1, 0.01, "Aim Speed");
    public final MinMaxNumberSetting delayPer = new MinMaxNumberSetting(this, 500, 750, 0, 2500, 1, "Delay Per Interact");
    public final BooleanSetting onlyOnGround = new BooleanSetting(this, true, "Only OnGround Rabbits");

    private long timer;

    public RabbitFarmModule() {
        super(Category.BLATANT, Description.of("Automatically walks to Rabbits and interacts with them"), "RabbitFarm");
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @Override
    public void onEnable() {
        timer = System.currentTimeMillis() + delayPer.getRandomInt();
    }

    @EventHandler
    private void onTick(PlayerTickEvent.Pre event) {
        if (getCurrentRabbit() == null || !nullCheck())
            return;

        HitResult hr = hr();
        if (hr instanceof EntityHitResult result && timer < System.currentTimeMillis()) {
            switch (interactMode.getMode()) {
                case Attack : {
                    mc.interactionManager.attackEntity(mc.player, result.getEntity());
                    if (Template.isClickSim())
                        MouseSimulation.mouseClick(mc.options.attackKey.boundKey.getCode());
                    break;
                }
                case Interact :
                case Interact_Sneak : {
                    mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.interact(result.getEntity(), interactMode.is(interactModeEnum.Interact_Sneak), Hand.MAIN_HAND));
                    if (Template.isClickSim())
                        MouseSimulation.mouseClick(mc.options.useKey.boundKey.getCode());
                    break;
                }
            }
            mc.player.swingHand(Hand.MAIN_HAND);
            onEnable();
        }
    }

    @EventHandler
    private void onRender(WorldRenderEvent event) {
        Entity rabbit = getCurrentRabbit();
        if (rabbit == null || !nullCheck())
            return;

        RenderUtils.Render3D.renderBox(PlayerUtils.renderBox(getCurrentRabbit()), color.getColor(), color.getColor().getAlpha(), event.context);
        Rotation rot = RotationUtils.correctSensitivity(RotationUtils.getSmoothRotation(Template.rotationManager().rotation(), RotationUtils.getRotations(mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)), MathUtils.closestPointToBox(PlayerUtils.renderBox(getCurrentRabbit()))), aimSpeed.getRandomDouble()));
        switch (rotMode.getMode()) {
            case Normal -> RotationUtils.setEntityRotation(mc.player, rot);
            case Silent -> Template.rotationManager().setRotation(rot);
        }
    }

    @EventHandler
    private void onInput(InputEvent event) {
        Entity rabbit = getCurrentRabbit();
        if (rabbit == null || !nullCheck())
            return;

        if (!(hr() instanceof EntityHitResult))
            event.input.movementForward = 1f;
        if (mc.player.horizontalCollision)
            event.input.jump();
    }

    private Entity getCurrentRabbit() {
        if (!nullCheck())
            return null;

        ArrayList<Entity> list = new ArrayList<>();
        mc.world.getEntities().forEach(list::add);
        list.removeIf(ent -> !(ent instanceof RabbitEntity) || ((RabbitEntity) ent).isDead() || isInAir(ent) || !PlayerUtils.canVectorBeSeen(mc.player.getPos(), MathUtils.closestPointToBox(mc.player.getPos(), ent.getBoundingBox())));
        list.sort(Comparator.comparing(ent -> MathUtils.closestPosBoxDistance(ent.getBoundingBox())));
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    private HitResult hr() {
        return PlayerUtils.getHitResult(mc.player, ent -> ent == getCurrentRabbit(), Template.rotationManager().yaw(), Template.rotationManager().pitch(), reach.value, false, 0);
    }

    private boolean isInAir(Entity e) {
        if (!onlyOnGround.isEnabled())
            return false;

        return mc.world.getBlockState(e.getBlockPos().down()).isAir();
    }
}
