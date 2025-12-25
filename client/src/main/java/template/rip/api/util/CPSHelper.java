package template.rip.api.util;

import template.rip.api.event.events.MousePressEvent;
import template.rip.api.event.orbit.EventHandler;

import java.util.concurrent.CopyOnWriteArrayList;

public class CPSHelper {

    public static final CopyOnWriteArrayList<Long> leftClicks = new CopyOnWriteArrayList<>();
    public static final CopyOnWriteArrayList<Long> rightClicks = new CopyOnWriteArrayList<>();

    @EventHandler
    public void onClick(MousePressEvent event) {
        long time = System.currentTimeMillis();

        if (event.action == 1) {
            if (event.button == 0) leftClicks.add(time);
            else if (event.button == 1) rightClicks.add(time);
        }

        removeOldClicks(time);
    }

    public int getCPS(int mouseButton) {
        removeOldClicks(System.currentTimeMillis());
        return mouseButton == 0 ? leftClicks.size() : rightClicks.size();
    }

    public void removeOldClicks(long currentTime) {
        leftClicks.removeIf(e -> e + 1000 < currentTime);
        rightClicks.removeIf(e -> e + 1000 < currentTime);
    }
}