package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class RobotExecutableRow extends AModelElement {

    private RobotToken action;
    private final List<RobotToken> arguments = new LinkedList<>();


    public RobotExecutableRow() {
        this.action = new RobotToken();
    }


    public RobotToken getAction() {
        return action;
    }


    public void setAction(RobotToken action) {
        this.action = action;
    }


    @Override
    public boolean isPresent() {
        return true;
    }
}
