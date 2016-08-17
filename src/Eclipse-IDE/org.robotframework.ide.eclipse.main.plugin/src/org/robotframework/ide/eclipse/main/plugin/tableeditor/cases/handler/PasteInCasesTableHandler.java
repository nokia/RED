package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.PasteInCasesTableHandler.E4PasteInCasesTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInCasesTableHandler extends DIParameterizedHandler<E4PasteInCasesTableHandler> {

    public PasteInCasesTableHandler() {
        super(E4PasteInCasesTableHandler.class);
    }

    public static class E4PasteInCasesTableHandler {

        @Execute
        public void pasteCellContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack, final RedClipboard clipboard) {

            final SelectionLayer selectionLayer = editor.getSelectionLayerAccessor().getSelectionLayer();
            final List<RobotElement> selectedElements = Selections.getElements(selection, RobotElement.class);

            final List<EditorCommand> pasteCommands = new PasteCasesCellsCommandsCollector()
                    .collectPasteCommands(selectionLayer, selectedElements, clipboard);

            for (final EditorCommand command : pasteCommands) {
                commandsStack.execute(command);
            }
        }
    }
}
