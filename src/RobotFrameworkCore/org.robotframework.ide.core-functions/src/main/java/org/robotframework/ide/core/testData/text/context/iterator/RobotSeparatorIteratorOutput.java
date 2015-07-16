package org.robotframework.ide.core.testData.text.context.iterator;

import org.robotframework.ide.core.testData.text.context.IContextElementType;


public class RobotSeparatorIteratorOutput {

    private StringBuilder leftPrettyAlign = new StringBuilder();
    private StringBuilder separator = new StringBuilder();
    private StringBuilder rightPrettyAlign = new StringBuilder();
    private final IContextElementType separatorType;


    public RobotSeparatorIteratorOutput(final IContextElementType separatorType) {
        this.separatorType = separatorType;
    }


    public IContextElementType getSeparatorType() {
        return separatorType;
    }


    public StringBuilder getLeftPrettyAlign() {
        return leftPrettyAlign;
    }


    public void setLeftPrettyAlign(StringBuilder leftPrettyAlign) {
        this.leftPrettyAlign = leftPrettyAlign;
    }


    public boolean hasLeftPrettyAlign() {
        return (leftPrettyAlign != null && isNotEmpty(leftPrettyAlign));
    }


    public StringBuilder getSeparator() {
        return separator;
    }


    public void setSeparator(StringBuilder separator) {
        this.separator = separator;
    }


    public boolean hasSeparator() {
        return (separator != null && isNotEmpty(separator));
    }


    public StringBuilder getRightPrettyAlign() {
        return rightPrettyAlign;
    }


    public void setRightPrettyAlign(StringBuilder rightPrettyAlign) {
        this.rightPrettyAlign = rightPrettyAlign;
    }


    public boolean hasRightPrettyAlign() {
        return (rightPrettyAlign != null && isNotEmpty(rightPrettyAlign));
    }


    private boolean isNotEmpty(final StringBuilder text) {
        return (text.length() > 0);
    }


    @Override
    public String toString() {
        return String
                .format("RobotSeparatorIteratorOutput [leftPrettyAlign=%s, separator=%s, rightPrettyAlign=%s, separatorType=%s]",
                        leftPrettyAlign, separator, rightPrettyAlign,
                        separatorType);
    }

}
