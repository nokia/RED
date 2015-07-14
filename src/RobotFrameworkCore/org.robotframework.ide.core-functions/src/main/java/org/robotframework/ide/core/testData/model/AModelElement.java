package org.robotframework.ide.core.testData.model;



public abstract class AModelElement {

    private final RobotLine containingLine;
    private final LineElement originalElement;


    public AModelElement(final RobotLine containingLine,
            final LineElement originalElement) {
        this.containingLine = containingLine;
        this.originalElement = originalElement;
    }


    public RobotLine getContainingLine() {
        return containingLine;
    }


    public LineElement getOriginalElement() {
        return originalElement;
    }
}
