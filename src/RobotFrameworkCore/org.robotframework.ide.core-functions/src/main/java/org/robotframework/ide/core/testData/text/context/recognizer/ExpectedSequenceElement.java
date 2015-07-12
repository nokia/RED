package org.robotframework.ide.core.testData.text.context.recognizer;

import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;


/**
 * Represents single search for any word in line for
 * {@link ATableElementRecognizer}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public class ExpectedSequenceElement {

    private final IRobotTokenType type;
    private final PriorityType priority;


    public ExpectedSequenceElement(final IRobotTokenType type,
            final PriorityType priority) {
        this.type = type;
        this.priority = priority;
    }


    public IRobotTokenType getType() {
        return type;
    }


    public PriorityType getPriority() {
        return priority;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpectedSequenceElement other = (ExpectedSequenceElement) obj;
        if (priority != other.priority)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }


    public static ExpectedSequenceElement buildMandatory(
            final IRobotTokenType type) {
        return new ExpectedSequenceElement(type, PriorityType.MANDATORY);
    }


    public static ExpectedSequenceElement buildOptional(
            final IRobotTokenType type) {
        return new ExpectedSequenceElement(type, PriorityType.OPTIONAL);
    }

    public static enum PriorityType {
        OPTIONAL, MANDATORY
    }

}
