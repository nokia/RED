/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.JoinedTextDeclarations;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.MappingResult;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VariableExtractorTest {

    @Test
    public void test_extractionOf_escapedQuotaTest_forSPACE_PIPE_SPACE_GREP_ESCAPE_part_commentOfComment() {
        // prepare
        final String text = "   \\\\${result}   Run \"echo \"\"${result}\"\" | grep \\\"\"PLMN-PLMN\\\"\" | awk -F '/' '{print \\$NF}' | awk '{print \\$1}'\"";
        final VariableExtractor extractor = new VariableExtractor();

        final RobotToken varToken = new RobotToken();
        varToken.setLineNumber(3);
        varToken.setStartColumn(1);
        varToken.setStartOffset(39);
        varToken.setText(text);

        final MappedResultStepHelper checkpoint = new MappedResultStepHelper(39).addNextTextStep("   ")
                .addNextTextStep("\\\\")
                .addNextVariableStep("${result}")
                .addNextTextStep("   Run \"echo \"\"")
                .addNextVariableStep("${result}")
                .addNextTextStep("\"\" | grep ")
                .addNextTextStep("\\")
                .addNextTextStep("\"\"PLMN-PLMN")
                .addNextTextStep("\\")
                .addNextTextStep("\"\" | awk -F '/' '")
                .addNextTextStep("{")
                .addNextTextStep("print")
                .addNextTextStep(" ")
                .addNextTextStep("\\")
                .addNextTextStep("$")
                .addNextTextStep("NF")
                .addNextTextStep("}")
                .addNextTextStep("' | awk '")
                .addNextTextStep("{")
                .addNextTextStep("print")
                .addNextTextStep(" ")
                .addNextTextStep("\\")
                .addNextTextStep("$")
                .addNextTextStep("1")
                .addNextTextStep("}")
                .addNextTextStep("'\"");

        // execute
        final MappingResult mapResult = extractor.extract(varToken, null);

        // verify
        checkpoint.assertMappingResult(mapResult);
    }

    @Test
    public void test_extractionOf_escapedQuotaTest_forSPACE_PIPE_SPACE_GREP_ESCAPE_part_onlyOneVariable() {
        // prepare
        final String text = "   \\${result}   Run \"echo \"\"${result}\"\" | grep \\\"\"PLMN-PLMN\\\"\" | awk -F '/' '{print \\$NF}' | awk '{print \\$1}'\"";
        final VariableExtractor extractor = new VariableExtractor();

        final RobotToken varToken = new RobotToken();
        varToken.setLineNumber(3);
        varToken.setStartColumn(1);
        varToken.setStartOffset(39);
        varToken.setText(text);

        final MappedResultStepHelper checkpoint = new MappedResultStepHelper(39).addNextTextStep("   ")
                .addNextTextStep("\\${")
                .addNextTextStep("result")
                .addNextTextStep("}")
                .addNextTextStep("   Run \"echo \"\"")
                .addNextVariableStep("${result}")
                .addNextTextStep("\"\" | grep ")
                .addNextTextStep("\\")
                .addNextTextStep("\"\"PLMN-PLMN")
                .addNextTextStep("\\")
                .addNextTextStep("\"\" | awk -F '/' '")
                .addNextTextStep("{")
                .addNextTextStep("print")
                .addNextTextStep(" ")
                .addNextTextStep("\\")
                .addNextTextStep("$")
                .addNextTextStep("NF")
                .addNextTextStep("}")
                .addNextTextStep("' | awk '")
                .addNextTextStep("{")
                .addNextTextStep("print")
                .addNextTextStep(" ")
                .addNextTextStep("\\")
                .addNextTextStep("$")
                .addNextTextStep("1")
                .addNextTextStep("}")
                .addNextTextStep("'\"");

        // execute
        final MappingResult mapResult = extractor.extract(varToken, null);

        // verify
        checkpoint.assertMappingResult(mapResult);
    }

    @Test
    public void test_extractionOf_escapedQuotaTest_forSPACE_PIPE_SPACE_GREP_ESCAPE_part() {
        // prepare
        final String text = "   ${result}   Run \"echo \"\"${result}\"\" | grep \\\"\"PLMN-PLMN\\\"\" | awk -F '/' '{print \\$NF}' | awk '{print \\$1}'\"";
        final VariableExtractor extractor = new VariableExtractor();

        final RobotToken varToken = new RobotToken();
        varToken.setLineNumber(3);
        varToken.setStartColumn(1);
        varToken.setStartOffset(39);
        varToken.setText(text);

        final MappedResultStepHelper checkpoint = new MappedResultStepHelper(39).addNextTextStep("   ")
                .addNextVariableStep("${result}")
                .addNextTextStep("   Run \"echo \"\"")
                .addNextVariableStep("${result}")
                .addNextTextStep("\"\" | grep ")
                .addNextTextStep("\\")
                .addNextTextStep("\"\"PLMN-PLMN")
                .addNextTextStep("\\")
                .addNextTextStep("\"\" | awk -F '/' '")
                .addNextTextStep("{")
                .addNextTextStep("print")
                .addNextTextStep(" ")
                .addNextTextStep("\\")
                .addNextTextStep("$")
                .addNextTextStep("NF")
                .addNextTextStep("}")
                .addNextTextStep("' | awk '")
                .addNextTextStep("{")
                .addNextTextStep("print")
                .addNextTextStep(" ")
                .addNextTextStep("\\")
                .addNextTextStep("$")
                .addNextTextStep("1")
                .addNextTextStep("}")
                .addNextTextStep("'\"");

        // execute
        final MappingResult mapResult = extractor.extract(varToken, null);

        // verify
        checkpoint.assertMappingResult(mapResult);
    }

    private static class MappedResultStepHelper {

        private final List<Step> steps = new ArrayList<>(0);

        private final int offset;

        public MappedResultStepHelper(final int offset) {
            this.offset = offset;
        }

        private static class Step {

            private final Type type;

            private final String text;

            private Step(final Type type, final String text) {
                this.type = type;
                this.text = text;
            }
        }

        public void assertMappingResult(final MappingResult mapResult) {
            final List<IElementDeclaration> mappedElements = mapResult.getMappedElements();

            int offsetCounter = offset;
            final int size = steps.size();
            for (int i = 0; i < size; i++) {
                final Step step = steps.get(i);
                assertThat(step.type.isCorrectType(mappedElements.get(i))).isTrue();
                assertThat(step.type.isTheSameText(mappedElements.get(i), step.text)).isTrue();
                assertThat(step.type.isTheSameOffset(offsetCounter, mappedElements.get(i))).isTrue();
                offsetCounter += step.text.length();
            }
        }

        private enum Type implements IElementValidator {
            JOINED_TEXT {

                @Override
                public boolean isCorrectType(final IElementDeclaration e) {
                    return (e instanceof JoinedTextDeclarations);
                }

                @Override
                public boolean isTheSameText(final IElementDeclaration e, final String text) {
                    return (((JoinedTextDeclarations) e).getText()).equals(text);
                }

            },
            VARIABLE {

                @Override
                public boolean isCorrectType(final IElementDeclaration e) {
                    return (e instanceof VariableDeclaration);
                }

                @Override
                public boolean isTheSameText(final IElementDeclaration e, final String text) {
                    return ((VariableDeclaration) e).asToken().getText().equals(text);
                }
            };

            @Override
            public boolean isTheSameOffset(final int offset, final IElementDeclaration e) {
                return (offset == e.getStartFromFile().getOffset());
            }
        }

        private interface IElementValidator {

            boolean isCorrectType(final IElementDeclaration e);

            boolean isTheSameText(final IElementDeclaration e, final String text);

            boolean isTheSameOffset(final int offset, final IElementDeclaration e);
        }

        public MappedResultStepHelper addNextVariableStep(final String text) {
            this.steps.add(new Step(Type.VARIABLE, text));
            return this;
        }

        public MappedResultStepHelper addNextTextStep(final String text) {
            this.steps.add(new Step(Type.JOINED_TEXT, text));
            return this;
        }
    }

    @Test
    public void test_extractionOf_EnvironmentVariable_ONLY() {
        // prepare
        final VariableExtractor extractor = new VariableExtractor();

        final RobotToken varToken = new RobotToken();
        varToken.setLineNumber(0);
        varToken.setStartColumn(1);
        varToken.setStartOffset(2);
        varToken.setText("%{user.home}");

        // execute
        final MappingResult mapResult = extractor.extract(varToken, "myFile.robot");

        // verify
        assertThat(mapResult.getMessages()).isEmpty();
        assertThat(mapResult.getFilename()).isEqualTo("myFile.robot");

        assertThat(mapResult.getMappedElements()).hasSize(1);
        assertThat(mapResult.getTextElements()).isEmpty();
        assertThat(mapResult.getCorrectVariables()).hasSize(1);
        final VariableDeclaration variableDeclaration = mapResult.getCorrectVariables().get(0);
        assertThat(variableDeclaration.getRobotType()).isEqualTo(VariableType.ENVIRONMENT);
        assertThat(variableDeclaration.getVariableText().getText()).isEqualTo("%{user.home}");
        assertThat(variableDeclaration.getVariableName().getText()).isEqualTo("user.home");
        final RobotToken asToken = variableDeclaration.asToken();
        assertThat(asToken.getStartColumn()).isEqualTo(1);
        assertThat(asToken.getEndColumn()).isEqualTo("%{user.home}".length() + 1);
    }

    @Test
    public void test_extractionOf_ScalarVariable_ONLY_withoutPositionSet() {
        // prepare
        final VariableExtractor extractor = new VariableExtractor();

        final RobotToken varToken = new RobotToken();
        varToken.setText("${user}");

        // execute
        final MappingResult mapResult = extractor.extract(varToken, "myFile.robot");

        // verify
        assertThat(mapResult.getMessages()).isEmpty();
        assertThat(mapResult.getFilename()).isEqualTo("myFile.robot");

        assertThat(mapResult.getMappedElements()).hasSize(1);
        assertThat(mapResult.getTextElements()).isEmpty();
        assertThat(mapResult.getCorrectVariables()).hasSize(1);
        final VariableDeclaration variableDeclaration = mapResult.getCorrectVariables().get(0);
        assertThat(variableDeclaration.getRobotType()).isEqualTo(VariableType.SCALAR);
        assertThat(variableDeclaration.getVariableText().getText()).isEqualTo("${user}");
        assertThat(variableDeclaration.getVariableName().getText()).isEqualTo("user");
        final RobotToken asToken = variableDeclaration.asToken();
        assertThat(asToken.getStartColumn()).isEqualTo(-1);
        assertThat(asToken.getEndColumn()).isEqualTo(-1);
    }
}
