package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

public class RobotCase implements RobotElement, Serializable {

    private transient RobotCasesSection parent;
    private String name;
    protected final List<RobotElement> elements = new ArrayList<>();
    private String comment;

    public RobotCase(final RobotCasesSection parent, final String name, final String comment) {
        this.parent = parent;
        this.name = name;
        this.comment = comment;
    }

    public RobotKeywordCall createKeywordCall(final String name, final String[] args, final String comment) {
        final RobotKeywordCall call = new RobotKeywordCall(this, name, newArrayList(args), comment);
        elements.add(call);
        return call;
    }

    public RobotCaseSetting createSettting(final String name, final String[] args, final String comment) {
        final RobotCaseSetting setting = new RobotCaseSetting(this, name, newArrayList(args), comment);
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

    public List<String> getArguments() {
        return new ArrayList<String>();
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    public void setParent(final RobotCasesSection parent) {
        this.parent = parent;
    }

    public void fixParents(final RobotElement parent) {
        this.parent = (RobotCasesSection) parent;

        for (final RobotElement element : elements) {
            ((RobotKeywordCall) element).fixParents(this);
        }
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
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
