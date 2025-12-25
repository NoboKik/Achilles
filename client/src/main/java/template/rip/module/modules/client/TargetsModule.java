package template.rip.module.modules.client;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;
import template.rip.module.setting.settings.RegistrySetting;

import java.util.Collections;

public class TargetsModule extends Module {

    public enum modePEnum{Distance, Health, HurtTime, FOV, Armor}
    public final ModeSetting<modePEnum> modeP = new ModeSetting<>(this, modePEnum.Distance, "Primary sorting metric");
    public enum modeSEnum{Distance, Health, HurtTime, FOV, Armor}
    public final ModeSetting<modeSEnum> modeS = new ModeSetting<>(this, modeSEnum.FOV, "Secondary sorting metric");

    public final NumberSetting search = new NumberSetting(this, 12, 0, 128, 1, "Max search distance");
    public final BooleanSetting onlySee = new BooleanSetting(this, false, "Only if can see");
    public final BooleanSetting other = new BooleanSetting(this, true, "Select Targets");
    public final BooleanSetting tabList = new BooleanSetting(this, false, "Only tablist");
    public final BooleanSetting players = new BooleanSetting(this, true, "Players");
    public final BooleanSetting mobs = new BooleanSetting(this, false, "Mobs");
    public final BooleanSetting animals = new BooleanSetting(this, false, "Animals");
    public final RegistrySetting<EntityType<?>> targetOptions = new RegistrySetting<>(Collections.singletonList(EntityType.PLAYER), this, Registries.ENTITY_TYPE, "Targets");
    public final BooleanSetting teammates = new BooleanSetting(this, true, "Teammates");
    public enum teammatesModeEnum{Name_Color, Vanilla_Teams}
    public final ModeSetting<teammatesModeEnum> teammatesMode = new ModeSetting<>(this, teammatesModeEnum.Vanilla_Teams, "Mode to detect teammates");
    public final BooleanSetting friends = new BooleanSetting(this, false, "Friends");

    public TargetsModule(Category category, Description description, String name) {
        super(category, description, name);
        targetOptions.addConditionBoolean(other, true);
        players.addConditionBoolean(other, false);
        mobs.addConditionBoolean(other, false);
        animals.addConditionBoolean(other, false);
    }

    @Override
    protected void enable() {}

    @Override
    protected void disable() {}
}
