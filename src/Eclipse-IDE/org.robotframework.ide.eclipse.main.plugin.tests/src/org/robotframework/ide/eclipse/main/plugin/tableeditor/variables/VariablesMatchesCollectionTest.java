/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesMatchesCollection.VariableFilter;

import com.google.common.collect.Range;

public class VariablesMatchesCollectionTest {

    @Test
    public void variableNameIsMatched() {
        final RobotVariablesSection varSection = createModelForTest();

        final VariablesMatchesCollection matches = new VariablesMatchesCollection();
        matches.collect(varSection, "scalar1");
        final VariableFilter filter = new VariableFilter(matches);

        assertThat(matches.getNumberOfMatchingElement()).isEqualTo(1);
        assertThat(filter.isMatching(varSection.getChildren().get(0))).isTrue();
        assertThat(filter.isMatching(varSection.getChildren().get(1))).isFalse();

        assertThat(matches.getNumberOfAllMatches()).isEqualTo(1);
        assertThat(matches.contains("${scalar10}")).isTrue();
        assertThat(matches.getRanges("${scalar10}").asRanges()).containsOnly(Range.closedOpen(2, 9));
    }

    @Test
    public void variableValueIsMatched() {
        final RobotVariablesSection varSection = createModelForTest();
        
        final VariablesMatchesCollection matches = new VariablesMatchesCollection();
        matches.collect(varSection, "100");
        final VariableFilter filter = new VariableFilter(matches);

        assertThat(matches.getNumberOfMatchingElement()).isEqualTo(1);
        assertThat(filter.isMatching(varSection.getChildren().get(0))).isTrue();
        assertThat(filter.isMatching(varSection.getChildren().get(1))).isFalse();

        assertThat(matches.getNumberOfAllMatches()).isEqualTo(1);
        assertThat(matches.contains("100")).isTrue();
        assertThat(matches.getRanges("100").asRanges()).containsOnly(Range.closedOpen(0, 3));
    }

    @Test
    public void variableCommentIsMatched() {
        final RobotVariablesSection varSection = createModelForTest();

        final VariablesMatchesCollection matches = new VariablesMatchesCollection();
        matches.collect(varSection, "t0");
        final VariableFilter filter = new VariableFilter(matches);

        assertThat(matches.getNumberOfMatchingElement()).isEqualTo(1);
        assertThat(filter.isMatching(varSection.getChildren().get(0))).isTrue();
        assertThat(filter.isMatching(varSection.getChildren().get(1))).isFalse();

        assertThat(matches.getNumberOfAllMatches()).isEqualTo(1);
        assertThat(matches.contains("#comment0")).isTrue();
        assertThat(matches.getRanges("#comment0").asRanges()).containsOnly(Range.closedOpen(7, 9));
    }

    @Test
    public void mulitplePlacesAreMatchedInSingleVariable() {
        final RobotVariablesSection varSection = createModelForTest();

        final VariablesMatchesCollection matches = new VariablesMatchesCollection();
        matches.collect(varSection, "0");
        final VariableFilter filter = new VariableFilter(matches);

        assertThat(matches.getNumberOfMatchingElement()).isEqualTo(1);
        assertThat(filter.isMatching(varSection.getChildren().get(0))).isTrue();
        assertThat(filter.isMatching(varSection.getChildren().get(1))).isFalse();

        assertThat(matches.getNumberOfAllMatches()).isEqualTo(4);

        assertThat(matches.contains("${scalar10}")).isTrue();
        assertThat(matches.getRanges("${scalar10}").asRanges()).containsOnly(Range.closedOpen(9, 10));
        assertThat(matches.contains("100")).isTrue();
        assertThat(matches.getRanges("100").asRanges()).containsOnly(Range.closedOpen(1, 3));
        assertThat(matches.contains("#comment0")).isTrue();
        assertThat(matches.getRanges("#comment0").asRanges()).containsOnly(Range.closedOpen(8, 9));
    }

    @Test
    public void mulitplePlacesAreMatchedInMultipleVariables() {
        final RobotVariablesSection varSection = createModelForTest();

        final VariablesMatchesCollection matches = new VariablesMatchesCollection();
        matches.collect(varSection, "1");
        final VariableFilter filter = new VariableFilter(matches);

        assertThat(matches.getNumberOfMatchingElement()).isEqualTo(2);
        assertThat(filter.isMatching(varSection.getChildren().get(0))).isTrue();
        assertThat(filter.isMatching(varSection.getChildren().get(1))).isTrue();

        assertThat(matches.getNumberOfAllMatches()).isEqualTo(4);

        assertThat(matches.contains("${scalar10}")).isTrue();
        assertThat(matches.getRanges("${scalar10}").asRanges()).containsOnly(Range.closedOpen(8, 9));
        assertThat(matches.contains("100")).isTrue();
        assertThat(matches.getRanges("100").asRanges()).containsOnly(Range.closedOpen(0, 1));
        assertThat(matches.contains("${scalar21}")).isTrue();
        assertThat(matches.getRanges("${scalar21}").asRanges()).containsOnly(Range.closedOpen(9, 10));
        assertThat(matches.contains("#comment1")).isTrue();
        assertThat(matches.getRanges("#comment1").asRanges()).containsOnly(Range.closedOpen(8, 9));
    }

    private RobotVariablesSection createModelForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("${scalar10}  100  #comment0")
                .appendLine("${scalar21}  222  #comment1")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection;
    }
}
