package template.rip.deprecated;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Pair;
import net.minecraft.world.GameMode;
import org.joml.Matrix4f;
import template.rip.Template;
import template.rip.api.event.events.NameTagRenderEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.api.object.TextBuilder;
import template.rip.api.util.PlayerUtils;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.ArrayList;
import java.util.List;

public class NameTagsModule extends Module {
    public final NumberSetting size = new NumberSetting(this, 3d, 1d, 6d, 0.1d, "Size");
    private final BooleanSetting pings = new BooleanSetting(this, false, "Ping");
    private final BooleanSetting gamemodes = new BooleanSetting(this, false, "Gamemode");
    private final BooleanSetting distances = new BooleanSetting(this, false, "Distance");
    private final BooleanSetting itemTimes = new BooleanSetting(this, false, "Item use time");
    private final BooleanSetting healths = new BooleanSetting(this, false, "Health");
    private final BooleanSetting totems = new BooleanSetting(this, false, "Totem Pops");
    private final BooleanSetting hurttimes = new BooleanSetting(this, false, "HurtTime");
    private final BooleanSetting teams = new BooleanSetting(this, false, "Team");

    public NameTagsModule() {
        super(Category.RENDER, Description.of("Renders custom player nametags"), "NameTags");
    }
    // Most of this code is copy and pasted from an unreleased mod of mine, I really do not want to rewrite all of it, since it'll just be the same thing anyway
    @EventHandler
    private void onTag(NameTagRenderEvent event) {
        if (mc.player == null)
            return;

        if (!(PlayerUtils.findTargets(true).contains(event.entity) && event.entity instanceof LivingEntity))
            return;

        LivingEntity ent = (LivingEntity) event.entity;
        event.cancel();

        Pair<String, String> ping = pair("0", "ping");

        Pair<String, String> gamemode = pair("", "gamemode");

        if (event.entity instanceof PlayerEntity) {
            PlayerEntity enty = (PlayerEntity) event.entity;

            try {
                ping.setLeft(String.valueOf(mc.getNetworkHandler().getPlayerListEntry((enty).getGameProfile().getName()).getLatency()));
            } catch (Exception ignored) {}
            GameMode gm = null;
            try {
                gm = mc.getNetworkHandler().getPlayerListEntry((enty).getGameProfile().getName()).getGameMode();
            } catch (Exception ignored) {}

            if (gm != null) {
                String gammode = switch (gm) {
                    case CREATIVE -> "C";
                    case SURVIVAL -> "S";
                    case ADVENTURE -> "A";
                    case SPECTATOR -> "SP";
                };
                gamemode.setLeft(gammode);
            }
        }
        String space = " ";

        StringBuilder useTimeSB = new StringBuilder();
        useTimeSB.append("ItemTime: ");
        useTimeSB.append(ent.getItemUseTime());
        if (ent.getItemUseTime() != 0) {
            useTimeSB.append('/');
            useTimeSB.append(ent.getItemUseTimeLeft());
        }

        Pair<String, String> useTime = pair(useTimeSB.toString(), "useTime");

        Pair<String, String> name = pair(event.entity.getDisplayName() != null ? event.entity.getDisplayName().getString() : "", "name");

        Pair<String, String> health = pair("", "health");

        StringBuilder healthSB = new StringBuilder();
        healthSB.append((double) Math.round(ent.getHealth() * 100.0) / 100.0);
        if (ent.getHealth() != ent.getMaxHealth()) {
            healthSB.append('/');
            healthSB.append((double) Math.round(ent.getMaxHealth() * 100.0) / 100.0);
        }
        if (ent.getAbsorptionAmount() != 0) {
            healthSB.append('+');
            healthSB.append((double) Math.round(ent.getAbsorptionAmount() * 100.0) / 100.0);
            health.setRight("number is dumb");
        }
        healthSB.append(" HP");
        health.setLeft(healthSB.toString());

        Pair<String, String> hurtTime = pair("HT: ".concat(String.valueOf(ent.hurtTime)), "hurtTime");

        Pair<String, String> distance = pair(mc.player != null ? "Dist: ".concat(String.valueOf(Math.round(Math.sqrt(mc.player.squaredDistanceTo(ent)) * 100.0) / 100.0)) : "", "distance");

        Pair<String, String> team = pair(ent.getScoreboardTeam() != null ? "Team: ".concat(ent.getScoreboardTeam().getName()) : "", "team");

        Pair<String, String> totemPops = pair("Pops: ".concat(String.valueOf(Template.crystalUtils().totemPops(ent))), "totemPops");

        List<Pair<String, String>> userSelection = new ArrayList<>();

        if (pings.isEnabled())
            userSelection.add(ping);

        if (gamemodes.isEnabled())
            userSelection.add(gamemode);

        if (distances.isEnabled())
            userSelection.add(distance);

        if (itemTimes.isEnabled())
            userSelection.add(useTime);

        userSelection.add(name);

        if (healths.isEnabled())
            userSelection.add(health);

        if (totems.isEnabled())
            userSelection.add(totemPops);

        if (hurttimes.isEnabled())
            userSelection.add(hurtTime);

        if (teams.isEnabled())
            userSelection.add(team);

        TextBuilder tb = new TextBuilder();
        for (Pair<String, String> pair : userSelection) {

            if (!pair.getLeft().isEmpty()) {
                switch (pair.getRight()) {
                    case "number is dumb": tb.add(colored(pair.getLeft(), "number is dumb", ent)); break;
                    case "health": tb.add(colored(pair.getLeft(), "health", ent)); break;
                    case "team": tb.add(colored(pair.getLeft(), "team", ent)); break;
                    case "hurtTime": tb.add(colored(pair.getLeft(), "hurtTime", ent)); break;
                    case "ping": tb.add(colored(pair.getLeft(), "ping", ent)); break;
                    case "gamemode": tb.add(colored(pair.getLeft(), "gamemode", ent)); break;
                    default: tb.add(pair.getLeft());
                }
                tb.add(space);
            }
        }
        renderLabelIfPresent(event.entity, tb.currentText, event.matrices, event.vertexConsumers);
    }

