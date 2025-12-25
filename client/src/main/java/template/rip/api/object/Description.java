package template.rip.api.object;

import template.rip.api.util.MathUtils;

public class Description {

    private final String[] description;

    private Description(String... description) {
        this.description = description;
    }

    public static Description of(String... description) {
        if (description.length == 1) {
            description = MathUtils.split(description[0]);
        }
        return new Description(description);
    }

    public boolean isEmpty() {
        return description.length == 0;
    }

    public String[] getContent() {
        String[] copy = new String[description.length];
        System.arraycopy(description, 0, copy, 0, description.length);
        return copy;
    }
}
