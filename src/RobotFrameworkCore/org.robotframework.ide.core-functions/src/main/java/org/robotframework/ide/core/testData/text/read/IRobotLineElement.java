package org.robotframework.ide.core.testData.text.read;

public interface IRobotLineElement {

    public static final int NOT_SET = -1;


    int getLineNumber();


    int getStartColumn();


    int getEndColumn();


    StringBuilder getText();


    IRobotTokenType getType();
}