    private MutableText colored(String str, String preset, LivingEntity ent) {
        int r = 0;
        int g = 0;
        int b = 0;
        int altRGB = 0;
        switch (preset) {
            case "health" : {
                Pair<Integer, Integer> pr = hpRatio(ent);
                r = pr.getRight();
                g = pr.getLeft();
                break;
            }
            case "number is dumb" : {
                r = 255;
                g = 165;
                break;
            }
            case "team" : altRGB = ent.getScoreboardTeam() != null && ent.getScoreboardTeam().getColor().getColorValue() != null ? ent.getScoreboardTeam().getColor().getColorValue() : 0; break;
            case "hurtTime" : {
                r = 255;
                Pair<Integer, Integer> pr = htRatio(ent);
                g = pr.getRight();
                b = pr.getLeft();
                break;
            }
            case "ping" : {
                int pong = Integer.parseInt(str);
                // 0-149, green
                // 150-299, yellow
                // 300-599, orange
                // 600-999, light red
                // 1000+, red
                if (pong <= 149) {
                    g = 255;
                    b = 33;
                } else if (pong < 300) {
                    r = 243;
                    g = 255;
                } else if (pong < 600) {
                    r = 255;
                    g = 165;
                } else if (pong < 1000) {
                    r = 255;
                    g = 67;
                } else {
                    r = 145;
                    b = 3;
                }
                str = str.concat("ms");
                break;
            }
            case "gamemode" : {
                //thanks gpt for the color ideas
                switch (str) {
                    case "C": g = 255; b = 255; break;
                    case "S": g = 255; break;
                    case "A": r = 255; g = 215; break;
                    case "SP": r = 128; b = 128; break;
                }

                str = "GM: ".concat(str);
                break;
            }
        }
        TextColor txtcolor;
        if (altRGB != 0) {
            txtcolor = TextColor.fromRgb(altRGB);
        } else {
            int rgb = ((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff);
            txtcolor = TextColor.fromRgb(rgb);
        }
        Style style = Style.EMPTY;
        style = style.withColor(txtcolor);
        return Text.literal(str).fillStyle(style);
    }

    private Pair<Integer, Integer> hpRatio(LivingEntity ent) {
        float divide = ent.getHealth() / ent.getMaxHealth();
        int red = Math.round(255 * divide);
        int green = 255 - Math.round(255 * divide);
        return new Pair<>(red, green);
    }

    private Pair<Integer, Integer> htRatio(LivingEntity ent) {
        double divide = (double) ent.hurtTime / (double) ent.maxHurtTime;
        int blue = (int) (255 - Math.round(255 * divide));
        int green = (int) (255 - Math.round(255 * divide));
        return new Pair<>(blue, green);
    }
    private void renderLabelIfPresent(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        double d = mc.getEntityRenderDispatcher().getSquaredDistanceToCamera(entity);
        float scale = (float) (Math.sqrt(d) / (200.0 + (size.value * 50)));
        float f = /*entity.getNameLabelHeight()*/entity.getHeight();
        matrices.push();
        matrices.translate(0.0f, f, 0.0f);
        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
        matrices.scale(-scale, -scale, scale);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int j = (int)(g * 255.0f) << 24;
        TextRenderer textRenderer = mc.textRenderer;
        float h = (float) -textRenderer.getWidth(text) / 2;
        textRenderer.draw(text, h, 0, 0x20FFFFFF, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, j, 16);
        textRenderer.draw(text, h, 0, -1, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 16);
        matrices.pop();
    }
    private Pair<String, String> pair(String left, String right) {
        return new Pair<>(left, right);
    }
}
