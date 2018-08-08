/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
@FunctionalInterface
public interface ILineDumpTokenListener {

    void tokenDumped(final RobotToken oldToken, final RobotToken newToken);
}
