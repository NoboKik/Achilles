package template.rip.module.modules.crystal;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.ExcludeMode;
import template.rip.api.util.InvUtils;
import template.rip.api.util.SlotUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;
import template.rip.module.setting.settings.NumberSetting;
import template.rip.module.setting.settings.RegistrySetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LootHelperModule extends Module {

    public enum modeEnum{Legit, Normal}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Legit, "Loot Mode");
    public final MinMaxNumberSetting throwDelay = new MinMaxNumberSetting(this, 200, 300, 0, 500, 1, "Throw Delay");
    public final NumberSetting maxEmptySlots = new NumberSetting(this, 3, 0, 30, 1, "Max empty slots");
    public final RegistrySetting<Item> lootItems = new RegistrySetting<>(Arrays.asList(
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE,
            Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE,
            Items.ENDER_CHEST, Items.SHULKER_BOX,
            Items.OBSIDIAN, Items.GLOWSTONE, Items.RESPAWN_ANCHOR, Items.END_CRYSTAL,
            Items.GOLDEN_APPLE, Items.GOLDEN_CARROT,
            Items.BOW, Items.CROSSBOW
    ), this, Registries.ITEM, "Loot Items");

    private long dropTimer;
    private HashMap<Integer, Long> lastClick;

    public LootHelperModule(Category category, Description description, String name) {
        super(category, description, name);
    }

    @Override
    public void onEnable() {
        lastClick = new HashMap<>();
    }

    private int emptySlots() {
        int slots = 0;
        for (ItemStack is : mc.player.getInventory().main) {
            if (is.isEmpty()) {
                slots++;
            }
        }
        return slots;
    }

    @EventHandler
    private void onRender2D(HudRenderEvent event) {
        if (!nullCheck() || !(mc.currentScreen instanceof InventoryScreen inv) || emptySlots() >= maxEmptySlots.getIValue())
            return;

        if (dropTimer < System.currentTimeMillis()) {
            ArrayList<Integer> sl = new ArrayList<>();
            lastClick.forEach((i, l) -> {
                if (System.currentTimeMillis() - l < 500) {
                    sl.add(i);
                }
            });

            switch (mode.getMode()) {
                case Normal: {
                    int slot = InvUtils.screenSlotOfItem(i -> !lootItems.selected.contains(i.getItem()), inv.getScreenHandler(), ExcludeMode.HotbarAndOffhand, sl);
                    InvUtils.drop().from(slot).to(slot);
                    lastClick.put(slot, System.currentTimeMillis());
                    dropTimer = System.currentTimeMillis() + throwDelay.getRandomInt();
                    break;
                }
                case Legit: {
                    Slot slot = inv.focusedSlot;
                    if (slot != null && SlotUtils.isMain(slot.getIndex()) && System.currentTimeMillis() - lastClick.getOrDefault(slot.getIndex(), 0L) > 500 && !lootItems.selected.contains(slot.getStack().getItem())) {
                        InvUtils.drop().from(slot.getIndex()).to(slot.getIndex());
                        lastClick.put(slot.getIndex(), System.currentTimeMillis());
                        dropTimer = System.currentTimeMillis() + throwDelay.getRandomInt();
                    }
                    break;
                }
            }
        }
    }
}
