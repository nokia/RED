package org.robotframework.ide.core.testData.model.table.variables;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface IVariableHolder {

    VariableType getType();


    String getName();


    List<RobotToken> getComment();


    void addCommentPart(final RobotToken rt);


    RobotToken getDeclaration();
}
