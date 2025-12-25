package template.rip.module.modules.player;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.commons.lang3.StringUtils;
import template.rip.api.event.events.ChatEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.util.ExcludeMode;
import template.rip.api.util.InvUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.MinMaxNumberSetting;
import template.rip.module.setting.settings.ModeSetting;

import java.util.Objects;
import java.util.function.Predicate;

public class AutoQueueModule extends Module {

    public enum modeEnum{Hoplite_Requeue, Hoplite_GUI}
    public final ModeSetting<modeEnum> mode = new ModeSetting<>(this, modeEnum.Hoplite_Requeue, "Server Mode");
    public final MinMaxNumberSetting queueDelay = new MinMaxNumberSetting(this, 1000, 1500, 250, 2500, 1, "Queue Delay");

    private boolean wasInLobby;
    private long queueTimer;
    private boolean queued;

    public AutoQueueModule() {
        super(Category.PLAYER, Description.of("Automatically queues duels on servers"), "AutoQueue");
    }

    @Override
    public void onEnable() {
        wasInLobby = false;
        queueTimer = 0;
        queued = false;
    }

    @EventHandler
    private void chatEvent(ChatEvent.Game event) {
        if (mode.is(modeEnum.Hoplite_GUI)) {
            if (event.message.getString().contains("Entered queue!")) {
                queued = true;
            }
            if (event.message.getString().contains("Left queue!")) {
                queued = false;
            }
        }
    }

    @EventHandler
    private void onPlayerTick(TickEvent.Pre event) {
        if (!nullCheck()) {
            return;
        }
        switch (mode.getMode()) {
            case Hoplite_Requeue: {
                ItemStack is = mc.player.getInventory().getStack(7);
                if (isInLobby()) {
                    if (!wasInLobby) {
                        queueTimer = System.currentTimeMillis() + queueDelay.getRandomInt();
                        wasInLobby = true;
                    }
                    if (queueTimer < System.currentTimeMillis()) {
                        if (!is.isEmpty()) {
                            if (mc.player.getInventory().selectedSlot != 7) {
                                InvUtils.setInvSlot(7);
                            } else {
                                mc.doItemUse();
                            }
                            queueTimer = System.currentTimeMillis() + queueDelay.getRandomInt();
                        }
                    }
                } else {
                    wasInLobby = false;
                    if (mc.player.getInventory().selectedSlot != 0 && queueTimer < System.currentTimeMillis()) {
                        mc.player.getInventory().selectedSlot = 0;
                    }
                }
                break;
            }
            case Hoplite_GUI: {
                if (needToLeave()) {
                    if (queueTimer < System.currentTimeMillis()) {
                        if (mc.player.getInventory().selectedSlot != 8) {
                            InvUtils.setInvSlot(8);
                        } else {
                            mc.doItemUse();
                        }
                        queueTimer = System.currentTimeMillis() + queueDelay.getRandomInt();
                    }
                } else if (isInLobby()) {
                    if (queueTimer < System.currentTimeMillis() && !queued) {
                        if (mc.player.getInventory().selectedSlot != 2) {
                            InvUtils.setInvSlot(2);
                        } else if (!(mc.currentScreen instanceof HandledScreen<?>)) {
                            mc.doItemUse();
                        } else if (mc.currentScreen instanceof HandledScreen<?> hs) {
                            Predicate<ItemStack> casualPred = item -> StringUtils.difference(item.getName().getString(), "Casual Duels").isEmpty();
                            Predicate<ItemStack> oneVonePred = item -> StringUtils.difference(item.getName().getString(), "Casual - 1v1").isEmpty();
                            Predicate<ItemStack> swordPred = item -> StringUtils.difference(item.getName().getString(), "Sword").isEmpty();
                            /*Slot slot = hs.focusedSlot;
                            if (slot != null && stack != slot.getStack()) {
                                stack = slot.getStack();
                                String str = stack.getName().getString();
                                mc.inGameHud.getChatHud().addMessage(Text.of(str));
                            }*/
                            int casual = InvUtils.screenSlotOfItem(casualPred, hs.getScreenHandler(), ExcludeMode.None);
                            if (casual != -1) {
                                InvUtils.click().fromId(casual).toId(casual);
                            } else {
                                int oneVone = InvUtils.screenSlotOfItem(oneVonePred, hs.getScreenHandler(), ExcludeMode.None);
                                if (oneVone != -1) {
                                    InvUtils.click().fromId(oneVone).toId(oneVone);
                                } else {
                                    int sword = InvUtils.screenSlotOfItem(swordPred, hs.getScreenHandler(), ExcludeMode.None);
                                    if (sword != -1) {
                                        InvUtils.click().fromId(sword).toId(sword);
                                    }
                                }
                            }
                        }
                        queueTimer = System.currentTimeMillis() + queueDelay.getRandomInt();
                    }
                } else {
                    if (mc.player.getInventory().selectedSlot != 0 && queueTimer < System.currentTimeMillis()) {
                        mc.player.getInventory().selectedSlot = 0;
                    }
                    queued = false;
                }
                break;
            }
        }
    }

    private boolean isInLobby() {
        return switch (mode.getMode()) {
            case Hoplite_Requeue -> mc.player.getInventory().getArmorStack(1).isEmpty() && mc.player.getInventory().getArmorStack(2).isEmpty() && mc.player.getInventory().getArmorStack(3).isEmpty();
            case Hoplite_GUI -> mc.player.getInventory().getStack(2).isOf(Items.PAPER);
        };
    }
    private boolean needToLeave() {
        if (Objects.requireNonNull(mode.getMode()) == modeEnum.Hoplite_GUI) {
            return mc.player.getInventory().getStack(8).isOf(Items.PAPER);
        }
        return false;
    }

}
