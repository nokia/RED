package org.robotframework.ide.core.testData.text.section;

public abstract class ASectionElement {

    protected ISectionElementType type;
    private SectionPosition startPosition = new SectionPosition();
    private SectionPosition endPosition = new SectionPosition();


    public ISectionElementType getSectionType() {
        return type;
    }


    public SectionPosition getStartPosition() {
        return startPosition;
    }


    public void setStartPosition(SectionPosition startPosition) {
        this.startPosition = startPosition;
    }


    public SectionPosition getEndPosition() {
        return endPosition;
    }


    public void setEndPosition(SectionPosition endPosition) {
        this.endPosition = endPosition;
    }

    public interface ISectionElementType {
    }
}
