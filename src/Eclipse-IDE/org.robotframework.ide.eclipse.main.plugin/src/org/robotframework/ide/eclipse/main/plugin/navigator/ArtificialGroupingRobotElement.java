/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class ArtificialGroupingRobotElement implements RobotFileInternalElement {

    private final SettingsGroup group;

    private final List<RobotFileInternalElement> groupedElements;

    public ArtificialGroupingRobotElement(final SettingsGroup group,
            final Collection<? extends RobotFileInternalElement> elements) {
        this.group = group;
        this.groupedElements = new ArrayList<>(elements);
    }

    public SettingsGroup getGroup() {
        return group;
    }

    @Override
    public String getName() {
        return group.getName();
    }

    @Override
    public String getComment() {
        return "";
    }

    @Override
    public RobotElement getParent() {
        return groupedElements.get(0);
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return groupedElements.get(0).getSuiteFile();
    }

    @Override
    public List<RobotFileInternalElement> getChildren() {
        return groupedElements;
    }

    @Override
    public ImageDescriptor getImage() {
        return groupedElements.get(0).getImage();
    }

    @Override
    public Position getPosition() {
        return groupedElements.get(0).getPosition();
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        return groupedElements.get(0).getDefinitionPosition();
    }

    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        return groupedElements.get(0).findElement(offset);
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return groupedElements.get(0).getOpenRobotEditorStrategy(page);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() == ArtificialGroupingRobotElement.class) {
            final ArtificialGroupingRobotElement that = (ArtificialGroupingRobotElement) obj;
            return Objects.equal(this.group, that.group) && Objects.equal(this.getSuiteFile(), that.getSuiteFile());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group, getSuiteFile());
    }

    @Override
    public Object getLinkedElement() {
        return null;
    }
}
