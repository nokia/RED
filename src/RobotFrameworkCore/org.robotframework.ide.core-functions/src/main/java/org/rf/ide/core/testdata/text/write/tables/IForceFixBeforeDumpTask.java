/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public interface IForceFixBeforeDumpTask {

    void fixBeforeDump(final AModelElement<? extends IExecutableStepsHolder<?>> currentElement,
            final List<RobotToken> tokens);
}
