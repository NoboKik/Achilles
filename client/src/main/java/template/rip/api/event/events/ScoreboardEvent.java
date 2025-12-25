package template.rip.api.event.events;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.ScoreboardObjective;
import template.rip.api.event.Cancellable;

public class ScoreboardEvent extends Cancellable {

	public DrawContext context;
	public ScoreboardObjective objective;

	public ScoreboardEvent(DrawContext context, ScoreboardObjective objective) {
		this.context = context;
		this.objective = objective;
	}
}
