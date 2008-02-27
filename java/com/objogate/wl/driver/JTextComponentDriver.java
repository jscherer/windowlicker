package com.objogate.wl.driver;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.equalTo;
import com.objogate.exception.Defect;
import com.objogate.wl.ComponentManipulation;
import com.objogate.wl.ComponentQuery;
import com.objogate.wl.ComponentSelector;
import com.objogate.wl.Prober;
import com.objogate.wl.gesture.GesturePerformer;
import com.objogate.wl.gesture.Gestures;
import static com.objogate.wl.gesture.Gestures.*;
import com.objogate.wl.internal.query.TextQuery;

public class JTextComponentDriver<T extends JTextComponent> extends ComponentDriver<T> implements TextQuery {

    public JTextComponentDriver(GesturePerformer gesturePerformer, T component) {
        super(gesturePerformer, component);
    }

    public JTextComponentDriver(GesturePerformer gesturePerformer, T component, Prober prober) {
        super(gesturePerformer, component, prober);
    }

    public JTextComponentDriver(GesturePerformer gesturePerformer, ComponentSelector<T> componentSelector) {
        super(gesturePerformer, componentSelector);
    }

    public JTextComponentDriver(GesturePerformer gesturePerformer, ComponentSelector<T> componentSelector, Prober prober) {
        super(gesturePerformer, componentSelector, prober);
    }

    public JTextComponentDriver(ComponentDriver<? extends Component> parentOrOwner, ComponentSelector<T> componentSelector) {
        super(parentOrOwner, componentSelector);
    }

    public JTextComponentDriver(ComponentDriver<? extends Component> parentOrOwner, Class<T> componentType, Matcher<? super T>... matchers) {
        super(parentOrOwner, componentType, matchers);
    }

    public void text(Matcher<String> textMatcher) {
        has("text", new ComponentQuery<T, String>() {
            public String query(T component) {
                return component.getText();
            }
        }, textMatcher);
    }

    public void hasText(String expectedText) {
        hasText(equalTo(expectedText));
    }

    public void hasText(Matcher<String> matcher) {
        text(matcher);
    }

    public void hasSelectedText(String expected) {
        hasSelectedText(equalTo(expected));
    }

    public void hasSelectedText(Matcher<String> matcher) {
        has("selected text", new ComponentQuery<T, String>() {
            public String query(T component) {
                return component.getSelectedText();
            }
        }, matcher);
    }

    public void selectionStartsAt(int index) {
        has("selected text start", new ComponentQuery<T, Integer>() {
            public Integer query(T component) {
                return component.getSelectionStart();
            }
        }, equalTo(index));
    }

    public void selectionEndsAt(int index) {
        has("selected text end", new ComponentQuery<T, Integer>() {
            public Integer query(T component) {
                return component.getSelectionEnd();
            }
        }, equalTo(index));
    }

    public void replaceAllText(String text) {
        moveMouseToCenter();

        selectAll();

        typeText(text);
        typeText("\n");
    }

    public void typeText(String text) {
        performGesture(type(text));
    }

    public void selectAll() {
        super.selectAll();
    }

    public void cut() {
        super.cut();
    }

    public void copy() {
        super.copy();
    }

    public void paste() {
        super.paste();
    }

    public void replaceText(TextOccurence textOccurence, String replacement) {
        selectText(textOccurence);

        typeText(replacement);
    }

    public void selectText(TextOccurence textOccurence) {
        moveMouseToCenter();
        performGesture(Gestures.leftClickMouse());

        waitUntilGesturesCompleted();

        TextSearch search = new TextSearch(textOccurence);
        perform("Text occurence", search);
        int start = search.getStart();

        moveCaretTo(start);

        int repetitions = search.getEnd() - start;
        performGesture(whileHoldingKey(KeyEvent.VK_SHIFT,
                repeat(repetitions, typeKey(KeyEvent.VK_RIGHT))));

        waitUntilGesturesCompleted();
    }

    public void moveCaretTo(int targetPosition) {

        int currentPosition = getCaretPosition();

        if (currentPosition < targetPosition) {
            performGesture(repeat(targetPosition - currentPosition, typeKey(KeyEvent.VK_RIGHT)));
        } else {
            performGesture(repeat(currentPosition - targetPosition, typeKey(KeyEvent.VK_LEFT)));
        }
    }

    public int getCaretPosition() {
        CarentPositionManipulation positionManipulation = new CarentPositionManipulation();
        perform("caret position", positionManipulation);
        return positionManipulation.getPosition();
    }

    public static TextOccurence occurence(int count) {
        return new TextOccurence(count);
    }

    protected static class TextSearch implements ComponentManipulation<JTextComponent> {
        private final int occurence;
        private final String text;
        private int start;
        private int end;

        public TextSearch(JTextFieldDriver.TextOccurence textOccurence) {
            occurence = textOccurence.getCount();
            text = textOccurence.getText();
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public void manipulate(JTextComponent component) {
            String s = component.getText();
            int lastIndex = 0;
            for (int i = 0; i < occurence; i++) {
                int index = s.indexOf(text, lastIndex);
                if(i == occurence -1)
                    lastIndex = index;
                else
                    lastIndex = index + text.length();
            }

            this.start = lastIndex;
            this.end = start + text.length();
        }
    }

    public static class TextOccurence {
        private final int count;
        private String text;

        public TextOccurence(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public String getText() {
            return text;
        }

        public JTextFieldDriver.TextOccurence of(final String text) {
            this.text = text;
            return this;
        }
    }

    protected static class TextWidthManipulation implements ComponentManipulation<JTextComponent> {
        private int width;
        private final int length;
        private final int startIndex;

        public TextWidthManipulation(int startIndex, int length) {
            this.startIndex = startIndex;
            this.length = length;
        }

        public void manipulate(JTextComponent component) {
            try {
                String text = component.getText(startIndex, length);
                Font font = component.getFont();
                width = component.getFontMetrics(font).stringWidth(text);
            } catch (BadLocationException e) {
                throw new Defect("Cannot get text width", e);
            }
        }

        public int getWidth() {
            return width;
        }
    }

    private static class CarentPositionManipulation implements ComponentManipulation<JTextComponent> {
        private int position;

        public void manipulate(JTextComponent component) {
            position = component.getCaretPosition();
        }

        public int getPosition() {
            return position;
        }
    }
}