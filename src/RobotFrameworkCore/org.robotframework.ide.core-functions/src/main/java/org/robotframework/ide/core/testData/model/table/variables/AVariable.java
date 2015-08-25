package org.robotframework.ide.core.testData.model.table.variables;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public abstract class AVariable extends AModelElement implements
        IVariableHolder {

    protected VariableType type;
    private final RobotToken declaration;
    private final String name;
    private final List<RobotToken> comment = new LinkedList<>();


    protected AVariable(final VariableType type, final String name,
            final RobotToken declaration) {
        this.type = type;
        this.name = name;
        this.declaration = declaration;
    }


    public VariableType getType() {
        return type;
    }


    public String getName() {
        return name;
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }

    public enum VariableType {
        /**
         * 
         */
        SCALAR("$", RobotTokenType.VARIABLES_SCALAR_DECLARATION),
        /**
         * Deprecated
         */
        SCALAR_AS_LIST("$", RobotTokenType.VARIABLES_SCALAR_AS_LIST_DECLARATION),
        /**
         * 
         */
        LIST("@", RobotTokenType.VARIABLES_LIST_DECLARATION),
        /**
         * 
         */
        DICTIONARY("&", RobotTokenType.VARIABLES_DICTIONARY_DECLARATION),
        /**
         * 
         */
        INVALID("\\s", RobotTokenType.VARIABLES_UNKNOWN_DECLARATION);

        private final String identificator;
        private final RobotTokenType type;


        private VariableType(final String identificator,
                final RobotTokenType type) {
            this.identificator = identificator;
            this.type = type;
        }


        public String getIdentificator() {
            return identificator;
        }


        public RobotTokenType getType() {
            return type;
        }
    }


    @Override
    public ModelType getModelType() {
        ModelType modelType = ModelType.UNKNOWN;

        if (type == VariableType.DICTIONARY) {
            modelType = ModelType.DICTIONARY_VARIABLE_DECLARATION_IN_TABLE;
        } else if (type == VariableType.INVALID) {
            modelType = ModelType.UNKNOWN_VARIABLE_DECLARATION_IN_TABLE;
        } else if (type == VariableType.LIST) {
            modelType = ModelType.LIST_VARIABLE_DECLARATION_IN_TABLE;
        } else if (type == VariableType.SCALAR
                || type == VariableType.SCALAR_AS_LIST) {
            modelType = ModelType.SCALAR_VARIABLE_DECLARATION_IN_TABLE;
        }

        return modelType;
    }


    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }
}
