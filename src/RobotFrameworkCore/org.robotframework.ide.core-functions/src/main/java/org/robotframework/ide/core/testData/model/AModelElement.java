package org.robotframework.ide.core.testData.model;

import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public abstract class AModelElement implements IOptional {

    public abstract ModelType getModelType();


    public abstract FilePosition getBeginPosition();


    public abstract List<RobotToken> getElementTokens();


    public FilePosition getEndPosition() {
        FilePosition pos = FilePosition.createNotSet();
        if (isPresent()) {
            List<RobotToken> elementTokens = getElementTokens();

            int size = elementTokens.size();
            for (int i = size - 1; i >= 0; i--) {
                RobotToken robotToken = elementTokens.get(i);
                if (robotToken.getStartOffset() >= 0) {
                    int endColumn = robotToken.getEndColumn();
                    int length = endColumn - robotToken.getStartColumn();
                    FilePosition fp = robotToken.getFilePosition();
                    pos = new FilePosition(fp.getLine(),
                            robotToken.getEndColumn(), fp.getOffset() + length);
                    break;
                }
            }
        }

        return pos;
    }
}
