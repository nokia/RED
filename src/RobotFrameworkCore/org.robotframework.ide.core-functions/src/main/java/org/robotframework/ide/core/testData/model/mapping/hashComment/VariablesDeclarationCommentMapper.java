package org.robotframework.ide.core.testData.model.mapping.hashComment;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class VariablesDeclarationCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SCALAR_VARIABLE_DECLARATION
                || state == ParsingState.SCALAR_VARIABLE_VALUE
                || state == ParsingState.LIST_VARIABLE_DECLARATION
                || state == ParsingState.LIST_VARIABLE_VALUE
                || state == ParsingState.DICTIONARY_VARIABLE_DECLARATION
                || state == ParsingState.DICTIONARY_VARIABLE_VALUE || state == ParsingState.VARIABLE_TABLE_INSIDE);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<IVariableHolder> variables = fileModel.getVariableTable()
                .getVariables();
        if (variables.isEmpty()) {
            ListVariable var = new ListVariable(null,
                    createArtifactalListVariable(rt));
            var.addCommentPart(rt);
            variables.add(var);
        } else {
            IVariableHolder var = variables.get(variables.size() - 1);
            if (var.getDeclaration().getLineNumber() == rt.getLineNumber()) {
                var.addCommentPart(rt);
            } else {
                ListVariable newVar = new ListVariable(null,
                        createArtifactalListVariable(rt));
                newVar.addCommentPart(rt);
                variables.add(newVar);
            }
        }

    }


    @VisibleForTesting
    protected RobotToken createArtifactalListVariable(final RobotToken rt) {
        RobotToken token = new RobotToken();
        token.setLineNumber(rt.getLineNumber());
        token.setStartColumn(rt.getStartColumn());
        token.setText(new StringBuilder());
        token.setRaw(new StringBuilder());
        token.setType(RobotTokenType.VARIABLES_LIST_DECLARATION);

        return token;
    }
}
