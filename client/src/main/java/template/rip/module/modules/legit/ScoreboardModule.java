/*package template.rip.module.modules.legit;


import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import template.rip.api.event.events.ScoreboardEvent;
import template.rip.api.event.events.TickEvent;
import template.rip.api.event.orbit.EventHandler;
import template.rip.api.object.Description;
import template.rip.module.Module;
import template.rip.module.setting.settings.BooleanSetting;
import template.rip.module.setting.settings.NumberSetting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ScoreboardModule extends Module {
    public final BooleanSetting hideScoreboard = new BooleanSetting(this, false, "Hide Scoreboard");
    public final BooleanSetting background = new BooleanSetting(this, true, "Background");
    public final BooleanSetting numbers = new BooleanSetting(this, true, "Numbers");
    public final BooleanSetting textShadow = new BooleanSetting(this, false, "Text Shadow");
    public final BooleanSetting left = new BooleanSetting(this, false, "Left");
    public NumberSetting yLevel = new NumberSetting(this, 0, -250, 250, 1, "Y Offset");

    public ScoreboardModule() {
        super(Category.LEGIT, Description.of("Change the scoreboard."), "Scoreboard");
    }


    @EventHandler
    private void onTick(TickEvent.Pre event) {
        yLevel.setMaximum(mc.inGameHud.scaledHeight / 2.0);
        yLevel.setMinimum(-(mc.inGameHud.scaledHeight / 2.0));
    }
    @EventHandler
    private void onScoreboard(ScoreboardEvent event) {
        event.cancel();
        if (hideScoreboard.isEnabled()) {
            return;
        }

        int i;
        Scoreboard scoreboard = event.objective.getScoreboard();
        Collection<ScoreboardPlayerScore> collection = scoreboard.getAllPlayerScores(event.objective);
        List<ScoreboardPlayerScore> list = collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList());
        collection = list.size() > 15 ? Lists.newArrayList(Iterables.skip(list, collection.size() - 15)) : list;
        ArrayList<Pair<ScoreboardPlayerScore, MutableText>> list2 = Lists.newArrayListWithCapacity(collection.size());
        Text text = event.objective.getDisplayName();
        int j = i = mc.textRenderer.getWidth(text);
        int k = mc.textRenderer.getWidth(InGameHud.SCOREBOARD_JOINER);
        for (ScoreboardPlayerScore scoreboardPlayerScore : collection) {
            Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
            MutableText text2 = Team.decorateName(team, Text.literal(scoreboardPlayerScore.getPlayerName()));
            list2.add(Pair.of(scoreboardPlayerScore, text2));
            j = Math.max(j, mc.textRenderer.getWidth(text2) + k + mc.textRenderer.getWidth(numbers.isEnabled() ? Integer.toString(scoreboardPlayerScore.getScore()) : ""));
        }
        int l = collection.size() * mc.textRenderer.fontHeight;
        int m = mc.inGameHud.scaledHeight / 2 + l / 3;
        m += yLevel.getIValue();
        int o = mc.inGameHud.scaledWidth - j - 3;
        if (left.isEnabled())
            o = 3;
        int p = 0;
        int q = background.isEnabled() ? mc.options.getTextBackgroundColor(0.3f) : mc.options.getTextBackgroundColor(0F);
        int r = background.isEnabled() ? mc.options.getTextBackgroundColor(0.4f) : mc.options.getTextBackgroundColor(0F);
        for (Pair<ScoreboardPlayerScore, MutableText> pair : list2) {
            ScoreboardPlayerScore scoreboardPlayerScore2 = pair.getFirst();
            Text text3 = pair.getSecond();
            String string = "" + Formatting.RED + scoreboardPlayerScore2.getScore();
            if (!numbers.isEnabled())
                string = "";
            int t = m - ++p * mc.textRenderer.fontHeight;
            int u = mc.inGameHud.scaledWidth - 3 + 2;
            if (left.isEnabled())
                u = j + 5;
            event.context.fill(o - 2, t, u, t + mc.textRenderer.fontHeight, q);
            if (textShadow.isEnabled()) {
                event.context.drawTextWithShadow(mc.textRenderer, text3, o, t, -1);
                event.context.drawTextWithShadow(mc.textRenderer, string, u - mc.textRenderer.getWidth(string), t, -1);
            } else {
                event.context.drawText(mc.textRenderer, text3, o, t, -1, false);
                event.context.drawText(mc.textRenderer, string, u - mc.textRenderer.getWidth(string), t, -1, false);
            }
            if (p != collection.size()) continue;
            event.context.fill(o - 2, t - mc.textRenderer.fontHeight - 1, u, t - 1, r);
            event.context.fill(o - 2, t - 1, u, t, q);
            event.context.drawText(mc.textRenderer, text, o + j / 2 - i / 2, t - mc.textRenderer.fontHeight, -1, false);
        }

    }

}*/
