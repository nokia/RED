package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedList;
import java.util.List;


public class SubSectionElement extends ASectionElement {

    private List<ASectionElement> elements = new LinkedList<>();


    public void addSectionElement(final ASectionElement elem) {
        elements.add(elem);
    }


    public List<ASectionElement> getSectionElements() {
        return elements;
    }
}
