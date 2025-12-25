package template.rip.module.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import template.rip.api.object.Description;
import template.rip.api.util.MathUtils;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;

public class ReachModule extends Module {

    public final NumberSetting entityReach = new NumberSetting(this, 3, 3, 6, 0.01, "Entity Reach");
    public final NumberSetting blockReach = new NumberSetting(this, 4.5, 4.5, 6, 0.01, "Block Reach");
    private final NumberSetting expand = new NumberSetting(this, 0, 0, 1, 0.01, "Box Increase");
    private final BooleanSetting render = new BooleanSetting(this, true, "Render Box");

    public enum itemWhitelistEnum{All_Items, Only_Weapon}
    public final ModeSetting<itemWhitelistEnum> itemWhitelist = new ModeSetting<>(this, itemWhitelistEnum.Only_Weapon, "Item Whitelist");

    private final BooleanSetting onlyTargets = new BooleanSetting(this, true, "Only Target's Box");
    private final NumberSetting hitboxChance = new NumberSetting(this, 100d, 0d, 100d, 1d, "Box Chance");

    public ReachModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public String getSuffix() {
        return " "+entityReach.getValue();
    }

    public float getHitboxSize(Entity entity) {
        if (!PlayerUtils.findTargets().contains(entity) && onlyTargets.isEnabled())
            return 0;

        if (itemWhitelist.is(itemWhitelistEnum.Only_Weapon)) {
            Item mainHandItem = mc.player.getMainHandStack().getItem();
            if (!(mainHandItem instanceof SwordItem || mainHandItem instanceof AxeItem))
                return 0;
        }

        int randomNum = MathUtils.getRandomInt(1, 100);

        if (randomNum <= hitboxChance.getIValue())
            return expand.getFValue();

        return 0;
    }

    public double getRenderHitboxSize(Entity entity) {
        if (!render.isEnabled())
            return 0;

        if (!PlayerUtils.findTargets().contains(entity) && onlyTargets.isEnabled())
            return 0;

        if (itemWhitelist.is(itemWhitelistEnum.Only_Weapon)) {
            Item mainHandItem = mc.player.getMainHandStack().getItem();
            if (!(mainHandItem instanceof SwordItem || mainHandItem instanceof AxeItem))
                return 0;
        }

        return expand.getValue();
    }
}
