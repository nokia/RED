package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshCodeHolderSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.PasteRobotElementCellsCommandsCollector;

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
    protected List<String> findValuesToPaste(RobotElement elementFromClipboard, int clipboardElementColumnIndex,
            int tableColumnsCount) {
        if (elementFromClipboard instanceof RobotKeywordCall) {
            return getValuesFromKeywordCall((RobotKeywordCall) elementFromClipboard, clipboardElementColumnIndex,
                    tableColumnsCount);
        } else if (elementFromClipboard instanceof RobotKeywordDefinition) {
            return getValuesFromKeywordDefinition((RobotKeywordDefinition) elementFromClipboard,
                    clipboardElementColumnIndex, tableColumnsCount);
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
            if (isCandidateForNewKeywordSetting(valueToPaste, selectedElementColumnIndex)) {
                pasteCommands.addAll(getCommandsForKeywordSetting(selectedElement, valueToPaste));
            } else {
                command = getCommandForKeywordCall((RobotKeywordCall) selectedElement, valueToPaste,
                        selectedElementColumnIndex, tableColumnsCount);
            }
        } else if (selectedElement instanceof RobotKeywordDefinition && !valueToPaste.isEmpty()) {
            command = getCommandForKeywordDefinition((RobotKeywordDefinition) selectedElement, valueToPaste,
                    selectedElementColumnIndex, tableColumnsCount);
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
            return modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW || modelType == ModelType.UNKNOWN
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

    private List<String> getValuesFromKeywordDefinition(final RobotKeywordDefinition keywordDef,
            int clipboardElementColumnIndex, int tableColumnsCount) {
        if (clipboardElementColumnIndex == 0) {
            return newArrayList(keywordDef.getName());
        } else if (clipboardElementColumnIndex > 0 && clipboardElementColumnIndex < tableColumnsCount - 1) {
            final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
            final int argIndex = clipboardElementColumnIndex - 1;
            if (argumentsSetting != null && argIndex < argumentsSetting.getArguments().size()) {
                return newArrayList(argumentsSetting.getArguments().get(argIndex));
            }
        }
        return newArrayList();
    }

    private EditorCommand getCommandForKeywordCall(final RobotKeywordCall keywordCall, final String valueToPaste,
            final int selectedElementColumnIndex, final int tableColumnsCount) {
        if (keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
            if (selectedElementColumnIndex == 0) {
                return new SetKeywordCallNameCommand(keywordCall, valueToPaste);
            } else if (selectedElementColumnIndex > 0 && selectedElementColumnIndex < (tableColumnsCount - 1)) {
                return new SetKeywordCallArgumentCommand(keywordCall, selectedElementColumnIndex - 1, valueToPaste);
            } else if (selectedElementColumnIndex == (tableColumnsCount - 1)) {
                return new SetKeywordCallCommentCommand(keywordCall, valueToPaste);
            }
        } else if (selectedElementColumnIndex > 0 && selectedElementColumnIndex < (tableColumnsCount - 1)) {
            return new SetKeywordSettingArgumentCommand(keywordCall, selectedElementColumnIndex - 1, valueToPaste);
        }
        return null;
    }

    private EditorCommand getCommandForKeywordDefinition(final RobotKeywordDefinition keywordDef,
            final String valueToPaste, final int selectedElementColumnIndex, final int tableColumnsCount) {
        if (selectedElementColumnIndex == 0) {
            return new SetKeywordDefinitionNameCommand(keywordDef, valueToPaste);
        } else if (selectedElementColumnIndex > 0 && selectedElementColumnIndex < (tableColumnsCount - 1)) {
            return new SetKeywordDefinitionArgumentCommand(keywordDef, selectedElementColumnIndex - 1, valueToPaste);
        }
        return null;
    }
    
    private List<EditorCommand> getCommandsForKeywordSetting(final RobotElement selectedElement,
            final String valueToPaste) {
        final List<EditorCommand> commands = newArrayList();
        final IRobotCodeHoldingElement parent = ((RobotKeywordCall) selectedElement).getParent();
        if (parent != null) {
            commands.add(new DeleteKeywordCallCommand(newArrayList((RobotKeywordCall) selectedElement)));
            commands.add(new CreateFreshCodeHolderSettingCommand((RobotCodeHoldingElement) parent,
                    parent.getChildren().indexOf(selectedElement), valueToPaste,
                    ((RobotKeywordCall) selectedElement).getArguments()));
        }
        return commands;
    }
    
    private boolean isCandidateForNewKeywordSetting(final String valueToPaste, final int selectedElementColumnIndex) {
        return selectedElementColumnIndex == 0 && valueToPaste.startsWith("[") && valueToPaste.endsWith("]");
    }
}
