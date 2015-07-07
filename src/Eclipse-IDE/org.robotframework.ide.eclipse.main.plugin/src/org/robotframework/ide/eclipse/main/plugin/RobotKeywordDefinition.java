package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public class RobotKeywordDefinition implements RobotElement, Serializable {

    private transient RobotKeywordsSection parent;
    private String name;
    protected final List<RobotElement> elements = new ArrayList<>();
    private String comment;
    private final List<String> arguments;

    public RobotKeywordDefinition(final RobotKeywordsSection parent, final String name, final List<String> arguments,
            final String comment) {
        this.parent = parent;
        this.name = name;
        this.arguments = arguments;
        this.comment = comment;
    }

    RobotKeywordCall createKeywordCall(final String name, final String[] args, final String comment) {
        final RobotKeywordCall call = new RobotKeywordCall(this, name, newArrayList(args), comment);
        elements.add(call);
        return call;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String newName) {
        this.name = newName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public RobotElement getParent() {
        return parent;
    }

    public void setParent(final RobotKeywordsSection parent) {
        this.parent = parent;
    }

    @Override
    public void fixParents(final RobotElement parent) {
        this.parent = (RobotKeywordsSection) parent;

        for (final RobotElement element : elements) {
            element.fixParents(this);
        }
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
        return RobotImages.getUserKeywordImage();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), (RobotSuiteFileSection) getParent(),
                this);
    }

}
