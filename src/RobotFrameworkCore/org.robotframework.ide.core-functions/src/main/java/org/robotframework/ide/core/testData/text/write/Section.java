package org.robotframework.ide.core.testData.text.write;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;


public class Section {

    private SectionType type = SectionType.UNKNOWN;
    private List<SectionElement> sectionContent = new LinkedList<>();

    public static class SectionElement {

        private ElementBoundaries boundaries = new ElementBoundaries();
        private List<Object> elements = new LinkedList<>();


        public void addElement(final Object elem) {
            elements.add(elem);
        }


        public List<Object> getElements() {
            return elements;
        }


        public ElementBoundaries getBoundaries() {
            return boundaries;
        }


        public void setBoundaries(ElementBoundaries boundaries) {
            this.boundaries = boundaries;
        }
    }

    public static class ElementBoundaries {

        private FilePosition startPosition;
        private FilePosition endPosition;


        public FilePosition getStartPosition() {
            return startPosition;
        }


        public void setStartPosition(FilePosition startPosition) {
            this.startPosition = startPosition;
        }


        public FilePosition getEndPosition() {
            return endPosition;
        }


        public void setEndPosition(FilePosition endPosition) {
            this.endPosition = endPosition;
        }
    }

    public enum SectionType {
        UNKNOWN, GARBAGE, SETTINGS, VARIABLES, TEST_CASES, KEYWORDS;
    }


    public SectionType getType() {
        return type;
    }


    public void setType(final SectionType type) {
        this.type = type;
    }


    public List<SectionElement> getSectionContent() {
        return sectionContent;
    }


    public boolean isSectionPresent() {
        return !sectionContent.isEmpty();
    }


    public void addSectionLine(final SectionElement sectionElem) {
        sectionContent.add(sectionElem);
    }
}
