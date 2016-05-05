/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.jface.viewers.Stylers.mixingStyler;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;

class CodeArgumentLabelProvider extends MatchesHighlightingLabelProvider {

    private final int index;

    CodeArgumentLabelProvider(final Supplier<HeaderFilterMatchesCollection> matchesProvider, final int index) {
        super(matchesProvider);
        this.index = index;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        StyledString label = null;
        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition def = (RobotKeywordDefinition) element;
            final List<String> arguments = getKeywordDefinitionArguments(def);
            if (index < arguments.size()) {
                final Styler variableStyler = mixingStyler(
                        withForeground(RedTheme.getVariableColor().getRGB()),
                        withFontStyle(SWT.BOLD));
                label = new StyledString(arguments.get(index), variableStyler);
            }
        } else if (element instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element;
            final List<String> arguments = call.getArguments();
            if (index < arguments.size()) {
                final String argument = arguments.get(index);

                final StyledString variablesLabel = new StyledString(argument);

                final List<Range<Integer>> variablesPositions = RobotExpressions.getVariablesPositions(argument);
                if (!variablesPositions.isEmpty()) {
                    final Styler variableStyler = withForeground(RedTheme.getVariableColor());
                    for (final Range<Integer> range : variablesPositions) {
                        variablesLabel.setStyle(range.lowerEndpoint(), range.upperEndpoint() - range.lowerEndpoint() + 1,
                                variableStyler);
                    }
                }
                label = variablesLabel;
            }
        }
        return highlightMatches(label);
    }

    private List<String> getKeywordDefinitionArguments(final RobotKeywordDefinition def) {
        if (def.hasArguments()) {
            final RobotDefinitionSetting argumentsSetting = def.getArgumentsSetting();
            return argumentsSetting.getArguments();
        }
        return newArrayList();
    }

}
