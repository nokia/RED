/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public abstract class RobotSuiteFileSection implements RobotElement {

    private final String name;

    private final RobotElement parent;
    protected final List<RobotElement> elements = new ArrayList<>();

    protected ARobotSectionTable sectionTable;

    RobotSuiteFileSection(final RobotSuiteFile parent, final String name) {
        this.parent = parent;
        this.name = name;
    }

    public void link(final ARobotSectionTable table) {
        this.sectionTable = table;
    }

    public ARobotSectionTable getLinkedElement() {
        return sectionTable;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == getClass()) {
            final RobotSuiteFileSection other = (RobotSuiteFileSection) obj;
            return name.equals(other.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getComment() {
        return "";
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getRobotCasesFileSectionImage();
    }

    @Override
    public Position getPosition() {
        // sectionTable.getHeaders().get(0).
        return new Position(0);
    }

    @Override
    public Position getDefinitionPosition() {
        final RobotToken tableHeader = sectionTable.getHeaders().get(0).getTableHeader();
        return new Position(tableHeader.getStartOffset(), tableHeader.getText().length());
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), RobotSuiteFileSection.this);
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public List<? extends RobotElement> getChildren() {
        return elements;
    }

    public RobotElement findChild(final String name) {
        for (final RobotElement element : elements) {
            if (element.getName().equals(name)) {
                return element;
            }
        }
        return null;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return (RobotSuiteFile) this.getParent();
    }

    public int getHeaderLine() {
        return sectionTable.getHeaders().get(0).getTableHeader().getLineNumber();
    }
}
