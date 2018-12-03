package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.text.DocumentCommand;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

public class RobotSuiteAutoEditStrategyTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotSuiteAutoEditStrategyTest.class);

    @Test
    public void separatorIsUsed_whenTabWasOriginallyRequested() {
        final RobotDocument document = newDocument("");
        final DocumentCommand command = newDocumentCommand(0, "\t");

        final RobotSuiteAutoEditStrategy strategy = newStrategy("the_separator");
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("the_separator");
    }

    @Test
    public void commandShouldNotBeChanged_whenItIsNotLineBreak() {
        final RobotDocument document = newDocument("x");
        final DocumentCommand command = newDocumentCommand(1, "q");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("q");
    }

    @Test
    public void indentShouldNotBeAdded_whenPreviousLineDoesNotStartFromIndent() {
        final RobotDocument document = newDocument("xyz");
        final DocumentCommand command = newDocumentCommand(3, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void indentFromPreviousLineShouldBeAdded_1() {
        final RobotDocument document = newDocument("    abc");
        final DocumentCommand command = newDocumentCommand(7, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n    ");
    }

    @Test
    public void indentFromPreviousLineShouldBeAdded_2() {
        final RobotDocument document = newDocument("\tabc");
        final DocumentCommand command = newDocumentCommand(4, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n\t");
    }

    @Test
    public void indentIsAdded_whenMovingToNewLineFromTestDefinitionLine() {
        final RobotDocument document = newDocument("*** Test Cases ***", "test");
        final DocumentCommand command = newDocumentCommand(23, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ");
    }

    @Test
    public void indentIsNotAdded_whenMovingToNewLineFromTestDefinitionLineButWithCaretJustBeforeDefinition() {
        final RobotDocument document = newDocument("*** Test Cases ***", "test");
        final DocumentCommand command = newDocumentCommand(19, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void indentIsAdded_whenMovingToNewLineFromTaskDefinitionLine() {
        final RobotDocument document = newDocument("*** Tasks ***", " task");
        final DocumentCommand command = newDocumentCommand(19, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ");
    }

    @Test
    public void indentIsNotAdded_whenMovingToNewLineFromTaskDefinitionLineButWithCaretJustBeforeDefinition() {
        final RobotDocument document = newDocument("*** Tasks ***", " task");
        final DocumentCommand command = newDocumentCommand(15, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void indentIsAdded_whenMovingToNewLineFromKeywordDefinitionLine() {
        final RobotDocument document = newDocument("*** Keywords ***", "keyword");
        final DocumentCommand command = newDocumentCommand(24, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ");
    }

    @Test
    public void indentIsNotAdded_whenMovingToNewLineFromKeywordDefinitionLineButWithCaretJustBeforeDefinition() {
        final RobotDocument document = newDocument("*** Keywords ***", "keyword");
        final DocumentCommand command = newDocumentCommand(17, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
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

            final RobotSuiteAutoEditStrategy strategy = newStrategy();
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n  \\  ");
        }
    }

    @Test
    public void forLoopIsMovedLineBelow_whenLineBreakIsAddedBeforeForInSameLine_1() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  :FOR  ${i}  IN  @{l}");
        final DocumentCommand command = newDocumentCommand(21, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void forLoopIsMovedLineBelow_whenLineBreakIsAddedBeforeForInSameLine_2() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  :FOR  ${i}  IN  @{l}");
        final DocumentCommand command = newDocumentCommand(22, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n ");
    }

    @Test
    public void forLoopContinuationBeContinuedWithBackslash_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  :FOR  ${i}  IN  @{l}", "  \\    text");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  \\  ");
    }

    @Test
    public void newStyleForLoopIsIndentedAndFinishedWithEnd_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  FOR  ${i}  IN  @{l}");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n    \n  END");
        assertThat(command.shiftsCaret).isFalse();
        assertThat(command.caretOffset).isEqualTo(47);
    }

    @Test
    public void newStyleForLoopIsOnlyIndented_whenBreakingTheLineAndEndAlreadyExist() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  FOR  ${i}  IN  @{l}", "  END");
        final DocumentCommand command = newDocumentCommand(42, "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n    ");
        assertThat(command.shiftsCaret).isTrue();
        assertThat(command.caretOffset).isEqualTo(-1);
    }

    @Test
    public void documentationOfSuiteShouldBeContinuedWithDots_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Settings ***", "Documentation  doc");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n...  ");
    }

    @Test
    public void documentationOfTestCaseShouldBeContinuedWithDots_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Test Cases ***", "t", "  [Documentation]  doc");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ...  ");
    }

    @Test
    public void documentationOfTaskShouldBeContinuedWithDots_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Tasks ***", "t", "  [Documentation]  doc");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ...  ");
    }

    @Test
    public void documentationOfKeywordShouldBeContinuedWithDots_whenBreakingTheLine() {
        final RobotDocument document = newDocument("*** Keywords ***", "kw", "  [Documentation]  doc");
        final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

        final RobotSuiteAutoEditStrategy strategy = newStrategy();
        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n  ...  ");
    }

    @Test
    public void previousLineContinuationShouldBeAdded() {
        final List<String> lines = Arrays.asList("...", "...     text");
        for (final String line : lines) {
            final RobotDocument document = newDocument(line);
            final DocumentCommand command = newDocumentCommand(document.getLength(), "\n");

            final RobotSuiteAutoEditStrategy strategy = newStrategy();
            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n...  ");
        }
    }

    private static RobotDocument newDocument(final String... lines) {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final RobotParser parser = RobotParser.create(robotProject.getRobotProjectHolder(),
                RobotParserConfig.allImportsLazy(new RobotVersion(3, 1)));
        final File file = new File("file.robot");

        final RobotDocument document = new RobotDocument(parser, file);
        document.set(Stream.of(lines).collect(joining("\n")));
        return document;
    }

    private static DocumentCommand newDocumentCommand(final int offset, final String text) {
        final DocumentCommand document = new DocumentCommand() { };
        document.offset = offset;
        document.text = text;
        document.shiftsCaret = true;
        document.caretOffset = -1;
        return document;
    }

    private RobotSuiteAutoEditStrategy newStrategy() {
        return newStrategy("  ");
    }

    private RobotSuiteAutoEditStrategy newStrategy(final String separator) {
        return new RobotSuiteAutoEditStrategy(() -> separator);
    }
}
