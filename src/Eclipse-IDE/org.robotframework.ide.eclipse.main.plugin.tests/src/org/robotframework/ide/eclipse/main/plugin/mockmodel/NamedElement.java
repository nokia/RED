package org.robotframework.ide.eclipse.main.plugin.mockmodel;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class NamedElement implements RobotElement {

    private final String name;

    public NamedElement(final String name) {
        this.name = name;
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
    public RobotElement getParent() {
        return null;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return null;
    }

    @Override
    public List<? extends RobotElement> getChildren() {
        return newArrayList();
    }

    @Override
    public ImageDescriptor getImage() {
        return null;
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return null;
    }
}
