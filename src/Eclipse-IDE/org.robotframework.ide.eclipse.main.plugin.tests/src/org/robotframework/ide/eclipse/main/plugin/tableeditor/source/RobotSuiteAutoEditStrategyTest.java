/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.DocumentCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotSuiteAutoEditStrategy.EditStrategyPreferences;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

@ExtendWith(ProjectExtension.class)
public class RobotSuiteAutoEditStrategyTest {

    @Project
    static IProject project;

    @Test
    public void separatorIsInserted_whenTabWasOriginallyRequestedInsideCellRegion_andJumpOfModeIsDisabled() {
        final RobotDocument document = newDocument("abc  def");
        final DocumentCommand command = newDocumentCommand(1, "\t");

        final EditStrategyPreferences preferences = newPreferences("the_separator", false);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("the_separator");
        assertThat(command.shiftsCaret).isTrue();
        assertThat(command.caretOffset).isEqualTo(-1);
        assertThat(command.length).isEqualTo(0);
    }

    @Test
    public void separatorIsInserted_whenTabWasOriginallyRequestedInsideVariableRegion_andJumpOfModeIsDisabled() {
        final RobotDocument document = newDocument("a${var}bc  def");
        final DocumentCommand command = newDocumentCommand(4, "\t");

        final EditStrategyPreferences preferences = newPreferences("the_separator", false);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("the_separator");
        assertThat(command.shiftsCaret).isTrue();
        assertThat(command.caretOffset).isEqualTo(-1);
        assertThat(command.length).isEqualTo(0);
    }

    @Test
    public void separatorIsInserted_whenTabWasOriginallyRequestedRightAfterCellRegion_andJumpOfModeIsEnabled() {
        final RobotDocument document = newDocument("abc  def");
        final DocumentCommand command = newDocumentCommand(3, "\t");

        final EditStrategyPreferences preferences = newPreferences("the_separator", true);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("the_separator");
        assertThat(command.shiftsCaret).isTrue();
        assertThat(command.caretOffset).isEqualTo(-1);
        assertThat(command.length).isEqualTo(0);
    }

    @Test
    public void separatorIsInserted_whenTabWasOriginallyRequestedRightBeforeCellRegion_andJumpOfModeIsEnabled() {
        final RobotDocument document = newDocument("abc  def");
        final DocumentCommand command = newDocumentCommand(0, "\t");

        final EditStrategyPreferences preferences = newPreferences("the_separator", true);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("the_separator");
        assertThat(command.shiftsCaret).isTrue();
        assertThat(command.caretOffset).isEqualTo(-1);
        assertThat(command.length).isEqualTo(0);
    }

    @Test
    public void caretIsShifted_whenTabWasOriginallyRequestedInsideCellRegion_andJumpOfModeIsEnabled() {
        final RobotDocument document = newDocument("abc  def");
        final DocumentCommand command = newDocumentCommand(1, "\t");

        final EditStrategyPreferences preferences = newPreferences("the_separator", true);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isNull();
        assertThat(command.shiftsCaret).isFalse();
        assertThat(command.caretOffset).isEqualTo(3);
        assertThat(command.length).isEqualTo(0);
    }

    @Test
    public void caretIsShifted_whenTabWasOriginallyRequestedInsideVariableRegion_andJumpOfModeIsEnabled() {
        final RobotDocument document = newDocument("a${var}bc  def");
        final DocumentCommand command = newDocumentCommand(3, "\t");

        final EditStrategyPreferences preferences = newPreferences("the_separator", true);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isNull();
        assertThat(command.shiftsCaret).isFalse();
        assertThat(command.caretOffset).isEqualTo(7);
        assertThat(command.length).isEqualTo(0);
    }

    @Test
    public void caretIsShifted_whenTabWasOriginallyRequestedRightAfterVariableRegion_andJumpOfModeIsEnabled() {
        final RobotDocument document = newDocument("a${var}bc  def");
        final DocumentCommand command = newDocumentCommand(1, "\t");

        final EditStrategyPreferences preferences = newPreferences("the_separator", true);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isNull();
        assertThat(command.shiftsCaret).isFalse();
        assertThat(command.caretOffset).isEqualTo(9);
        assertThat(command.length).isEqualTo(0);
    }

