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
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.NonEnvironmentDeclarationMapper;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;

/**
 * @author lwlodarc
 */
public class VariablesInNamesStyleConfiguration extends RobotElementsStyleConfiguration {

    public VariablesInNamesStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    VariablesInNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    String getConfigLabel() {
        return VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        final Style style = new Style();
        final Styler variableStyler = createStyler(SyntaxHighlightingCategory.VARIABLE);
        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                findVariables(new VariableExtractor(new NonEnvironmentDeclarationMapper()), variableStyler));
        return style;
    }

    static Function<String, RangeMap<Integer, Styler>> findVariables(final VariableExtractor extractor,
            final Styler variableStyler) {
        return label -> {
            final RangeMap<Integer, Styler> mapping = TreeRangeMap.create();
            for (final Range<Integer> varRange : markVariables(label, extractor).asRanges()) {
                mapping.put(varRange, variableStyler);
            }
            return mapping;
        };
    }

    static RangeSet<Integer> markVariables(final String label, final VariableExtractor extractor) {
        final MappingResult extract = extractor.extract(FilePosition.createNotSet(), label);
        final List<IElementDeclaration> mappedElements = extract.getMappedElements();

        final RangeSet<Integer> variableRanges = TreeRangeSet.create();

        IElementDeclaration variableStart = null;
        for (final IElementDeclaration declaration : mappedElements) {
            if (declaration.isComplex()) {
                if (variableStart == null) {
                    variableStart = declaration;
                }
            } else {
                if (variableStart != null) {
                    variableRanges.add(Range.closedOpen(variableStart.getStart().getStart() - 1,
                            declaration.getStart().getStart()));
                    variableStart = null;
                }
            }
        }

        return variableRanges;
    }
}
