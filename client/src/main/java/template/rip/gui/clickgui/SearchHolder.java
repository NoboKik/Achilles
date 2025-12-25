package template.rip.gui.clickgui;

public class SearchHolder {

    private String[] value;
    private int count;

    public SearchHolder() {
        value = new String[]{};
        count = 0;
    }

    public int indexOf(String[] str) {
        return indexOfStrInValue(value, str);
    }

    public int indexOfSelfIn(String[] str) {
        return indexOfStrInValue(str, value);
    }

    public static int indexOfStrInValue(String[] value, String[] str) {
        if (str.length == 0) {
            return 0;
        }
        if (value.length == 0) {
            return -1;
        }
        int countForVal = countForStringArray(value);
        if (str.length == 1) {
            char[] cs = str[0].toCharArray();
            if (cs.length == 1) {
                char c = cs[0];
                for (int i = 0; i < countForVal; i++) {
                    if (charAt(value, i, true) == c) {
                        return i;
                    }
                }
                return -1;
            }
        }
        return indexOf(value, countForVal - 1, str, countForStringArray(str) - 1);
    }

    private static int countForStringArray(String[] str) {
        int i = 0;
        for (String s : str) {
            i += s.length();
        }
        return i;
    }

    private static int indexOf(String[] value, int valueCount, String[] str, int strCount) {
        char first = charAt(str, 0, true);
        int max = (valueCount - strCount);
        for (int i = 0; i <= max; i++) {
            // Look for first character.
            if (charAt(value, i, true) != first) {
                while (++i <= max && charAt(value, i, true) != first);
            }
            // Found first character, now look at the rest of value
            if (i <= max) {
                int j = i + 1;
                int end = j + strCount - 1;
                for (int k = 1; j < end && charAt(value, j, true) == charAt(str, k, true); j++, k++);
                if (j == end) {
                    // Found whole string.
                    return i;
                }
            }
        }
        return -1;
    }

    public char charAt(int index) {
        if (count < index || index < 0) {
            throw new IndexOutOfBoundsException(index);
        }
        int arrIndex = Math.max((int) Math.ceil((index + 1.0) / 3.0) - 1, 0);
        int strIndex = index - (arrIndex * 3);
        try {
            return value[arrIndex].charAt(strIndex);
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException(
                    "Out of bounds for: " + value[arrIndex]
                            + " index: " + index
                            + " strIndex: " + strIndex
                            + " arrIndex: " + arrIndex);
        }
    }

    private static char charAt(String[] from, int index, boolean toLowerCase) {
        // calculating count just for this check is probably too expensive
        if (index < 0) {
            throw new IndexOutOfBoundsException(index);
        }
        int arrIndex = Math.max((int) Math.ceil((index + 1.0) / 3.0) - 1, 0);
        int strIndex = index - (arrIndex * 3);
        try {
            if (toLowerCase) {
                return Character.toLowerCase(from[arrIndex].charAt(strIndex));
            } else {
                return from[arrIndex].charAt(strIndex);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException(
                    "Out of bounds for: " + from[arrIndex]
                            + " index: " + index
                            + " strIndex: " + strIndex
                            + " arrIndex: " + arrIndex);
        }
    }

    public int length() {
        int len = 0;
        for (String str : value) {
            len += str.length();
        }
        return len;
    }

    public boolean isEmpty() {
        return value.length == 0;
    }

    public String[] get() {
        String[] v = new String[value.length];
        System.arraycopy(value, 0, v, 0, value.length);
        return v;
    }

    public void push(char... chars) {
        for (char c : chars) {
            push(c);
        }
    }

    private void push(char c) {
        count++;
        int arrIndex = Math.max((int) Math.ceil(count / 3.0) - 1, 0);
        int strIndex = count == 0 ? 0 : (count % 3 == 0 ? 2 : (count % 3) - 1);

        if (value.length - 1 < arrIndex) {
            String[] current = value;
            value = new String[arrIndex + 1];
            System.arraycopy(current, 0, value, 0, current.length);
        }

        String arrString = strIndex == 0 ? "" : value[arrIndex];
        char[] chars = new char[strIndex + 1];
        System.arraycopy(arrString.toCharArray(), 0, chars, 0, strIndex);
        chars[strIndex] = c;
        value[arrIndex] = new String(chars);
    }

    public char[] pop(int amount) {
        char[] chars = new char[amount];
        for (int i = 0; i < amount; i++) {
            Character character = pop();
            if (character == null) {
                char[] c = new char[i];
                System.arraycopy(chars, 0, c, 0, i);
                return c;
            }
            chars[i] = character;
        }
        return chars;
    }

    public Character pop() {
        if (count == 0) {
            return null;
        }

        int arrIndex = Math.max((int) Math.ceil(count / 3.0) - 1, 0);
        int strIndex = count % 3 == 0 ? 2 : count % 3 - 1;

        String arrString = value[arrIndex];
        char c = arrString.charAt(strIndex);
        char[] chars = new char[strIndex];
        System.arraycopy(arrString.toCharArray(), 0, chars, 0, strIndex);
        value[arrIndex] = new String(chars);

        count--;

        arrIndex = Math.max((int) Math.ceil(count / 3.0) - 1, 0);
        if (arrIndex < value.length - 1) {
            String[] current = value;
            value = new String[arrIndex + 1];
            System.arraycopy(current, 0, value, 0, current.length - 1);
        }

        if (count == 0) {
            value = new String[0];
        }

        return c;
    }
}
