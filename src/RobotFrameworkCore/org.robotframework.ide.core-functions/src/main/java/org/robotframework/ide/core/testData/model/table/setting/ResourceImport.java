package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class ResourceImport extends AImported {

    private final List<RobotToken> unexpectedTrashArguments = new LinkedList<>();


    public ResourceImport(final RobotToken resourceDeclaration) {
        super(Type.RESOURCE, resourceDeclaration);
    }


    public List<RobotToken> getUnexpectedTrashArguments() {
        return unexpectedTrashArguments;
    }


    public void addUnexpectedTrashArgument(final RobotToken trashArgument) {
        this.unexpectedTrashArguments.add(trashArgument);
    }


    @Override
    public boolean isPresent() {
        return true; // TODO: check if correct imported
    }
}
