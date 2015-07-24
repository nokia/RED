package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.jface.viewers.Stylers.mixStylers;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.SWT;
import org.robotframework.ide.eclipse.main.plugin.RobotExpressions;
import org.robotframework.ide.eclipse.main.plugin.RobotTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;

import com.google.common.collect.Range;

class CodeArgumentLabelProvider extends StylersDisposingLabelProvider {

    private final int index;

    CodeArgumentLabelProvider(final int index) {
        this.index = index;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition def = (RobotKeywordDefinition) element;
            final List<String> arguments = getKeywordDefinitionArguments(def);
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

    private List<String> getKeywordDefinitionArguments(final RobotKeywordDefinition def) {
        if (def.hasArguments()) {
            final RobotDefinitionSetting argumentsSetting = def.getArgumentsSetting();
            return argumentsSetting.getArguments();
        }
        return newArrayList();
    }

}
