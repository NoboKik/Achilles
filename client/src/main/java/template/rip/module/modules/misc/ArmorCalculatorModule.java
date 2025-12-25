package template.rip.module.modules.misc;

import net.minecraft.entity.LivingEntity;
import template.rip.Template;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.EntityHealthEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.ArmorDamage;
import template.rip.api.object.Description;
import template.rip.api.util.DamageUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;

import java.util.concurrent.ConcurrentHashMap;

public class ArmorCalculatorModule extends Module {

    public ConcurrentHashMap<LivingEntity, ArmorDamage> calculatedDamage = new ConcurrentHashMap<>();

    public ArmorCalculatorModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        calculatedDamage.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null) {
            return;
        }
        calculatedDamage.forEach((p, a) -> a.setDamage());
    }

    @EventHandler
    private void onAttack(AttackEntityEvent.Pre event) {
//      if (event.target instanceof LivingEntity) {
//          mc.inGameHud.getChatHud().addMessage(Text.of("dmg: " + DamageUtils.getNonReducedDamage(mc.player, le)));
//      }
    }
    @EventHandler
    private void onHealth(EntityHealthEvent event) {
        if (!nullCheck() || event.type != EntityHealthEvent.Type.Damage || (!event.calculator && Template.moduleManager.isModuleEnabled(HealthCalculatorModule.class))) {
            return;
        }

        /*if (!(event.entity instanceof PlayerEntity pe)) {
            return;
        }*/
        LivingEntity pe = event.entity;
        if (event.entity != PlayerUtils.findFirstTarget()) {
            return;
        }
        ArmorDamage ad = calculatedDamage.getOrDefault(pe, new ArmorDamage(pe));
        double damage = DamageUtils.nonReducedDamage(event.fromHP - event.toHP, pe);
        ad.damageArmor((float) damage);
//        mc.inGameHud.getChatHud().addMessage(Text.of(" " + damage+ " " +Arrays.toString(ad.damage)));
        calculatedDamage.put(pe, ad);
    }
}