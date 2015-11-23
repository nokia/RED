/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.mapping;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public interface IHashCommentMapper {

    boolean isApplicable(ParsingState state);


    void map(final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel);
}
