/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author Michal Anglart
 *
 */
class RobotTokens {

    static RobotToken create(final String content) {
        final RobotToken token = new RobotToken();
        token.setText(content);
        token.setRaw(content);
        return token;
    }
}
