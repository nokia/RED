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
