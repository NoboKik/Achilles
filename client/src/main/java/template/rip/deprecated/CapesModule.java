package template.rip.deprecated;

import template.rip.api.object.Description;
import template.rip.module.Module;

public class CapesModule extends Module {
    public CapesModule() {
        super(Category.RENDER, Description.of("Renders custom capes!"), "Capes");
    }

}
