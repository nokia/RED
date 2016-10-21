package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.junit.Before;
import org.junit.Test;

public class SuiteSourceIndentLineEditStrategyTest {

    private final SuiteSourceIndentLineEditStrategy strategy = new SuiteSourceIndentLineEditStrategy(false);

    private Document document;

    private DocumentCommand command;

    @Before
    public void before() {
        document = new Document();
        command = new DocumentCommand() {
        };
    }

    @Test
    public void commandShouldNotBeChanged_whenItIsNotLineBreak() {
        document.set("x");
        command.offset = 1;
        command.text = "q";

        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("q");
    }

    @Test
    public void indentShouldNotBeAdded_whenPreviousLineDoesNotStartFromIndent() {
        document.set("xyz");
        command.offset = 3;
        command.text = "\n";

        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n");
    }

    @Test
    public void indentFromPreviousLineShouldBeAdded() {
        document.set("    abc");
        command.offset = 7;
        command.text = "\n";

        strategy.customizeDocumentCommand(document, command);

        assertThat(command.text).isEqualTo("\n    ");
    }

    @Test
    public void forLoopShouldBeStarted() {
        final List<String> lines = Arrays.asList("  :FOR", "  : FOR", "  :FOR  ${i}  in  1  2");
        for (String line : lines) {
            document.set(line);
            command.offset = line.length();
            command.text = "\n";

            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n  \\");
        }
    }

    @Test
    public void forLoopContinuationShouldBeAdded() {
        final List<String> lines = Arrays.asList("  \\", "  \\  text");
        for (String line : lines) {
            document.set(line);
            command.offset = line.length();
            command.text = "\n";

            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n  \\");
        }
    }

    @Test
    public void documentationShouldBeStarted() {
        final List<String> lines = Arrays.asList("  [Documentation]", "  [Documentation]  text");
        for (String line : lines) {
            document.set(line);
            command.offset = line.length();
            command.text = "\n";

            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n  ...");
        }
    }

    @Test
    public void previousLineContinuationShouldBeAdded() {
        final List<String> lines = Arrays.asList("  ...", "  ...  text");
        for (String line : lines) {
            document.set(line);
            command.offset = line.length();
            command.text = "\n";

            strategy.customizeDocumentCommand(document, command);

            assertThat(command.text).isEqualTo("\n  ...");
        }
    }
}
