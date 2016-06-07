/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public interface ICommentHolder {

    List<RobotToken> getComment();

    void setComment(final String comment);

    void setComment(final RobotToken comment);

    void addCommentPart(final RobotToken cmPart);

    void removeCommentPart(final int index);

    void clearComment();
}
