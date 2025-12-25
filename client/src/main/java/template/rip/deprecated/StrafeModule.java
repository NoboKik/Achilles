package template.rip.deprecated;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.PlayerTickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

import java.util.Iterator;

public class StrafeModule extends Module {
    private final BooleanSetting onGround = new BooleanSetting(this, true, "OnGround");
    private final BooleanSetting inAir = new BooleanSetting(this, true, "InAir");
    private final BooleanSetting grim = new BooleanSetting(this, Description.of("Boosts your speed when near other entities"), false, "Grim Collision Strafe");
    public StrafeModule() {
        super(Category.BLATANT, Description.of("Increases control over strafe speed"), "Strafe");
    }

    @Override
    protected void enable() {
        if (!Template.isBlatant())
            return;

        super.enable();
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        if (mc.world == null || mc.player == null)
            return;

        if (grim.isEnabled()) {
            Iterator<Entity> it = mc.world.getEntities().iterator();

            Entity ent = null;
            while (it.hasNext()) {
                Entity e = it.next();

                if (e != mc.player && !(e instanceof ArmorStandEntity) && e instanceof LivingEntity && e.distanceTo(mc.player) <= 1.5)
                    ent = e;

            }

            if (ent != null) {
                if (mc.player.distanceTo(ent) <= 1.5) {
                    double veloX = mc.player.getVelocity().x * (mc.player.isOnGround() ? 1.2 * 1.12 : 1.1 * 1.09);
                    double veloZ = mc.player.getVelocity().z * (mc.player.isOnGround() ? 1.2 * 1.12 : 1.1 * 1.09);
                    mc.player.setVelocity(veloX, mc.player.getVelocity().y, veloZ);

                }
            }
            return;
        }

        if (onGround.isEnabled() && mc.player.isOnGround() || inAir.isEnabled() && !mc.player.isOnGround()) {
            if (!PlayerUtils.isPressingMoveInput(false))
                return;

            double rad = Math.toRadians(PlayerUtils.getMoveDirection() + 90);
            double moveSpeed = 0.15321679421194379; // Sprint walk speed
            double x = Math.abs(Math.cos(rad) * moveSpeed) > Math.abs(mc.player.getVelocity().x) ? Math.cos(rad) * moveSpeed : mc.player.getVelocity().x;
            double z = Math.abs(Math.sin(rad) * moveSpeed) > Math.abs(mc.player.getVelocity().z) ? Math.sin(rad) * moveSpeed : mc.player.getVelocity().z;
            Vec3d vec = new Vec3d(x, mc.player.getVelocity().y, z);
            mc.player.setVelocity(vec);
        }

    }
}
