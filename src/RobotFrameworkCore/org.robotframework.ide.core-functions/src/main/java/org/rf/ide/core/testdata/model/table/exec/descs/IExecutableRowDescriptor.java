/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;

public interface IExecutableRowDescriptor<T> {

    List<VariableDeclaration> getCreatedVariables();

    RobotAction getAction();

    List<VariableDeclaration> getUsedVariables();

    List<VariableDeclaration> getCommentedVariables();

    List<IElementDeclaration> getTextParameters();

    List<BuildMessage> getMessages();

    IRowType getRowType();

    RobotExecutableRow<T> getRow();

    public enum ERowType implements IRowType {
        UNKONWN,
        COMMENTED_HASH,
        SIMPLE,
        FOR,
        FOR_CONTINUE;

        @Override
        public IRowType getParentType() {
            return null;
        }
    }

    public interface IRowType {

        IRowType getParentType();
    }
}
