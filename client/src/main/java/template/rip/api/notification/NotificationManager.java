package template.rip.api.notification;

import net.minecraft.util.math.Vec2f;
import template.rip.gui.utils.Renderable;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static template.rip.Template.mc;

public class NotificationManager implements Renderable {

    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();
    private static final ThreadPoolExecutor tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);

    public void addNotification(Notification notification) {
        this.notifications.add(notification);
    }

    @Override
    public String getName() {
        return "NotificationManager";
    }

    @Override
    public void render() {
        if (notifications.isEmpty()) {
            return;
        }

        tpe.execute(() -> notifications.removeIf(notification -> notification.getTimeProgress() <= 0f));

        Vec2f windowPos = new Vec2f(mc.getWindow().getWidth() - 310, mc.getWindow().getHeight() - 110);
        for (Notification notification : notifications) {
            if (notification.getWindowY() == 0f) notification.setWindowY(windowPos.y);
            notification.render(windowPos.x);
            windowPos = new Vec2f(windowPos.x, windowPos.y - 110);
        }
    }
}
