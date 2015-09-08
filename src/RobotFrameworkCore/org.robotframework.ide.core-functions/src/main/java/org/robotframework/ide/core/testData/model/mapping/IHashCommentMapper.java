package org.robotframework.ide.core.testData.model.mapping;

import org.robotframework.ide.core.testData.model.IRobotFile;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface IHashCommentMapper {

    boolean isApplicable(ParsingState state);


    void map(final RobotToken rt, final ParsingState currentState,
            final IRobotFile fileModel);
}
