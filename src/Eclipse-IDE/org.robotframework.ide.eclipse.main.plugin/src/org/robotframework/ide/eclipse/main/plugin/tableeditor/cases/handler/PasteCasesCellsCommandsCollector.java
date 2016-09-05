package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
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
    protected List<String> findValuesToPaste(final RobotElement elementFromClipboard, final int clipboardElementColumnIndex,
            final int tableColumnsCount) {
        if (elementFromClipboard instanceof RobotKeywordCall) {
            return getValuesFromKeywordCall((RobotKeywordCall) elementFromClipboard, clipboardElementColumnIndex,
                    tableColumnsCount);
        } else if (elementFromClipboard instanceof RobotCase) {
            return getValuesFromTestCase((RobotCase) elementFromClipboard, clipboardElementColumnIndex,
                    tableColumnsCount);
        }
        return newArrayList();
    }

    @Override
    protected List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount) {

        final List<EditorCommand> pasteCommands = newArrayList();

        EditorCommand command = null;
        final String valueToPaste = valuesToPaste.isEmpty() ? "" : valuesToPaste.get(0);
        if (selectedElement instanceof RobotKeywordCall) {
            command = getCommandForKeywordCall((RobotKeywordCall) selectedElement, valueToPaste,
                    selectedElementColumnIndex, tableColumnsCount);
        } else if (selectedElement instanceof RobotCase && !valueToPaste.isEmpty()) {
            command = getCommandForTestCase((RobotCase) selectedElement, valueToPaste, selectedElementColumnIndex,
                    tableColumnsCount);
        }

        if (command != null) {
            pasteCommands.add(command);
        }
        return pasteCommands;
    }

    private List<String> getValuesFromKeywordCall(final RobotKeywordCall keywordCall,
            final int clipboardElementColumnIndex, final int tableColumnsCount) {
        if (clipboardElementColumnIndex == 0) {
            final ModelType modelType = keywordCall.getLinkedElement().getModelType();
            return modelType == ModelType.TEST_CASE_EXECUTABLE_ROW || modelType == ModelType.UNKNOWN
                    ? newArrayList(keywordCall.getName()) : newArrayList("[" + keywordCall.getName() + "]");
        } else if (clipboardElementColumnIndex > 0 && clipboardElementColumnIndex < tableColumnsCount - 1) {
            final List<String> arguments = keywordCall.getArguments();
            final int argIndex = clipboardElementColumnIndex - 1;
            if (argIndex < arguments.size()) {
                return newArrayList(arguments.get(argIndex));
            }
        } else if (clipboardElementColumnIndex == tableColumnsCount - 1) {
            return newArrayList(keywordCall.getComment());
        }
        return newArrayList();
    }

    private List<String> getValuesFromTestCase(final RobotCase testCase, final int clipboardElementColumnIndex,
            final int tableColumnsCount) {
        if (clipboardElementColumnIndex == 0) {
            return newArrayList(testCase.getName());
        }
        return newArrayList();
    }

    private EditorCommand getCommandForKeywordCall(final RobotKeywordCall keywordCall, final String valueToPaste,
            final int selectedElementColumnIndex, final int tableColumnsCount) {
        if (selectedElementColumnIndex == 0) {
            return new SetCaseKeywordCallNameCommand(keywordCall, valueToPaste);
        } else if (selectedElementColumnIndex > 0 && selectedElementColumnIndex < (tableColumnsCount - 1)) {
            return new SetCaseKeywordCallArgumentCommand(keywordCall, selectedElementColumnIndex - 1, valueToPaste);
        } else if (selectedElementColumnIndex == (tableColumnsCount - 1)) {
            return new SetKeywordCallCommentCommand(keywordCall, valueToPaste);
        }

        return null;
    }

    private EditorCommand getCommandForTestCase(final RobotCase testCase, final String valueToPaste,
            final int selectedElementColumnIndex, final int tableColumnsCount) {
        if (selectedElementColumnIndex == 0) {
            return new SetCaseNameCommand(testCase, valueToPaste);
        }
        return null;
    }
}
