/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.function.Function;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.NonEnvironmentDeclarationMapper;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
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

    public ActionNamesStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    ActionNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        super(theme, preferences);
    }

    @Override
    String getConfigLabel() {
        return ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL;
    }

    @Override
    Style createElementStyle() {
        final Style style = createStyle(SyntaxHighlightingCategory.KEYWORD_CALL);
        final Styler gherkinStyler = createStyler(SyntaxHighlightingCategory.GHERKIN);
        final Styler libraryStyler = createStyler(SyntaxHighlightingCategory.KEYWORD_CALL_LIBRARY);
        final Styler quoteStyler = createStyler(SyntaxHighlightingCategory.KEYWORD_CALL_QUOTE);
        final Styler variableStyler = createStyler(SyntaxHighlightingCategory.VARIABLE);
        style.setAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES,
                findStyleRanges(gherkinStyler, libraryStyler, quoteStyler, variableStyler, createVariableExtractor()));
        return style;
    }

    private VariableExtractor createVariableExtractor() {
        return new VariableExtractor(new NonEnvironmentDeclarationMapper());
    }

    private static Function<String, RangeMap<Integer, Styler>> findStyleRanges(final Styler gherkinStyler,
            final Styler libraryStyler, final Styler quoteStyler, final Styler variableStyler,
            final VariableExtractor variableExtractor) {
        return label -> {
            final RangeMap<Integer, Styler> mapping = TreeRangeMap.create();

            final Range<Integer> gherkinRange = Range.closedOpen(0,
                    label.length() - GherkinStyleSupport.getTextAfterGherkinPrefixesIfExists(label).length());
            if (!gherkinRange.isEmpty()) {
                mapping.put(gherkinRange, gherkinStyler);
            }

            for (final IElementDeclaration declaration : variableExtractor.extract(label).getMappedElements()) {
                if (declaration.isComplex()) {
                    mapping.put(Range.closedOpen(declaration.getStart().getStart() - 1,
                            declaration.getEnd().getStart() + 1), variableStyler);
                } else {
                    final String textToEvaluate = declaration.getText();
                    int offset = gherkinRange.upperEndpoint();

                    if (declaration.getStart().getStart() == 0) {
                        int dotIndex = textToEvaluate.indexOf('.', offset);
                        while (dotIndex > 0) {
                            final int quoteOpeningIndex = textToEvaluate.indexOf('"', offset);
                            if (quoteOpeningIndex == -1 || dotIndex < quoteOpeningIndex) {
                                mapping.put(Range.closedOpen(offset, dotIndex + 1), libraryStyler);
                                offset = dotIndex + 1;
                                dotIndex = textToEvaluate.indexOf('.', offset);
                                continue;
                            }
                            break;
                        }
                    }

                    while (offset < textToEvaluate.length()) {
                        final int quoteOpenIndex = textToEvaluate.indexOf('"', offset);
                        if (quoteOpenIndex != -1) {
                            final int quoteCloseIndex = textToEvaluate.indexOf('"', quoteOpenIndex + 1);
                            if (quoteOpenIndex < quoteCloseIndex) {
                                mapping.put(Range.closedOpen(quoteOpenIndex + declaration.getStart().getStart(),
                                        quoteCloseIndex + declaration.getStart().getStart() + 1), quoteStyler);
                                offset = quoteCloseIndex + 1;
                                continue;
                            }
                        }
                        break;
                    }
                }
            }

            return mapping;
        };
    }
}
