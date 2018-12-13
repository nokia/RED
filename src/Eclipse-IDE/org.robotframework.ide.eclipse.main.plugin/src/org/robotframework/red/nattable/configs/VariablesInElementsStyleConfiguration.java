/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * @author lwlodarc
 */
public class VariablesInElementsStyleConfiguration extends RobotElementsStyleConfiguration {

    public VariablesInElementsStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    VariablesInElementsStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    String getConfigLabel() {
        return VariablesInElementsLabelAccumulator.POSSIBLE_VARIABLES_IN_ELEMENTS_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        final Style style = new Style();
        final Styler variableStyler = createStyler(SyntaxHighlightingCategory.VARIABLE);
        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                findVariables(variableStyler, label -> createVariableExtractor().extract(label).getMappedElements()));
        return style;
    }

    VariableExtractor createVariableExtractor() {
        return new VariableExtractor();
    }

    private static Function<String, RangeMap<Integer, Styler>> findVariables(final Styler variableStyler,
            final Function<String, List<IElementDeclaration>> variableExtractor) {
        return label -> {
            final RangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            for (final IElementDeclaration declaration : variableExtractor.apply(label)) {
                if (declaration.isComplex()) {
                    mapping.put(Range.closedOpen(declaration.getStart().getStart() - 1,
                            declaration.getEnd().getStart() + 1), variableStyler);
                }
            }
            return mapping;
        };
    }
}
