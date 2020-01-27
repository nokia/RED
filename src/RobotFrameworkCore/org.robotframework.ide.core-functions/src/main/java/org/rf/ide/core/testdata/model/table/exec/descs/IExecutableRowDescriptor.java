/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import java.util.List;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public interface IExecutableRowDescriptor<T> {

    AModelElement<T> getRow();

    RowType getRowType();

    boolean isCreatingVariables();

    Stream<RobotToken> getCreatingVariables();

    List<VariableUse> getUsedVariables();

    RobotToken getAction();

    List<RobotToken> getKeywordArguments();

    RobotToken getKeywordAction();

    List<BuildMessage> getMessages();

    public enum RowType {
        UNKNOWN,
        SETTING,
        COMMENTED_HASH,
        SIMPLE,
        FOR,
        FOR_CONTINUE,
        FOR_END;
    }
}
