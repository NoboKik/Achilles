package template.rip.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.render.Camera;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import template.rip.Template;
import template.rip.api.event.events.HudRenderEvent;
import template.rip.api.event.events.NameTagRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.font.JColor;
import template.rip.api.object.Description;
import template.rip.api.object.Rectangle;
import template.rip.api.util.*;
import template.rip.gui.ImguiLoader;
import template.rip.gui.utils.Renderable;
import template.rip.module.Module;
import template.rip.module.setting.settings.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NewNameTagsModule extends Module implements Renderable {

    public final DividerSetting generalDivider = new DividerSetting(this, false, "General");
    public final BooleanSetting scaling = new BooleanSetting(this, false, "Scaling");
    public final NumberSetting nameScale = new NumberSetting(this, 1, 0.01, 20, 0.01, "Scale");
    public final BooleanSetting scaleRange = new BooleanSetting(this, false, "Scale Range Enabled");
    public final MinMaxNumberSetting nameScaleRange = new MinMaxNumberSetting(this, 0.01, 40, 0.01, 40, 0.01, "Scale Range");
    public final NumberSetting height = new NumberSetting(this, 0, 0, 60, 1, "Height");
    public final NumberSetting paddingVertical = new NumberSetting(this, 5, 0, 50, 1, "Padding Vertical");
    public final NumberSetting paddingHorizontal = new NumberSetting(this, 5, 0, 50, 1, "Padding Horizontal");
    public final NumberSetting rounding = new NumberSetting(this, 6, 0, 30, 1, "Rounding");
    public final BooleanSetting showHP = new BooleanSetting(this, true, "Show HP");
    public final BooleanSetting showPing = new BooleanSetting(this, true, "Show Ping");
    public final BooleanSetting showTotemPops = new BooleanSetting(this, true, "Show Totem Pops");
    public final NumberSetting textOffsetY = new NumberSetting(this, 0, -30, 30, 1, "Text Offset Y");
    public final BooleanSetting textShadow = new BooleanSetting(this, false, "Text Shadow");
    public final BooleanSetting armor = new BooleanSetting(this, true, "Armor");
    public final BooleanSetting armorDura = new BooleanSetting(this, true, "Armor Durability");
    public final BooleanSetting armorEnchants = new BooleanSetting(this, true, "Armor Enchants");
    public final NumberSetting armorPadding = new NumberSetting(this, 14, 0, 50, 1, "Armor Padding Y");
    public final NumberSetting armorSpacing = new NumberSetting(this, 1.4, 0, 10, 0.01, "Armor Spacing");
    public final DividerSetting colorsDivider = new DividerSetting(this, false, "Colors");
    public final ColorSetting windowBg = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.19f, 0.8f), true, "Background");
    public final ColorSetting textColor = new ColorSetting(this, new JColor(0.83f, 0.86f, 0.94f, 1.00f), true, "Text");
    public final ColorSetting hpTextColor = new ColorSetting(this, new JColor(0f, 1f, 0f, 1f), true, "HP Text Color");
    public final BooleanSetting hpRelativeColor = new BooleanSetting(this, false, "HP Relative Color");
    public final BooleanSetting useTeamColor = new BooleanSetting(this, false, "Use Team Color");
    public final DividerSetting glowDivider = new DividerSetting(this, false, "Glow");
    public final BooleanSetting glowEnabled = new BooleanSetting(this, false, "Glow Enabled");
    public final ColorSetting glowColor = new ColorSetting(this, new JColor(0.13f, 0.14f, 0.18f, 0.7f), true, "Glow Color");
    public final NumberSetting glowSize = new NumberSetting(this, 30, 0, 60, 1, "Glow Size");

    public NewNameTagsModule(Category category, Description description, String name) {
        super(category, description, name);
        mergeDividers();
    }

    @Override
    public void onDisable() {
        ImguiLoader.queueRemove(this);
    }

    @Override
    public void onEnable() { ImguiLoader.addRenderable(this); }

    @EventHandler
    public void onNametag(NameTagRenderEvent event) {
        if (this.isEnabled() && CrystalUtils.positions.containsKey(event.entity)) event.setCancelled(true);
    }

    private double distance(Vec3d vec) {
        Camera cam = mc.gameRenderer.getCamera();
        if (cam != null) return cam.getPos().distanceTo(vec);
        else return vec.length();
    }

    @Override
    public void render() {
        if (!Template.displayRender() || !nullCheck())
            return;

        int imGuiWindowFlags = 0;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBackground;
        imGuiWindowFlags |= ImGuiWindowFlags.NoTitleBar;
        imGuiWindowFlags |= ImGuiWindowFlags.NoDocking;
        imGuiWindowFlags |= ImGuiWindowFlags.NoFocusOnAppearing;
        imGuiWindowFlags |= ImGuiWindowFlags.NoResize;
        imGuiWindowFlags |= ImGuiWindowFlags.NoBringToFrontOnFocus;
        ImGui.setNextWindowSize(1, 1);
        ImGui.setNextWindowPos(10000, 10000);
        ImGui.begin(this.getName(), imGuiWindowFlags);
        for (Map.Entry<Entity, Pair<Rectangle, Boolean>> e : CrystalUtils.getEntrySet()) {
            if (mc.world == null || mc.world.getEntityById(e.getKey().getId()) == null || !PlayerUtils.findTargets(true).contains(e.getKey()) || !e.getValue().getLeft().safe()) {
                continue;
            }

            if (e.getValue().getRight()) {
                float scale = (float) (mc.getWindow().getScaleFactor());
                float[] c = windowBg.getColor().getFloatColorWAlpha();

                Entity entity = e.getKey();

                double mult;

                double distance = distance(entity.getPos());
                float val = 1;
                if (scaling.isEnabled()) {
                    float min = nameScaleRange.getFMinValue();
                    float max = nameScaleRange.getFMaxValue();
                    val = (float) (1f/(distance))*nameScale.getFValue();
                    if (scaleRange.isEnabled()) {
                        if (val < min) val = min;
                        if (val > max) val = max;
                    }
                    mult = val;
                } else mult = nameScale.getFValue();

                int size = (int) Math.round(24 * mult);
                size = (int) MathUtils.coerceIn(MathUtils.round(size, 8.0), 18, 48);
                if (size == 40) {
                    size = 48;
                }

                ImFont font = UI.getFont(size);

                String text;
                String textNoColors;
                String name = entity.getType().getName().getString();
                String hpText = "";
                String pingText = "";
                String popText = "";
                if (entity instanceof PlayerEntity) {
                    name = entity.getName().getString();
                }
                if (showHP.isEnabled() && entity instanceof LivingEntity) {
                    hpText = " "+Math.floor(((LivingEntity) entity).getHealth())+" HP";
                    hpText = hpText.replace(".0", "");
                }
                if (showPing.isEnabled()) {
                    String ping = "0";
                    try {
                        if (entity instanceof PlayerEntity)
                            ping = String.valueOf(mc.getNetworkHandler().getPlayerListEntry(((PlayerEntity) entity).getGameProfile().getName()).getLatency());
                    } catch (Exception ignored) {}
                    pingText = String.format(" %sms", ping);
                }
                if (showTotemPops.isEnabled()) {
                    int pops = 0;
                    try {
                        if (entity instanceof PlayerEntity)
                            pops = CrystalUtils.totemPops(((PlayerEntity) entity));
                    } catch (Exception ignored) {}
                    if (pops != 0) popText = " -"+pops;
                }
                text = String.format("%s%s%s%s", name, pingText, popText, hpText);
                textNoColors = String.format("%s%s%s", name, pingText, popText);
                float[] c3 = textColor.getColor().getFloatColorWAlpha();
                ImGui.pushFont(font);
                float offsetX = ImGui.calcTextSize(text).x / 2;
                float offsetY = ImGui.calcTextSize(text).y+height.getFValue()*val;

                float x2 = (float) e.getValue().getLeft().x * scale + ((float) e.getValue().getLeft().z * scale - (float) e.getValue().getLeft().x * scale) / 2;
                float y2 = (float) e.getValue().getLeft().y * scale;

                ImGui.getBackgroundDrawList().addRectFilled(
                        x2 - offsetX - paddingHorizontal.getFValue()*val,
                        y2 - offsetY - paddingVertical.getFValue()*val,
                        x2 + offsetX + paddingHorizontal.getFValue()*val,
                        y2 - height.getFValue()*val + paddingVertical.getFValue()*val,
                        ImGui.getColorU32(c[0], c[1], c[2], c[3]),
                        rounding.getFValue()*val
                );

                int col = e.getKey().getTeamColorValue();
                int red = (col >> 16) & 0xFF;
                int green = (col >> 8) & 0xFF;
                int blue = col & 0xFF;

                int finalColor = ImGui.getColorU32(c3[0], c3[1], c3[2], c3[3]);
                int finalColorShadow = ImGui.getColorU32(c3[0]/4f, c3[1]/4f, c3[2]/4f, c3[3]);
                if (useTeamColor.isEnabled()) {
                    finalColor = ImGui.getColorU32(red/255f, green/255f, blue/255f, 1f);
                    finalColorShadow = ImGui.getColorU32(red/255f/4f, green/255f/4f, blue/255f/4f, 1f);
                }

                if (textShadow.isEnabled())
                    ImGui.getBackgroundDrawList().addText(x2 - offsetX+2, y2 - offsetY+2+textOffsetY.getFValue(), finalColorShadow, textNoColors);
                ImGui.getBackgroundDrawList().addText(x2 - offsetX, y2 - offsetY+textOffsetY.getFValue(), finalColor, textNoColors);

                if (hpRelativeColor.isEnabled() && entity instanceof LivingEntity le) {
                    float divide = le.getHealth() / le.getMaxHealth();
                    int g = Math.round(255 * divide);
                    int r = 255 - Math.round(255 * divide);
                    if (le.getAbsorptionAmount() > 0f) {
                        r = 255;
                        g = 255;
                    }

                    if (textShadow.isEnabled())
                        ImGui.getBackgroundDrawList().addText(x2 - offsetX + ImGui.calcTextSize(textNoColors).x + 2, y2 - offsetY + 2+textOffsetY.getFValue(), ImGui.getColorU32((float) (r / 255) / 4, ((float) g / 255) / 4, 0, 1f), hpText);

                    ImGui.getBackgroundDrawList().addText(x2 - offsetX + ImGui.calcTextSize(textNoColors).x, y2 - offsetY+textOffsetY.getFValue(), ImGui.getColorU32((float) r / 255, (float) g / 255, 0, 1f), hpText);
                } else {
                    float[] c4 = hpTextColor.getColor().getFloatColorWAlpha();
                    if (textShadow.isEnabled())
                        ImGui.getBackgroundDrawList().addText(x2 - offsetX + ImGui.calcTextSize(textNoColors).x + 2, y2 - offsetY + 2+textOffsetY.getFValue(), ImGui.getColorU32(c4[0]/4f, c4[1]/4f, c4[2]/4f, c4[3]), hpText);

                    ImGui.getBackgroundDrawList().addText(x2 - offsetX + ImGui.calcTextSize(textNoColors).x, y2 - offsetY+textOffsetY.getFValue(), ImGui.getColorU32(c4[0], c4[1], c4[2], c4[3]), hpText);
                }
                ImGui.getFont().setScale(1f);
                font.setScale(1f);
                ImGui.popFont();

                // Glow
                if (glowEnabled.isEnabled()) {
                    float[] glowColorF = glowColor.getColor().getFloatColorWAlpha();
                    float alpha = glowColorF[3];

                    ImVec2 pos1 = new ImVec2(x2 - offsetX - paddingHorizontal.getFValue(), y2 - offsetY - paddingVertical.getFValue());
                    ImVec2 pos2 = new ImVec2(x2 + offsetX + paddingHorizontal.getFValue(), y2 - height.getFValue() + paddingVertical.getFValue());

                    GuiUtils.drawShadow(pos1, pos2, rounding.getFValue(), ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], alpha), ImGui.getColorU32(glowColorF[0], glowColorF[1], glowColorF[2], 0f), glowSize.getFValue());
                }

                // Dura
                if (armor.isEnabled() && armorDura.isEnabled() && entity instanceof LivingEntity le) {
                    scale = (float) (mc.getWindow().getScaleFactor());

                    float width = 16 * 4 * scale;

                    offsetX = (width / 2) + (16 * scale * armorSpacing.getFValue());
                    offsetY = (armorPadding.getFValue() + 30f) * scale;

                    x2 = (float) e.getValue().getLeft().x * scale + ((float) e.getValue().getLeft().z * scale - (float) e.getValue().getLeft().x * scale) / 2;
                    y2 = (float) e.getValue().getLeft().y * scale;

                    HashMap<Integer, ItemStack> armors = new HashMap<>();
                    armors.put(0, le.getEquippedStack(EquipmentSlot.MAINHAND));
                    armors.put(1, le.getEquippedStack(EquipmentSlot.OFFHAND));
                    armors.put(2, le.getEquippedStack(EquipmentSlot.HEAD));
                    armors.put(3, le.getEquippedStack(EquipmentSlot.CHEST));
                    armors.put(4, le.getEquippedStack(EquipmentSlot.LEGS));
                    armors.put(5, le.getEquippedStack(EquipmentSlot.FEET));

                    for (int i = 0; i <= 5; i++) {
                        ItemStack item = armors.get(i);
                        if (item != null && item.isDamageable() && !item.isEmpty()) {
                            String str = String.valueOf(item.getMaxDamage() - item.getDamage());
                            ImGui.pushFont(font);
                            float strX = ImGui.calcTextSize(str).x;
                            ImGui.popFont();
                            ImGui.getBackgroundDrawList().addText(
                                    font,
                                    24,
                                    x2 - offsetX + (i * 16 * scale * armorSpacing.getFValue()) + 8 * scale - strX / 2,
                                    (y2 - offsetY) + 16 * scale,
                                    new JColor(colDamage(item)).getU32(),
                                    str
                            );

                            if (armorEnchants.isEnabled()) {
                                ArrayList<Object2IntMap.Entry<RegistryEntry<Enchantment>>> nbtList = new ArrayList<>(item.getEnchantments().getEnchantmentEntries());
                                for (int count = 0; count < nbtList.size(); count++) {
                                    Object2IntMap.Entry<RegistryEntry<Enchantment>> ench = nbtList.get(count);
                                    try {
                                        int level = ench.getIntValue();
                                        String format = formatEnchant(ench.getKey().getIdAsString()) + level;

                                        ImGui.getBackgroundDrawList().addText(
                                                font,
                                                16,
                                                x2 - offsetX + (i * 16 * scale * armorSpacing.getFValue()) + 8 * scale - strX / 2,
                                                y2 - offsetY - (count + 1) * 20 + -4 * scale,
                                                textColor.getColor().getU32(),
                                                format
                                        );
                                    } catch (Exception exc) {//just in case
                                        exc.printStackTrace(System.err);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        ImGui.end();
    }

    private String formatEnchant(String enchant) {
        StringBuilder finalString = new StringBuilder();
        if (enchant.contains("minecraft:")) {
            enchant = enchant.replace("minecraft:", "");
        }
        String[] words = enchant.split("_");
        for (String word : words) {
            String str = word;
            for (String c : Arrays.asList("a", "e", "u", "i"/*, "o"*/)) {
                if (str.charAt(0) != c.charAt(0)) {
                    str = str.replace(c, "");
                }
            }
            if (str.length() > 3) {
                str = str.substring(0, 4);
            }
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
            finalString.append(str);
        }
        return finalString.toString();
    }

    @EventHandler
    private void onRender(HudRenderEvent event) {
        if (!armor.isEnabled()) return;
        for (Map.Entry<Entity, Pair<Rectangle, Boolean>> e : CrystalUtils.getEntrySet()) {
            if (mc.world == null || mc.world.getEntityById(e.getKey().getId()) == null || !PlayerUtils.findTargets(true).contains(e.getKey()) || !e.getValue().getLeft().safe()) {
                continue;
            }

            if (e.getValue().getRight() && e.getKey() instanceof LivingEntity entity) {
                float scale = (float) (mc.getWindow().getScaleFactor());
                float armorScal = 1f; //armorScale.getFValue() * 5f;

                float width = 16*4*scale;


                float offsetX = (width / 2) + (16*scale*armorSpacing.getFValue());
                float offsetY = (armorPadding.getFValue() + 30f)*scale;

                float x2 = (float) e.getValue().getLeft().x * scale + ((float) e.getValue().getLeft().z * scale - (float) e.getValue().getLeft().x * scale) / 2;
                float y2 = (float) e.getValue().getLeft().y * scale;

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                HashMap<Integer, ItemStack> armors = new HashMap<>();
                armors.put(0, entity.getEquippedStack(EquipmentSlot.MAINHAND));
                armors.put(1, entity.getEquippedStack(EquipmentSlot.OFFHAND));
                armors.put(2, entity.getEquippedStack(EquipmentSlot.HEAD));
                armors.put(3, entity.getEquippedStack(EquipmentSlot.CHEST));
                armors.put(4, entity.getEquippedStack(EquipmentSlot.LEGS));
                armors.put(5, entity.getEquippedStack(EquipmentSlot.FEET));

                for (int i = 0; i <= 5; i++) {
                    ItemStack item = armors.get(i);
                    if (item != null && !item.isEmpty()) {
                        RenderUtils.renderHotbarItem(event.context, (x2 - offsetX + (i * 16 * scale * armorSpacing.getFValue())) / scale, (y2 - offsetY) / scale, event.tickDelta, entity, item, 1, armorScal);
                    }
                }
                RenderSystem.disableBlend();
            }
        }
    }

    public Color colDamage(ItemStack stack) {
        if (!stack.isDamageable()) {
            return new Color(255, 255, 255);
        }
        float divide = (float) stack.getDamage() / stack.getMaxDamage();
        int green = 255 -Math.round(255 * divide);
        int red = Math.round(255 * divide);
        return new Color(red, green, 0);
    }
}
