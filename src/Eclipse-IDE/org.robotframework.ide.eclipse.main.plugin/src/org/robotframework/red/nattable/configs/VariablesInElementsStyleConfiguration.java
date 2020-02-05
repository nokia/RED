/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.descs.ExpressionVisitor;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
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

    private final Supplier<RobotVersion> versionSupplier;

    public VariablesInElementsStyleConfiguration(final TableTheme theme, final Supplier<RobotVersion> versionSupplier) {
        this(theme, RedPlugin.getDefault().getPreferences(), versionSupplier);
    }

    @VisibleForTesting
    VariablesInElementsStyleConfiguration(final TableTheme theme, final RedPreferences preferences,
            final Supplier<RobotVersion> versionSupplier) {
        super(theme, preferences);
        this.versionSupplier = versionSupplier;
    }

    @Override
    String getConfigLabel() {
        return VariablesInElementsLabelAccumulator.POSSIBLE_VARIABLES_IN_ELEMENTS_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        final Style style = new Style();
        final Styler variableStyler = createStyler(SyntaxHighlightingCategory.VARIABLE);

        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES, findVariables(variableStyler,
                VariablesAnalyzer.analyzer(versionSupplier.get(), getAllowedVariableMarks())));
        return style;
    }

    protected String getAllowedVariableMarks() {
        return VariablesAnalyzer.ALL;
    }

    private static Function<String, RangeMap<Integer, Styler>> findVariables(final Styler variableStyler,
            final VariablesAnalyzer variablesAnalyzer) {
        return label -> {
            final RangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            variablesAnalyzer.visitExpression(label, new ExpressionVisitor() {

                @Override
                public boolean visit(final VariableUse usage) {
                    final FileRegion region = usage.getRegion();
                    mapping.put(Range.closedOpen(region.getStart().getOffset(), region.getEnd().getOffset()),
                            variableStyler);
                    return true;
                }
            });
            return mapping;
        };
    }
}
