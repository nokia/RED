package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyInCasesTableHandler.E4CopyInCasesTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.ArraysSerializerDeserializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyInCasesTableHandler extends DIParameterizedHandler<E4CopyInCasesTableHandler> {

    public CopyInCasesTableHandler() {
        super(E4CopyInCasesTableHandler.class);
    }

    public static class E4CopyInCasesTableHandler {

        @Execute
        public boolean copyContent(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final PositionCoordinate[] selectedCellPositions = editor.getSelectionLayerAccessor()
                    .getSelectedPositions();

            if (selectedCellPositions.length > 0) {

                final RobotKeywordCall[] keywordCalls = Selections.getElementsArray(selection, RobotKeywordCall.class);
                final RobotCase[] cases = Selections.getElementsArray(selection, RobotCase.class);

                final RobotKeywordCall[] keywordCallsCopy = ArraysSerializerDeserializer.copy(RobotKeywordCall.class,
                        keywordCalls);
                final RobotCase[] casesCopy = ArraysSerializerDeserializer.copy(RobotCase.class, cases);

                final PositionCoordinateSerializer[] serializablePositions = PositionCoordinateSerializer
                        .createFrom(selectedCellPositions);

                if (keywordCallsCopy.length == 0 && casesCopy.length == 0) {
                    return false;
                } else if (keywordCallsCopy.length > 0 && casesCopy.length > 0) {
                    clipboard.insertContent(serializablePositions, keywordCallsCopy, casesCopy);
                } else if (keywordCallsCopy.length > 0) {
                    clipboard.insertContent(serializablePositions, keywordCallsCopy);
                } else {
                    clipboard.insertContent(serializablePositions, casesCopy);
                }
                return true;
            }
            return false;
        }
    }
}
