package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;


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
