package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;

public class UserKeywordArgumentLabelProvider extends StylersDisposingLabelProvider {

    private final int index;

    public UserKeywordArgumentLabelProvider(final int index) {
        this.index = index;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition def = (RobotKeywordDefinition) element;
            final List<String> arguments = def.getArguments();
            if (index < arguments.size()) {
                return new StyledString(arguments.get(index), new Styler() {
                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.foreground = RobotTheme.getVariableColor();
                    }
                });
            }
        }
        return new StyledString();
    }

}
