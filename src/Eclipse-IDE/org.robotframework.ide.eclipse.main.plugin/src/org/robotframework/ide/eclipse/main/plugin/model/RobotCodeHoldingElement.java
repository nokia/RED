/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.ui.IWorkbenchPage;

import com.google.common.base.Optional;

public abstract class RobotCodeHoldingElement implements IRobotCodeHoldingElement, Serializable {

    private static final long serialVersionUID = -3138118535388297030L;

    private transient RobotSuiteFileSection parent;

    private String name;
    private String comment;

    private final List<RobotKeywordCall> calls = newArrayList();

    RobotCodeHoldingElement(final RobotSuiteFileSection parent, final String name, final String comment) {
        this.parent = parent;
        this.name = name;
        this.comment = comment;
    }

    public RobotKeywordCall createKeywordCall() {
        return createKeywordCall(-1, -1);
    }

    public abstract RobotKeywordCall createKeywordCall(final int modelTableIndex, final int codeHoldingElementIndex);
    
    public abstract void insertKeywordCall(final int modelTableIndex, final int codeHoldingElementIndex, final RobotKeywordCall keywordCall);

    public RobotDefinitionSetting createDefinitionSetting(final String name, final List<String> args,
            final String comment) {
        return createDefinitionSetting(getChildren().size(), name, args, comment);
    }

    public RobotDefinitionSetting createDefinitionSetting(final int index, final String name, final List<String> args,
            final String comment) {
        final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, name, args, comment);
        getChildren().add(index, setting);
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
    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    @Override
    public RobotSuiteFileSection getParent() {
        return parent;
    }

    public void setParent(final RobotSuiteFileSection parent) {
        this.parent = parent;
    }

    public void fixParents() {
        for (final RobotKeywordCall call : calls) {
            call.fixParents(this);
        }
    }

    @Override
    public List<RobotKeywordCall> getChildren() {
        return calls;
    }

    @Override
    public abstract Position getPosition();

    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        for (final RobotKeywordCall element : calls) {
            final Optional<? extends RobotElement> candidate = element.findElement(offset);
            if (candidate.isPresent()) {
                return candidate;
            }
        }
        final Position position = getPosition();
        if (position.getOffset() <= offset && offset <= position.getOffset() + position.getLength()) {
            return Optional.of(this);
        }
        return Optional.absent();
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return getParent().getSuiteFile();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy(final IWorkbenchPage page) {
        return new PageActivatingOpeningStrategy(page, getSuiteFile().getFile(), getParent(), this);
    }

}
