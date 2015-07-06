package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;

public class ArtificialGroupingRobotElement implements RobotElement {

    private final SettingsGroup group;

    private final List<RobotElement> groupedElements;

    public ArtificialGroupingRobotElement(final SettingsGroup group, final Collection<RobotElement> elements) {
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
    public RobotElement getParent() {
        return groupedElements.get(0);
    }

    @Override
    public void fixParents(final RobotElement parent) {
        // this method will never be called for artificial element objects
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return groupedElements.get(0).getSuiteFile();
    }

    @Override
    public List<RobotElement> getChildren() {
        return groupedElements;
    }

    @Override
    public ImageDescriptor getImage() {
        return groupedElements.get(0).getImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return groupedElements.get(0).getOpenRobotEditorStrategy(page);
    }
}
