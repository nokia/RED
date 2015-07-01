package org.robotframework.ide.eclipse.main.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public class RobotCase implements RobotElement {

    private final RobotCasesSection parent;
    private String name;
    protected final List<RobotElement> elements = new ArrayList<>();

    public RobotCase(final RobotCasesSection parent, final String name) {
        this.parent = parent;
        this.name = name;
    }

    public RobotKeywordCall createKeywordCall(final String name, final String[] args, final String comment) {
        final RobotKeywordCall call = new RobotKeywordCall(this, name, Arrays.asList(args), comment);
        elements.add(call);
        return call;
    }

    public RobotCaseSetting createSettting(final String name, final String[] args, final String comment) {
        final RobotCaseSetting setting = new RobotCaseSetting(this, name, Arrays.asList(args), comment);
        elements.add(setting);
        return setting;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String newName) {
        this.name = newName;
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return parent.getSuiteFile();
    }

    @Override
    public List<RobotElement> getChildren() {
        return elements;
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getTestCaseImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), (RobotSuiteFileSection) getParent(),
                this);
    }
}
