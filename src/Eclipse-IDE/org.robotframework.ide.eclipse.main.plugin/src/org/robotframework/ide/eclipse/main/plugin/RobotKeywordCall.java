package org.robotframework.ide.eclipse.main.plugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IWorkbenchPage;

public abstract class RobotKeywordCall implements RobotElement {

    private final RobotElement parent;
    private final String name;
    private final List<String> args;
    private String comment;

    RobotKeywordCall(final RobotElement parent, final String name, final List<String> args, final String comment) {
        this.parent = parent;
        this.name = name;
        this.args = args;
        this.comment = comment;
    }

    @Override
    public String getName() {
        return name;
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
        return new ArrayList<>();
    }

    public List<String> getArguments() {
        return args;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new OpenStrategy();
    }
}
