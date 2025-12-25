package template.rip.module.modules.combat;

import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import template.rip.api.event.events.AttackEntityEvent;
import template.rip.api.event.events.HandleInputEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.event.orbit.EventPriority;
import template.rip.api.object.Description;
import template.rip.api.util.InvUtils;
import template.rip.api.util.SlotUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class MaceSwapModule extends Module {

    public enum mode{Mace, Custom_Slot}
    public final ModeSetting<mode> modeSetting = new ModeSetting<>(this, mode.Mace, "Mode");
    public final BooleanSetting workWithAxe = new BooleanSetting(this, true, "Swap while holding axe");
    public final NumberSetting fromSlot = new NumberSetting(this, 1d, 1d, 9d, 1d, "From Slot");
    public final NumberSetting toSlot = new NumberSetting(this, 1d, 1d, 9d, 1d, "To Slot");
    public final BooleanSetting switchBack = new BooleanSetting(this, true, "Switch Back");
    public final MinMaxNumberSetting switchBackTime = new MinMaxNumberSetting(this, 2, 4, 0, 20, 1, "Switch Back Delay");
    private int switchBackTimer;
    private int lastSlot;

    public MaceSwapModule(Category category, Description description, String name) {
        super(category, description, name);
        workWithAxe.addConditionMode(modeSetting, mode.Mace);
        fromSlot.addConditionMode(modeSetting, mode.Custom_Slot);
        toSlot.addConditionMode(modeSetting, mode.Custom_Slot);
        switchBackTime.addConditionBoolean(switchBack, true);
    }

    @Override
    public void onEnable() {
        switchBackTimer = -1;
        lastSlot = -1;
    }

    @EventHandler
    private void onTick(HandleInputEvent.Pre event) {
        if (!nullCheck()) {
            return;
        }
        if (mc.player.age > switchBackTimer && SlotUtils.isHotbar(lastSlot)) {
            if (switchBack.isEnabled()) {
                InvUtils.setInvSlot(lastSlot);
            }
            switchBackTimer = -1;
            lastSlot = -1;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onAttack(AttackEntityEvent.Pre event) {
        if (!nullCheck()) {
            return;
        }

        int lastQuestionMark = mc.player.getInventory().selectedSlot;
        boolean back = switch (modeSetting.getMode()) {
            case Mace -> {
                Item item = mc.player.getMainHandStack().getItem();
                if (!(item instanceof SwordItem) && (!workWithAxe.isEnabled() || !(item instanceof AxeItem))) {
                    yield false;
                }
                int i = InvUtils.getItemStackSlot(is -> is.getName().getString().toLowerCase().contains("mace"));
                if (i == -1) {
                    yield false;
                }
                InvUtils.setInvSlot(i);
                yield true;
            }
            case Custom_Slot -> {
                if (mc.player.getInventory().selectedSlot != fromSlot.getIValue() - 1) {
                    yield false;
                }
                InvUtils.setInvSlot(toSlot.getIValue() - 1);
                yield true;
            }
        };
        if (!back) {
            return;
        }
        switchBackTimer = mc.player.age + switchBackTime.getRandomInt();
        lastSlot = lastQuestionMark;
    }
}
