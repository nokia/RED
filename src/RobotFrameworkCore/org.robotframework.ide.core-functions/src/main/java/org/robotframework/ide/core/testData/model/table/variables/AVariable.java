package org.robotframework.ide.core.testData.model.table.variables;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


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
        SCALAR("$"),
        /**
         * Deprecated
         */
        SCALAR_AS_LIST("$"),
        /**
         * 
         */
        LIST("@"),
        /**
         * 
         */
        DICTIONARY("&"),
        /**
         * 
         */
        INVALID(null);

        private final String identificator;


        private VariableType(final String identificator) {
            this.identificator = identificator;
        }


        public String getIdentificator() {
            return identificator;
        }
    }
}
