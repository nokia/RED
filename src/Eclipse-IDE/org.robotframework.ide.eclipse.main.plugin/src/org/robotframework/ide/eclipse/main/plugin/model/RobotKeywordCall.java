/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Optional;

public class RobotKeywordCall implements RobotFileInternalElement, Serializable {

    private static final long serialVersionUID = 3968389012402369728L;
    
    private String name;
    private List<String> args;
    private String comment;
    private transient IRobotCodeHoldingElement parent;
    // TODO : fix this stuff for serialization
    private AModelElement<?> linkedElement;

    RobotKeywordCall(final IRobotCodeHoldingElement parent, final String name, final List<String> args,
            final String comment) {
        this.parent = parent;
        this.name = name;
        this.args = args;
        this.comment = comment;
    }

    public void link(final AModelElement<?> executableRow) {
        this.linkedElement = executableRow;
    }

    @Override
    public AModelElement<?> getLinkedElement() {
        return linkedElement;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getLabel() {
        RobotToken token;
        if (linkedElement instanceof RobotExecutableRow<?>) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) linkedElement;
            token = row.buildLineDescription().getAction().getToken();
        } else {
            token = linkedElement.getElementTokens().get(0);
        }
        return token.getText().toString();
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
        return RedImages.getKeywordImage();
    }

    public List<String> getArguments() {
        return args;
    }
    
    public void setArgs(final List<String> args) {
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
    public Position getPosition() {
        return new Position(0);
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        RobotToken token;
        if (linkedElement instanceof RobotExecutableRow<?>) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) linkedElement;
            token = row.buildLineDescription().getAction().getToken();
        } else {
            token = linkedElement.getElementTokens().get(0);
        }

        if (token.getFilePosition().isNotSet()) {
            token = linkedElement.getElementTokens().get(0);
        }

        return new DefinitionPosition(token.getFilePosition(), token.getText().length());
    }

    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        if (!linkedElement.getBeginPosition().isNotSet() && linkedElement.getBeginPosition().getOffset() <= offset
                && offset <= linkedElement.getEndPosition().getOffset()) {
            return Optional.of(this);
        }
        return Optional.absent();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), getSection(), this);
    }
}
