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
import org.rf.ide.core.testdata.model.ModelType;

import com.google.common.base.Optional;

public abstract class RobotCodeHoldingElement implements IRobotCodeHoldingElement, Serializable {

    private static final long serialVersionUID = -3138118535388297030L;

    private transient RobotSuiteFileSection parent;

    private final List<RobotKeywordCall> calls = newArrayList();

    RobotCodeHoldingElement(final RobotSuiteFileSection parent) {
        this.parent = parent;
    }

    public abstract RobotKeywordCall createKeywordCall(final int index, final String name, final List<String> args,
            final String comment);

    public abstract void insertKeywordCall(final int index, final RobotKeywordCall keywordCall);

    public abstract RobotDefinitionSetting createSetting(final int index, final String name, final List<String> args,
            final String comment);

    @Override
    public String getComment() {
        return "";
    }

    @Override
    public RobotSuiteFileSection getParent() {
        return parent;
    }

    public void setParent(final RobotSuiteFileSection parent) {
        this.parent = parent;
    }

    @Override
    public List<RobotKeywordCall> getChildren() {
        return calls;
    }

    @Override
    public int getIndex() {
        return parent == null ? -1 : parent.getChildren().indexOf(this);
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
    
    protected int countRowsOfTypeUpTo(final ModelType type, final int toIndex) {
        int index = 0;
        int count = 0;
        for (final RobotKeywordCall call : getChildren()) {
            if (index >= toIndex) {
                break;
            }
            if (call.getLinkedElement().getModelType() == type) {
                count++;
            }
            index++;
        }
        return count;
    }
    
    public RobotDefinitionSetting findSetting(final String name) {
        for (final RobotKeywordCall call : getChildren()) {
            if (call instanceof RobotDefinitionSetting && call.getName().equalsIgnoreCase(name)) {
                return (RobotDefinitionSetting) call;
            }
        }
        return null;
    }
}
