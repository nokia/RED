package org.rf.ide.core.testdata.model.presenter.update.keywords;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IKeywordTableElementOperation;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordExecutableRowModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_EXECUTABLE_ROW;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_ACTION_NAME;
    }

    @Override
    public AModelElement<?> create(final UserKeyword userKeyword, final String actionName, final List<String> args,
            final String comment) {
        final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
        row.setParent(userKeyword);

        row.setAction(RobotToken.create(actionName));
        for (final String argument : args) {
            row.addArgument(RobotToken.create(argument));
        }
        if (comment != null && !comment.isEmpty()) {
            row.setComment(comment);
        }
        return row;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword userKeyword, final int index,
            final AModelElement<?> modelElement) {
        @SuppressWarnings("unchecked")
        final RobotExecutableRow<UserKeyword> executableRow = (RobotExecutableRow<UserKeyword>) modelElement;
        
        userKeyword.addKeywordExecutionRow(executableRow, index);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final RobotExecutableRow<?> row = (RobotExecutableRow<?>) modelElement;

        if (value != null) {
            row.setArgument(index, value);
        } else if (index < row.getArguments().size()) {
            row.removeElementToken(index);
        }
    }

    @Override
    public void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        final RobotExecutableRow<?> row = (RobotExecutableRow<?>) modelElement;

        for (int i = 0; i < row.getArguments().size(); i++) {
            row.removeElementToken(0);
        }
        for (int i = 0; i < newArguments.size(); i++) {
            row.setArgument(i, newArguments.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.removeExecutableRow((RobotExecutableRow<UserKeyword>) modelElement);
    }
}
