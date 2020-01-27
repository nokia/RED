/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.function.Function;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
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
public class ActionNamesStyleConfiguration extends RobotElementsStyleConfiguration {

    private final SyntaxHighlightingCategory mainCategory;

    public ActionNamesStyleConfiguration(final TableTheme theme) {
        this(theme, RedPlugin.getDefault().getPreferences(), SyntaxHighlightingCategory.KEYWORD_CALL);
    }

    @VisibleForTesting
    ActionNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        this(theme, preferences, SyntaxHighlightingCategory.KEYWORD_CALL);
    }

    @VisibleForTesting
    ActionNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences,
            final SyntaxHighlightingCategory mainCategory) {
        super(theme, preferences);
        this.mainCategory = mainCategory;
    }

    @Override
    String getConfigLabel() {
        return ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        final Style style = createStyle(mainCategory);
        final Styler gherkinStyler = createStyler(SyntaxHighlightingCategory.GHERKIN);
        final Styler libraryStyler = createStyler(SyntaxHighlightingCategory.KEYWORD_CALL_LIBRARY);
        final Styler quoteStyler = createStyler(SyntaxHighlightingCategory.KEYWORD_CALL_QUOTE);
        final Styler variableStyler = createStyler(SyntaxHighlightingCategory.VARIABLE);
        // FIXME : version !!!
        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                findStyleRanges(gherkinStyler, libraryStyler, quoteStyler, variableStyler,
                        VariablesAnalyzer.analyzer(null, VariablesAnalyzer.ALL_ROBOT)));
        return style;
    }

    private static Function<String, RangeMap<Integer, Styler>> findStyleRanges(final Styler gherkinStyler,
            final Styler libraryStyler, final Styler quoteStyler, final Styler variableStyler,
            final VariablesAnalyzer variablesAnalyzer) {
        return label -> {
            final RangeMap<Integer, Styler> mapping = TreeRangeMap.create();

            final Range<Integer> gherkinRange = Range.closedOpen(0,
                    label.length() - GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(label).length());
            if (!gherkinRange.isEmpty()) {
                mapping.put(gherkinRange, gherkinStyler);
            }

            variablesAnalyzer.visitExpression(label, new ExpressionVisitor() {

                @Override
                public boolean visit(final VariableUse usage) {
                    final FileRegion region = usage.getRegion();
                    mapping.put(Range.closedOpen(region.getStart().getOffset(), region.getEnd().getOffset()),
                            variableStyler);
                    return true;
                }

                @Override
                public boolean visit(final String text, final FileRegion region) {
                    int offset = gherkinRange.upperEndpoint();

                    if (region.getStart().getOffset() == 0) {
                        int dotIndex = text.indexOf('.', offset);
                        while (dotIndex > 0) {
                            final int quoteOpeningIndex = text.indexOf('"', offset);
                            if (quoteOpeningIndex == -1 || dotIndex < quoteOpeningIndex) {
                                mapping.put(Range.closedOpen(offset, dotIndex + 1), libraryStyler);
                                offset = dotIndex + 1;
                                dotIndex = text.indexOf('.', offset);
                                continue;
                            }
                            break;
                        }
                    }

                    while (offset < text.length()) {
                        final int quoteOpenIndex = text.indexOf('"', offset);
                        if (quoteOpenIndex != -1) {
                            final int quoteCloseIndex = text.indexOf('"', quoteOpenIndex + 1);
                            if (quoteOpenIndex < quoteCloseIndex) {
                                mapping.put(Range.closedOpen(quoteOpenIndex + region.getStart().getOffset(),
                                        quoteCloseIndex + region.getStart().getOffset() + 1), quoteStyler);
                                offset = quoteCloseIndex + 1;
                                continue;
                            }
                        }
                        break;
                    }
                    return true;
                }
            });
            return mapping;
        };
    }
}
