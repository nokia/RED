/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.filePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.noFilePositions;
import static org.robotframework.ide.eclipse.main.plugin.model.ModelConditions.nullParent;
import static org.robotframework.ide.eclipse.main.plugin.model.RobotVariableConditions.properlySetParent;

import java.util.List;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;

public class RobotVariableTest {

    @Test
    public void copyBySerializationTest() {
        for (final RobotVariable variable : createVariablesForTest()) {
            assertThat(variable).has(properlySetParent()).has(filePositions());

            final RobotVariable variableCopy = ModelElementsSerDe.copy(variable);
            assertThat(variableCopy).isNotSameAs(variable).has(nullParent()).has(noFilePositions());
            assertThat(variableCopy.getLinkedElement()).isNotSameAs(variable.getLinkedElement());

            assertThat(variableCopy.getName()).isEqualTo(variable.getName());
            assertThat(variableCopy.getValue()).isEqualTo(variable.getValue());
            assertThat(variableCopy.getComment()).isEqualTo(variable.getComment());
        }
    }

    private static List<RobotVariable> createVariablesForTest() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("#comment")
                .appendLine("${s0}")
                .appendLine("${s1}  ")
                .appendLine("${s2}  1")
                .appendLine("${s3}  1  2  3")
                .appendLine("${s4}  # comment 1  comment 2")
                .appendLine("${s5}  1  # comment")
                .appendLine("${s6}  2  3  # comment")
                .appendLine("@{l0}")
                .appendLine("@{l1}  ")
                .appendLine("@{l2}  1")
                .appendLine("@{l3}  1  2  3")
                .appendLine("@{l4}  # comment 1  comment 2")
                .appendLine("@{l5}  1  # comment")
                .appendLine("@{l6}  2  3  # comment")
                .appendLine("&{d0}")
                .appendLine("&{d1}  ")
                .appendLine("&{d2}  a=1")
                .appendLine("&{d3}  a=1  b=2  3")
                .appendLine("&{d4}  # comment 1  comment 2")
                .appendLine("&{d5}  a=1  # comment")
                .appendLine("&{d6}  2  3  # comment")
                .appendLine("u0")
                .appendLine("u1  ")
                .appendLine("u2  1")
                .appendLine("u3  1  2  3")
                .appendLine("u4  # comment 1  comment 2")
                .appendLine("u5  1  # comment")
                .appendLine("u6  2  3  # comment")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren();
    }
}
