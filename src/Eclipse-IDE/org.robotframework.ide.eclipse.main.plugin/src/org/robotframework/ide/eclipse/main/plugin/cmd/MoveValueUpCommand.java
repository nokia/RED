package org.robotframework.ide.eclipse.main.plugin.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveValueUpCommand extends EditorCommand {

    RobotCollectionElement selectedElement;
    
    public MoveValueUpCommand(RobotCollectionElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    @Override
    public void execute() throws CommandExecutionException {
        //List<RobotCollectionElement> list = selectedElement.getFormDialog().getCollectionElements();
        selectedElement.getFormDialog().moveElementUp(selectedElement);
        
//        CollectionElement elementAbove = collectionElements.get(selectionIndex - 1);
//        elementAbove.setIndex(elementAbove.getIndex() + 1);
//        CollectionElement elementBelow = collectionElements.get(selectionIndex);
//        elementBelow.setIndex(elementAbove.getIndex() - 1);
//        collectionElements.set(selectionIndex - 1, elementBelow);
//        collectionElements.set(selectionIndex, elementAbove);
//        tableViewer.refresh();
    }
}
