package org.robotframework.ide.core.testData.text.read;

import java.util.List;


public interface IRobotLineElement {

    public static final int NOT_SET = -1;


    int getLineNumber();


    int getStartColumn();


    int getEndColumn();


    int getStartOffset();


    StringBuilder getText();


    StringBuilder getRaw();


    List<IRobotTokenType> getTypes();
}
