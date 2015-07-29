package org.robotframework.ide.eclipse.main.plugin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

public class RobotKeywordCall implements RobotElement, Serializable {

    private String name;
    private List<String> args;
    private String comment;
    private transient IRobotCodeHoldingElement parent;

    RobotKeywordCall(final IRobotCodeHoldingElement parent, final String name, final List<String> args,
            final String comment) {
        this.parent = parent;
        this.name = name;
        this.args = args;
        this.comment = comment;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public IRobotCodeHoldingElement getParent() {
        return parent;
    }

    public void setParent(final IRobotCodeHoldingElement parent) {
        this.parent = parent;
    }

    public void fixParents() {
        // nothing to fix
    }

    void fixParents(final IRobotCodeHoldingElement parent) {
        this.parent = parent;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return parent.getSuiteFile();
    }

    @Override
    public List<RobotElement> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public ImageDescriptor getImage() {
        return RobotImages.getKeywordImage();
    }

    public List<String> getArguments() {
        return args;
    }
    
    public void setArgs(List<String> args) {
        this.args = args;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public RobotSuiteFileSection getSection() {
        RobotElement current = this;
        while (current != null && !(current instanceof RobotSuiteFileSection)) {
            current = current.getParent();
        }
        return (RobotSuiteFileSection) current;
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), getSection(), this);
    }
}
