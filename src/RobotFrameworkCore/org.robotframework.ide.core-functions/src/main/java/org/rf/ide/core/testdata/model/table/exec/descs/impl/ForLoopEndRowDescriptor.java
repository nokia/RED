package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ForLoopEndRowDescriptor<T> implements IExecutableRowDescriptor<T> {

    private final RobotExecutableRow<T> row;

    public ForLoopEndRowDescriptor(final RobotExecutableRow<T> row) {
        this.row = row;
    }

    @Override
    public List<VariableDeclaration> getCreatedVariables() {
        return new ArrayList<>();
    }

    @Override
    public RobotAction getAction() {
        return new RobotAction(row.getAction(), null);
    }

    @Override
    public RobotAction getKeywordAction() {
        return getAction();
    }

    @Override
    public List<VariableDeclaration> getUsedVariables() {
        return new ArrayList<>();
    }

    @Override
    public List<VariableDeclaration> getCommentedVariables() {
        return new ArrayList<>();
    }

    @Override
    public List<IElementDeclaration> getTextParameters() {
        return null;
    }

    @Override
    public List<BuildMessage> getMessages() {
        return new ArrayList<>();
    }

    @Override
    public List<RobotToken> getKeywordArguments() {
        return new ArrayList<>();
    }

    @Override
    public RowType getRowType() {
        return RowType.FOR_END;
    }

    @Override
    public AModelElement<T> getRow() {
        return row;
    }
}
