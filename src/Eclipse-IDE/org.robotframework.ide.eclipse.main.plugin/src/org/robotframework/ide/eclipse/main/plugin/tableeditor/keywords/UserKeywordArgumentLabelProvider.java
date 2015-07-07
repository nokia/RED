package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static org.eclipse.jface.viewers.Stylers.mixStylers;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.SWT;
import org.robotframework.ide.eclipse.main.plugin.RobotExpressions;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;

import com.google.common.collect.Range;

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
                final DisposeNeededStyler variableStyler = addDisposeNeededStyler(mixStylers(withForeground(RobotTheme
                        .getVariableColor().getRGB()), withFontStyle(SWT.BOLD)));
                return new StyledString(arguments.get(index), variableStyler);
            }
        } else if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;
            final List<String> arguments = call.getArguments();
            if (index < arguments.size()) {
                final String argument = arguments.get(index);

                final StyledString label = new StyledString(argument);

                final List<Range<Integer>> variablesPositions = RobotExpressions.getVariablesPositions(argument);
                if (!variablesPositions.isEmpty()) {
                    final DisposeNeededStyler variableStyler = addDisposeNeededStyler(withForeground(RobotTheme
                            .getVariableColor().getRGB()));
                    for (final Range<Integer> range : variablesPositions) {
                        label.setStyle(range.lowerEndpoint(), range.upperEndpoint() - range.lowerEndpoint() + 1,
                                variableStyler);
                    }
                }
                return label;
            }
        }
        return new StyledString();
    }

}
