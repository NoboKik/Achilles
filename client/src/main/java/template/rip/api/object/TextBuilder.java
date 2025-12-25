package template.rip.api.object;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextBuilder {

    public MutableText currentText = Text.literal("");

    public TextBuilder() {}

    public void add(Text text) {
        currentText = currentText.append(text);
    }

    public void add(String string) {
        currentText = currentText.append(string);
    }

    public MutableText get() {
        return currentText;
    }
}
