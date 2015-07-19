package org.robotframework.ide.core.testData.model;

import org.robotframework.ide.core.testData.model.LineElement.ElementType;


public abstract class AModelElement {

    private final ElementType type;
    private final RobotLine containingLine;
    private final LineElement originalElement;


    public AModelElement(final ElementType type,
            final RobotLine containingLine, final LineElement originalElement) {
        this.type = type;
        this.containingLine = containingLine;
        this.originalElement = originalElement;
    }


    public RobotLine getContainingLine() {
        return containingLine;
    }


    public LineElement getOriginalElement() {
        return originalElement;
    }


    public ElementType getType() {
        return type;
    }


    @Override
    public String toString() {
        return String
                .format("AModelElement [type=%s, containingLine=%s, originalElement=%s]",
                        type, containingLine, originalElement);
    }

}
