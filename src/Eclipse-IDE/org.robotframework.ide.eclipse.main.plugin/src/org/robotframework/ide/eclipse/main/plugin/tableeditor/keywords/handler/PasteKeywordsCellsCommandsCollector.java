package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.PasteRobotElementCellsCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsTableValuesChangingCommandsCollector;

/**
 * @author mmarzec
 */
public class PasteKeywordsCellsCommandsCollector extends PasteRobotElementCellsCommandsCollector {

    @Override
    protected boolean hasRobotElementsInClipboard(final RedClipboard clipboard) {
        return clipboard.hasKeywordCalls() || clipboard.hasKeywordDefinitions();
    }

    @Override
    protected RobotElement[] getRobotElementsFromClipboard(final RedClipboard clipboard) {
        final RobotKeywordCall[] keywordCalls = clipboard.getKeywordCalls();
        final RobotKeywordDefinition[] keywordDefinitions = clipboard.getKeywordDefinitions();
        final List<RobotElement> elements = new ArrayList<>();
        if (keywordCalls != null) {
            elements.addAll(Arrays.asList(keywordCalls));
        }
        if (keywordDefinitions != null) {
            elements.addAll(Arrays.asList(keywordDefinitions));
        }
        return elements.toArray(new RobotElement[0]);
    }

    @Override
    protected List<String> findValuesToPaste(final RobotElement elementFromClipboard,
            final int clipboardElementColumnIndex, final int tableColumnsCount) {
        if (elementFromClipboard instanceof RobotKeywordCall) {
            return getValuesFromKeywordCall((RobotKeywordCall) elementFromClipboard, clipboardElementColumnIndex);

        } else if (elementFromClipboard instanceof RobotKeywordDefinition) {
            return getValuesFromKeywordDefinition((RobotKeywordDefinition) elementFromClipboard,
                    clipboardElementColumnIndex, tableColumnsCount);
        }
        return new ArrayList<>();
    }

    @Override
    protected List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount) {

        final List<EditorCommand> pasteCommands = new ArrayList<>();

        final String valueToPaste = valuesToPaste.isEmpty() ? "" : valuesToPaste.get(0);
        final List<? extends EditorCommand> commands = new KeywordsTableValuesChangingCommandsCollector()
                .collectForChange(selectedElement, valueToPaste, selectedElementColumnIndex, tableColumnsCount);
        pasteCommands.addAll(commands);

        return pasteCommands;
    }

    private List<String> getValuesFromKeywordCall(final RobotKeywordCall keywordCall,
            final int clipboardElementColumnIndex) {

        final ModelType modelType = keywordCall.getLinkedElement().getModelType();

        if (clipboardElementColumnIndex > 0 && modelType == ModelType.USER_KEYWORD_DOCUMENTATION) {
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

    private List<String> getValuesFromKeywordDefinition(final RobotKeywordDefinition keywordDef,
            final int clipboardElementColumnIndex, final int tableColumnsCount) {
        if (clipboardElementColumnIndex == 0) {
            return newArrayList(keywordDef.getName());
        } else if (clipboardElementColumnIndex > 0 && clipboardElementColumnIndex < tableColumnsCount - 1) {
            final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
            final int argIndex = clipboardElementColumnIndex - 1;
            if (argumentsSetting != null && argIndex < argumentsSetting.getArguments().size()) {
                return newArrayList(argumentsSetting.getArguments().get(argIndex));
            }
        }
        return new ArrayList<>();
    }
}
