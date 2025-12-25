package template.rip.module.modules.render;

import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.SweepAttackParticle;
import template.rip.api.event.events.FloatingItemEvent;
import template.rip.api.event.events.ParticleEvent;
import template.rip.api.event.events.SubTitleEvent;
import template.rip.api.event.events.TitleEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;

public class NoRenderModule extends Module {

    public final BooleanSetting powderSnow = new BooleanSetting(this, false, "Powder snow");
    public final BooleanSetting pumpkin = new BooleanSetting(this, false, "Pumpkin blur");
    public final BooleanSetting fire = new BooleanSetting(this, false, "Fire");
    public final BooleanSetting totemPop = new BooleanSetting(this, false, "Totem Pop");
    public final BooleanSetting shieldDelay = new BooleanSetting(this, false, "Shield delay");
    public final BooleanSetting blindness = new BooleanSetting(this, false, "Blindness");
    public final BooleanSetting nausea = new BooleanSetting(this, false, "Nausea");
    public final BooleanSetting explosion = new BooleanSetting(this, false, "Explosions");
    public final BooleanSetting sweep = new BooleanSetting(this, false, "Sweep Particles");
    public final BooleanSetting title = new BooleanSetting(this, false, "Titles");
    public final BooleanSetting subTitle = new BooleanSetting(this, false, "SubTitles");
    public final BooleanSetting mineCart = new BooleanSetting(this, false, "MineCarts");
    public final BooleanSetting fishBobber = new BooleanSetting(this, false, "Fishing Bobber");

    public NoRenderModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @EventHandler
    private void onParticle(ParticleEvent event) {
        if (explosion.isEnabled() && event.particle instanceof ExplosionLargeParticle)
            event.cancel();
        if (sweep.isEnabled() && event.particle instanceof SweepAttackParticle)
            event.cancel();
    }

    @EventHandler
    private void onTitle(TitleEvent event) {
        if (title.isEnabled())
            event.cancel();
    }

    @EventHandler
    private void onSubTitle(SubTitleEvent event) {
        if (subTitle.isEnabled())
            event.cancel();
    }

    @EventHandler
    private void onFloatingItem(FloatingItemEvent event) {
        if (totemPop.isEnabled())
            event.cancel();
    }
}
