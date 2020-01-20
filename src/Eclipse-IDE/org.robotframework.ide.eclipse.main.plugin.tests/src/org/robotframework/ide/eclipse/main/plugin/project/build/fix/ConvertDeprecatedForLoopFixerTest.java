package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;


public class ConvertDeprecatedForLoopFixerTest {

    @Test
    public void documentIsProperlyChangedAndEndStatementIsAdded_whenThereIsNoEndStatementAfterForRegion()
            throws IOException, CoreException {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(IMarker.CHAR_START, -1)).thenReturn(8);
        when(marker.getAttribute(IMarker.CHAR_END, -1)).thenReturn(13);
        when(marker.getAttribute(IMarker.LINE_NUMBER, -1)).thenReturn(1);

        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("        :F oR  ${x}    IN RANGE    1")
                .appendLine("        \\    kw    \\")
                .build();

        final Document document = new Document("        :F oR  ${x}    IN RANGE    1", "        \\    kw    \\");
        final ConvertDeprecatedForLoopFixer fixer = new ConvertDeprecatedForLoopFixer(49);
        final Stream<IDocument> changedDocuments = Stream.of(fixer)
                .map(Fixers.byApplyingToDocument(marker, document, model));

        assertThat(fixer.getLabel()).isEqualTo("Convert to current FOR loop syntax");
        assertThat(changedDocuments)
                .containsExactly(
                        new Document("        FOR  ${x}    IN RANGE    1", "             kw    \\", "        END"));
    }

    @Test
    public void documentIsProperlyChangedEndStatementIsNotAdded_whenThereIsEndStatementAfterForRegion()
            throws CoreException, IOException {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(IMarker.CHAR_START, -1)).thenReturn(0);
        when(marker.getAttribute(IMarker.CHAR_END, -1)).thenReturn(3);
        when(marker.getAttribute(IMarker.LINE_NUMBER, -1)).thenReturn(1);

        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("FOR  ${x}    IN RANGE    1")
                .appendLine("    \\    kw")
                .appendLine("END")
                .build();

        final Document document = new Document("FOR  ${x}    IN RANGE    1", "    \\    kw", "END");
        final ConvertDeprecatedForLoopFixer fixer = new ConvertDeprecatedForLoopFixer(38);
        final Stream<IDocument> changedDocuments = Stream.of(fixer)
                .map(Fixers.byApplyingToDocument(marker, document, model));

        assertThat(fixer.getLabel()).isEqualTo("Convert to current FOR loop syntax");
        assertThat(changedDocuments).containsExactly(new Document("FOR  ${x}    IN RANGE    1", "         kw", "END"));
    }

    @Test
    public void documentIsProperlyChangedEndStatementIsNotAdded_whenThereIsNoKeywordsAndEndStatementInForLoop()
            throws CoreException, IOException {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(IMarker.CHAR_START, -1)).thenReturn(0);
        when(marker.getAttribute(IMarker.CHAR_END, -1)).thenReturn(4);
        when(marker.getAttribute(IMarker.LINE_NUMBER, -1)).thenReturn(1);

        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine(":FOR  ${x}    IN RANGE    1")
                .appendLine("        kw")
                .build();

        final Document document = new Document(":FOR  ${x}    IN RANGE    1", "        kw");
        final ConvertDeprecatedForLoopFixer fixer = new ConvertDeprecatedForLoopFixer(27);
        final Stream<IDocument> changedDocuments = Stream.of(fixer).map(Fixers.byApplyingToDocument(marker, document,
                model));

        assertThat(fixer.getLabel()).isEqualTo("Convert to current FOR loop syntax");
        assertThat(changedDocuments).containsExactly(new Document("FOR  ${x}    IN RANGE    1", "END", "        kw"));
    }
}
