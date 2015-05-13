package org.robotframework.ide.core.testData.text;

public interface IMatcher {

    RobotToken match(int currentChar, LinearFilePosition positionInLine);
}
