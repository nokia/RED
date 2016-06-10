/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal.VariableOrigin;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class RedVariableProposalTest {

    @Test
    public void verifyProposalPropertiesMadeFromBuiltInVariable() {
        final RedVariableProposal proposal = RedVariableProposal.createBuiltIn("${VAR}", "value");

        assertThat(proposal.getName()).isEqualTo("${VAR}");
        assertThat(proposal.getValue()).isEqualTo("value");
        assertThat(proposal.getSource()).isEqualTo("built-in");
        assertThat(proposal.getComment()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
    }

    @Test
    public void verifyProposalPropertiesMadeFromVariableFiles() {
        final RedVariableProposal proposal = RedVariableProposal.create("@{var}", "value", "varfile.py");

        assertThat(proposal.getName()).isEqualTo("@{var}");
        assertThat(proposal.getValue()).isEqualTo("value");
        assertThat(proposal.getSource()).isEqualTo("varfile.py");
        assertThat(proposal.getComment()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotListVariableImage());
    }

    @Test
    public void verifyProposalPropertiesMadeFromLocalDefinition_1() {
        final RedVariableProposal proposal = RedVariableProposal.createLocal("${var}=10", "resfile.robot");

        assertThat(proposal.getName()).isEqualTo("${var}");
        assertThat(proposal.getValue()).isEmpty();
        assertThat(proposal.getSource()).isEqualTo("resfile.robot");
        assertThat(proposal.getComment()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
    }

    @Test
    public void verifyProposalPropertiesMadeFromLocalDefinition_2() {
        final RedVariableProposal proposal = RedVariableProposal.createLocal("&{var}", "resfile.robot");

        assertThat(proposal.getName()).isEqualTo("&{var}");
        assertThat(proposal.getValue()).isEmpty();
        assertThat(proposal.getSource()).isEqualTo("resfile.robot");
        assertThat(proposal.getComment()).isEmpty();
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotDictionaryVariableImage());
    }

    @Test
    public void verifyProposalPropertiesMadeFromModelPart() {
        final RobotVariable variable = createVariable();
        final RedVariableProposal proposal = RedVariableProposal.create(variable);

        assertThat(proposal.getName()).isEqualTo("${scalar}");
        assertThat(proposal.getValue()).isEqualTo("0");
        assertThat(proposal.getSource()).isEqualTo("file.robot");
        assertThat(proposal.getComment()).isEqualTo("#something");
        assertThat(proposal.getImage()).isEqualTo(RedImages.getRobotScalarVariableImage());
    }

    @Test
    public void hashCodeIsMadeFromAllFields() {
        final String name = "name";
        final String source = "source";
        final String value = "value";
        final String comment = "comment";
        final VariableOrigin origin = VariableOrigin.BUILTIN;
        final RedVariableProposal proposal = new RedVariableProposal(name, source, value, comment, origin);

        assertThat(proposal.hashCode()).isEqualTo(Objects.hash(name, source, value, comment, origin));
    }

    @Test
    public void equalityTests() {
        final RedVariableProposal proposal = new RedVariableProposal("name", "source", "value", "comment",
                VariableOrigin.BUILTIN);

        assertThat(proposal
                .equals(new RedVariableProposal("other_name", "source", "value", "comment", VariableOrigin.BUILTIN)))
                        .isFalse();
        assertThat(proposal
                .equals(new RedVariableProposal("name", "other_source", "value", "comment", VariableOrigin.BUILTIN)))
                        .isFalse();
        assertThat(proposal
                .equals(new RedVariableProposal("name", "source", "other_value", "comment", VariableOrigin.BUILTIN)))
                        .isFalse();
        assertThat(proposal
                .equals(new RedVariableProposal("name", "source", "value", "other_comment", VariableOrigin.BUILTIN)))
                        .isFalse();
        assertThat(
                proposal.equals(new RedVariableProposal("name", "source", "value", "comment", VariableOrigin.IMPORTED)))
                        .isFalse();
        assertThat(
                proposal.equals(new RedVariableProposal("name", "source", "value", "comment", VariableOrigin.BUILTIN)))
                        .isTrue();
    }

    private static RobotVariable createVariable() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0  #something")
                .build();
        return model.findSection(RobotVariablesSection.class).get().getChildren().get(0);
    }
}
