/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.List;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public interface ExecutableSetting {

    RobotToken getDeclaration();

    RobotToken getKeywordName();

    List<RobotToken> getArguments();

    <T> RobotExecutableRow<T> asExecutableRow();

    boolean isSetup();

    boolean isTeardown();

    default boolean isDisabled() {
        return getKeywordName() == null || getKeywordName().getText().equalsIgnoreCase("none");
    }
}
