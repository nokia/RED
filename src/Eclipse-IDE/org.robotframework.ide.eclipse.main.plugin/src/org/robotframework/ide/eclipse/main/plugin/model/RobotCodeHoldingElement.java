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

    private final List<RobotKeywordCall> calls = newArrayList();

    RobotCodeHoldingElement(final RobotSuiteFileSection parent) {
        this.parent = parent;
    }

    public RobotKeywordCall createKeywordCall() {
        return createKeywordCall("", -1, -1);
    }

    public abstract RobotKeywordCall createKeywordCall(String callName, int modelTableIndex,
            final int codeHoldingElementIndex);

    public abstract void insertKeywordCall(final int modelTableIndex, final int codeHoldingElementIndex,
            final RobotKeywordCall keywordCall);

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
