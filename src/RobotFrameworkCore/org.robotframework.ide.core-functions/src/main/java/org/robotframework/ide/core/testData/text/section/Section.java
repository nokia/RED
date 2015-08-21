package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedList;
import java.util.List;


public class Section {

    private SectionType type = SectionType.UNKNOWN;
    private List<ASectionElement> elements = new LinkedList<>();


    public void addSectionElement(final ASectionElement elem) {
        elements.add(elem);
    }


    public List<ASectionElement> getSectionElements() {
        return elements;
    }


    public boolean isEmptySection() {
        return elements.isEmpty();
    }


    public void setType(SectionType type) {
        this.type = type;
    }


    public SectionType getType() {
        return type;
    }

    public enum SectionType {
        UNKNOWN, GARBAGE, SETTINGS, VARIABLES, TEST_CASES, KEYWORDS;
    }
}
