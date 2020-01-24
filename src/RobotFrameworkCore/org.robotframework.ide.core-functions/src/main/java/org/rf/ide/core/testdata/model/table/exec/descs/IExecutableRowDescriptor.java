/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public interface IExecutableRowDescriptor<T> {

    List<VariableDeclaration> getCreatedVariables();

    RobotToken getAction();

    RobotToken getKeywordAction();

    List<VariableDeclaration> getUsedVariables();

    List<IElementDeclaration> getTextParameters();

    List<BuildMessage> getMessages();

    List<RobotToken> getKeywordArguments();

    RowType getRowType();

    AModelElement<T> getRow();

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