    @Test
    public void caretIsShifted_whenTabWasOriginallyRequestedRightBeforeVariableRegion_andJumpOfModeIsEnabled() {
        final RobotDocument document = newDocument("a${var}bc  def");
        final DocumentCommand command = newDocumentCommand(7, "\t");

        final EditStrategyPreferences preferences = newPreferences("the_separator", true);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isNull();
        assertThat(command.shiftsCaret).isFalse();
        assertThat(command.caretOffset).isEqualTo(9);
        assertThat(command.length).isEqualTo(0);
    }

    @Test
    public void variableBracketsAreNotAdded_whenVariableIdentifiersAreRequestedAtDocumentEnd_andInsertionModeIsDisabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument("abc");
            final DocumentCommand command = newDocumentCommand(3, varId);

            final EditStrategyPreferences preferences = newPreferences(false);
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo(varId);
            assertThat(command.shiftsCaret).isTrue();
            assertThat(command.caretOffset).isEqualTo(-1);
            assertThat(command.length).isEqualTo(0);
        }
    }

    @Test
    public void variableBracketsAreAdded_whenVariableIdentifiersAreRequestedAtDocumentEnd_andInsertionModeIsEnabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument("abc");
            final DocumentCommand command = newDocumentCommand(3, varId);

            final EditStrategyPreferences preferences = newPreferences(true);
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo(varId + "{}");
            assertThat(command.shiftsCaret).isFalse();
            assertThat(command.caretOffset).isEqualTo(5);
            assertThat(command.length).isEqualTo(0);
        }
    }

    @Test
    public void variableBracketsAreAdded_whenVariableIdentifiersAreRequestedInsideDocument_andInsertionModeIsEnabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument("abc");
            final DocumentCommand command = newDocumentCommand(1, varId);

            final EditStrategyPreferences preferences = newPreferences(true);
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo(varId + "{}");
            assertThat(command.shiftsCaret).isFalse();
            assertThat(command.caretOffset).isEqualTo(3);
            assertThat(command.length).isEqualTo(0);
        }
    }

    @Test
    public void variableBracketsAreNotAdded_whenVariableIdentifiersAreRequestedBeforeOpeningBracket_andInsertionModeIsEnabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument("{name}");
            final DocumentCommand command = newDocumentCommand(varId);

            final EditStrategyPreferences preferences = newPreferences(true);
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo(varId);
            assertThat(command.shiftsCaret).isTrue();
            assertThat(command.caretOffset).isEqualTo(-1);
            assertThat(command.length).isEqualTo(0);
        }
    }

    @Test
    public void bracketsAreNotDeleted_whenBackspaceOrDeleteIsRequestedOnEmptyVariableBrackets_andInsertionModeIsDisabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument(varId + "{}");

            final DocumentCommand command = newDocumentCommand(1, "", 1);

            final EditStrategyPreferences preferences = newPreferences(false);
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEmpty();
            assertThat(command.shiftsCaret).isTrue();
            assertThat(command.caretOffset).isEqualTo(-1);
            assertThat(command.length).isEqualTo(1);
        }
    }

    @Test
    public void bracketsAreDeleted_whenBackspaceOrDeleteIsRequestedOnEmptyVariableBrackets_andInsertionModeIsEnabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument(varId + "{}");

            final DocumentCommand command = newDocumentCommand(1, "", 1);

            final EditStrategyPreferences preferences = newPreferences(true);
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEmpty();
            assertThat(command.shiftsCaret).isTrue();
            assertThat(command.caretOffset).isEqualTo(-1);
            assertThat(command.length).isEqualTo(2);
        }
    }

    @Test
    public void bracketsAreNotDeleted_whenBackspaceOrDeleteIsRequestedOnEmptyNonVariableBrackets_andInsertionModeIsEnabled() {
        final RobotDocument document = newDocument("abc{}");

        final DocumentCommand command = newDocumentCommand(3, "", 1);

        final EditStrategyPreferences preferences = newPreferences(true);
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEmpty();
        assertThat(command.shiftsCaret).isTrue();
        assertThat(command.caretOffset).isEqualTo(-1);
        assertThat(command.length).isEqualTo(1);
    }

    @Test
    public void textIsNotWrappedInVariableBrackets_whenVariableIdentifiersAreRequestedOnSelectedTextContainingOnlyWordCharacters_andWrappingModeIsDisabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument("Abc Def_123");

            final DocumentCommand command = newDocumentCommand(4, varId, 7);

            final EditStrategyPreferences preferences = newPreferences(true);
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo(varId);
            assertThat(command.shiftsCaret).isTrue();
            assertThat(command.caretOffset).isEqualTo(-1);
            assertThat(command.length).isEqualTo(7);
        }
    }

    @Test
    public void textIsWrappedInVariableBrackets_whenVariableIdentifiersAreRequestedOnSelectedTextContainingOnlyWordCharacters_andWrappingModeIsEnabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument("Abc Def_123");

            final DocumentCommand command = newDocumentCommand(4, varId, 7);

            final EditStrategyPreferences preferences = newPreferences(true, true, "\\w+");
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo(varId + "{Def_123}");
            assertThat(command.shiftsCaret).isFalse();
            assertThat(command.caretOffset).isEqualTo(13);
            assertThat(command.length).isEqualTo(7);
        }
    }

    @Test
    public void textIsNotWrappedInVariableBrackets_whenVariableIdentifiersAreRequestedOnSelectedTextContainingNonWordCharacter_andWrappingModeIsEnabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument("Abc Def_123");

            final DocumentCommand command = newDocumentCommand(2, varId, 6);

            final EditStrategyPreferences preferences = newPreferences(true, true, "\\w+");
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo(varId);
            assertThat(command.shiftsCaret).isTrue();
            assertThat(command.caretOffset).isEqualTo(-1);
            assertThat(command.length).isEqualTo(6);
        }
    }

    @Test
    public void textIsWrappedInVariableBrackets_whenVariableIdentifiersAreRequestedOnSelectedTextContainingWordAndVariableCharacters_andWrappingModeIsEnabled() {
        for (final String varId : AVariable.ROBOT_VAR_IDENTIFICATORS) {
            final RobotDocument document = newDocument("Abc Def_123_${var}_456");

            final DocumentCommand command = newDocumentCommand(6, varId, 14);

            final EditStrategyPreferences preferences = newPreferences(true, true, "[\\$@&{}\\w]+");
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo(varId + "{f_123_${var}_4}");
            assertThat(command.shiftsCaret).isFalse();
            assertThat(command.caretOffset).isEqualTo(22);
            assertThat(command.length).isEqualTo(14);
        }
    }

    @Test
    public void commandShouldNotBeChanged_whenItIsNotLineBreak() {
        final RobotDocument document = newDocument("x");
        final DocumentCommand command = newDocumentCommand(1, "q");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("q");
    }

    @Test
    public void indentShouldNotBeAdded_whenPreviousLineDoesNotStartFromIndent() {
        final RobotDocument document = newDocument("xyz");
        final DocumentCommand command = newDocumentCommand(3, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void indentFromPreviousLineShouldBeAdded_1() {
        final RobotDocument document = newDocument("    abc");
        final DocumentCommand command = newDocumentCommand(7, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n    ");
    }

    @Test
    public void indentFromPreviousLineShouldBeAdded_2() {
        final RobotDocument document = newDocument("\tabc");
        final DocumentCommand command = newDocumentCommand(4, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n\t");
    }

    @Test
    public void indentIsAdded_whenMovingToNewLineFromTestDefinitionLine() {
        final RobotDocument document = newDocument("*** Test Cases ***", "test");
        final DocumentCommand command = newDocumentCommand(23, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ");
    }

    @Test
    public void indentIsNotAdded_whenMovingToNewLineFromTestDefinitionLineButWithCaretJustBeforeDefinition() {
        final RobotDocument document = newDocument("*** Test Cases ***", "test");
        final DocumentCommand command = newDocumentCommand(19, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void indentIsAdded_whenMovingToNewLineFromTaskDefinitionLine() {
        final RobotDocument document = newDocument("*** Tasks ***", "task");
        final DocumentCommand command = newDocumentCommand(18, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ");
    }

    @Test
    public void indentIsNotAdded_whenMovingToNewLineFromTaskDefinitionLineButWithCaretJustBeforeDefinition() {
        final RobotDocument document = newDocument("*** Tasks ***", "task");
        final DocumentCommand command = newDocumentCommand(14, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void indentIsAdded_whenMovingToNewLineFromKeywordDefinitionLine() {
        final RobotDocument document = newDocument("*** Keywords ***", "keyword");
        final DocumentCommand command = newDocumentCommand(24, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ");
    }

    @Test
    public void indentIsNotAdded_whenMovingToNewLineFromKeywordDefinitionLineButWithCaretJustBeforeDefinition() {
        final RobotDocument document = newDocument("*** Keywords ***", "keyword");
        final DocumentCommand command = newDocumentCommand(17, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void forLoopShouldBeContinuedWithBackslash_whenBreakingTheLine() {
        final List<String> lines = Arrays.asList(
                "  :FOR",
                "  : FOR",
                "  :FOR  ${i}    in    1    2");
        for (final String line : lines) {
            final RobotDocument document = newDocument("*** Test Cases ***", "t", line);
            final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

            final EditStrategyPreferences preferences = newPreferences();
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n  \\  ");
        }
    }

    @Test
    public void forLoopIsMovedLineBelow_whenLineBreakIsAddedBeforeForInSameLine_1() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  :FOR  ${i}  IN  @{l}");
        final DocumentCommand command = newDocumentCommand(21, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void forLoopIsMovedLineBelow_whenLineBreakIsAddedBeforeForInSameLine_2() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  :FOR  ${i}  IN  @{l}");
        final DocumentCommand command = newDocumentCommand(22, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n ");
    }

    @Test
    public void forLoopContinuationBeContinuedWithBackslash_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  :FOR  ${i}  IN  @{l}", "  \\    text");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  \\  ");
    }

    @Test
    public void newStyleForLoopIsIndentedAndFinishedWithEnd_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  FOR  ${i}  IN  @{l}");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n    \n  END");
        assertThat(command.shiftsCaret).isFalse();
        assertThat(command.caretOffset).isEqualTo(47);
    }

    @Test
    public void newStyleForLoopIsOnlyIndented_whenBreakingTheLineAndEndAlreadyExist() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  FOR  ${i}  IN  @{l}", "  END");
        final DocumentCommand command = newDocumentCommand(42, "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n    ");
        assertThat(command.shiftsCaret).isTrue();
        assertThat(command.caretOffset).isEqualTo(-1);
    }

    @Test
    public void documentationOfSuiteShouldBeContinuedWithDots_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Settings ***", "Documentation  doc");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n...  ");
    }

    @Test
    public void documentationOfTestCaseShouldBeContinuedWithDots_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  [Documentation]  doc");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ...  ");
    }

    @Test
    public void documentationOfTaskShouldBeContinuedWithDots_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Tasks ***", "t", "  [Documentation]  doc");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ...  ");
    }

    @Test
    public void documentationOfKeywordShouldBeContinuedWithDots_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Keywords ***", "kw", "  [Documentation]  doc");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final EditStrategyPreferences preferences = newPreferences();
        final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ...  ");
    }

    @Test
    public void previousLineContinuationShouldBeAdded() {
        final List<String> lines = Arrays.asList("...", "...     text");
        for (final String line : lines) {
            final RobotDocument document = newDocument(line);
            final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

            final EditStrategyPreferences preferences = newPreferences();
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n...  ");
        }
    }

    @Test
    public void breakingLineShouldAddContinuation_onlyForOffsetsBetweenCells() {
        final RobotDocument document = newDocument("*** Test Cases ***", "case", "  Log Many  ${x}   aa    bbb");
        final RangeSet<Integer> offsetsBetweenCells = TreeRangeSet
                .create(Arrays.asList(Range.closed(34, 36), Range.closed(40, 43), Range.closed(45, 49)));
        for (int offset = 34; offset <= 52; offset++) {
            final DocumentCommand command = newDocumentCommand(offset, "\n");

            final EditStrategyPreferences preferences = newPreferences();
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            if (offsetsBetweenCells.contains(offset)) {
                final Range<Integer> separator = offsetsBetweenCells.rangeContaining(offset);
                final int separatorStart = separator.lowerEndpoint();
                final int separatorEnd = separator.upperEndpoint();
                if (separatorStart == offset) {
                    assertThat(command.text).isEqualTo("\n  ...");
                    assertThat(command.offset).isEqualTo(separatorStart);
                    assertThat(command.length).isEqualTo(0);
                    assertThat(command.caretOffset).isEqualTo(separatorEnd);
                } else {
                    assertThat(command.text).isEqualTo("\n  ..." + Strings.repeat(" ", separatorEnd - separatorStart));
                    assertThat(command.offset).isEqualTo(separatorStart);
                    assertThat(command.length).isEqualTo(separatorEnd - separatorStart);
                    assertThat(command.caretOffset).isEqualTo(-1);
                }
            } else {
                assertThat(command.text).isEqualTo("\n  ");
                assertThat(command.offset).isEqualTo(offset);
                assertThat(command.length).isEqualTo(0);
                assertThat(command.caretOffset).isEqualTo(-1);
            }
        }
    }

    @Test
    public void breakingLineShouldNotAddContinuation_whenOffsetInNonBreakableCells() {
        final RobotDocument document = newDocument("*** Test Cases ***", "case", "  Log Many  ${x}  #comment  abc");
        for (int offset = 40; offset <= 55; offset++) {
            final DocumentCommand command = newDocumentCommand(offset, "\n");

            final EditStrategyPreferences preferences = newPreferences();
            final RobotSuiteAutoEditStrategy strategy = new RobotSuiteAutoEditStrategy(preferences, false);
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n  ");
            assertThat(command.offset).isEqualTo(offset);
            assertThat(command.length).isEqualTo(0);
            assertThat(command.caretOffset).isEqualTo(-1);
        }
    }

    private static RobotDocument newDocument(final String... lines) {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);

        final RobotParser parser = new RobotParser(robotProject.getRobotProjectHolder(), new RobotVersion(3, 1));
        final File file = new File("file.robot");

        final RobotDocument document = new RobotDocument(parser, file);
        document.set(String.join("\n", lines));
        return document;
    }

    private static DocumentCommand newDocumentCommand(final String text) {
        return newDocumentCommand(0, text);
    }

    private static DocumentCommand newDocumentCommand(final int offset, final String text) {
        return newDocumentCommand(offset, text, 0);
    }

    private static DocumentCommand newDocumentCommand(final int offset, final String text, final int length) {
        final DocumentCommand command = new DocumentCommand() {};
        command.offset = offset;
        command.text = text;
        command.shiftsCaret = true;
        command.caretOffset = -1;
        command.length = length;
        return command;
    }

    private EditStrategyPreferences newPreferences() {
        return newPreferences("  ", false);
    }

    private EditStrategyPreferences newPreferences(final String separator, final boolean isSeparatorJumpModeEnabled) {
        return new EditStrategyPreferences(null) {

            @Override
            String getSeparatorToUse(final boolean isTsvFile) {
                return separator;
            }

            @Override
            boolean isSeparatorJumpModeEnabled() {
                return isSeparatorJumpModeEnabled;
            }
        };
    }

    private EditStrategyPreferences newPreferences(final boolean isVariablesBracketsInsertionEnabled) {
        return newPreferences(isVariablesBracketsInsertionEnabled, false, null);
    }

    private EditStrategyPreferences newPreferences(final boolean isVariablesBracketsInsertionEnabled,
            final boolean isVariablesBracketsInsertionWrappingEnabled,
            final String variablesBracketsInsertionWrappingPattern) {
        return new EditStrategyPreferences(null) {

            @Override
            boolean isVariablesBracketsInsertionEnabled() {
                return isVariablesBracketsInsertionEnabled;
            }

            @Override
            boolean isVariablesBracketsInsertionWrappingEnabled() {
                return isVariablesBracketsInsertionWrappingEnabled;
            }

            @Override
            String getVariablesBracketsInsertionWrappingPattern() {
                return variablesBracketsInsertionWrappingPattern;
            }
        };
    }
}
