package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesTableValuesChangingCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.PasteRobotElementCellsCommandsCollector;

public class PasteCasesCellsCommandsCollector extends PasteRobotElementCellsCommandsCollector {

    @Override
    protected boolean hasRobotElementsInClipboard(final RedClipboard clipboard) {
        return clipboard.hasKeywordCalls() || clipboard.hasCases();
    }

    @Override
    protected RobotElement[] getRobotElementsFromClipboard(final RedClipboard clipboard) {
        final RobotKeywordCall[] keywordCalls = clipboard.getKeywordCalls();
        final RobotCase[] cases = clipboard.getCases();
        final List<RobotElement> elements = new ArrayList<>();
        if (keywordCalls != null) {
            elements.addAll(Arrays.asList(keywordCalls));
        }
        if (cases != null) {
            elements.addAll(Arrays.asList(cases));
        }
        return elements.toArray(new RobotElement[0]);
    }

    @Override
    protected List<String> findValuesToPaste(final RobotElement elementFromClipboard,
            final int clipboardElementColumnIndex, final int tableColumnsCount) {
        if (elementFromClipboard instanceof RobotKeywordCall) {
            return getValuesFromKeywordCall((RobotKeywordCall) elementFromClipboard, clipboardElementColumnIndex);

        } else if (elementFromClipboard instanceof RobotCase) {
            return getValuesFromTestCase((RobotCase) elementFromClipboard, clipboardElementColumnIndex);
        }
        return new ArrayList<>();
    }

    @Override
    protected List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount) {

        final List<EditorCommand> pasteCommands = new ArrayList<>();

        final String valueToPaste = valuesToPaste.isEmpty() ? "" : valuesToPaste.get(0);
        new CasesTableValuesChangingCommandsCollector()
                .collectForChange(selectedElement, valueToPaste, selectedElementColumnIndex).ifPresent(pasteCommands::add);

        return pasteCommands;
    }

    private List<String> getValuesFromKeywordCall(final RobotKeywordCall keywordCall,
            final int clipboardElementColumnIndex) {
        final ModelType modelType = keywordCall.getLinkedElement().getModelType();

        if (clipboardElementColumnIndex > 0 && modelType == ModelType.TEST_CASE_DOCUMENTATION) {
            if (clipboardElementColumnIndex == 1) {
                return newArrayList(getDocumentationText(keywordCall));
            } else {
                return new ArrayList<>();
            }
        }

        final List<RobotToken> execRowView = ExecutablesRowHolderCommentService.execRowView(keywordCall);
        if (clipboardElementColumnIndex < execRowView.size()) {
            return newArrayList(execRowView.get(clipboardElementColumnIndex).getText());
        }
        return new ArrayList<>();
    }

    private String getDocumentationText(final RobotKeywordCall keywordCall) {
        return DocumentationServiceHandler.toEditConsolidated((IDocumentationHolder) keywordCall.getLinkedElement());
    }

    private List<String> getValuesFromTestCase(final RobotCase testCase, final int clipboardElementColumnIndex) {
        if (clipboardElementColumnIndex == 0) {
            return newArrayList(testCase.getName());
        }
        return new ArrayList<>();
    }
}
