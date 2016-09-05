/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.Position;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;

public abstract class RobotCodeHoldingElement<T extends AModelElement<?>>
        implements IRobotCodeHoldingElement, Serializable {

    private static final long serialVersionUID = 1L;

    private transient RobotSuiteFileSection parent;

    private final T linkedElement;

    private final List<RobotKeywordCall> calls = newArrayList();

    RobotCodeHoldingElement(final RobotSuiteFileSection parent, final T linkedElement) {
        this.parent = parent;
        this.linkedElement = linkedElement;
    }

    public abstract IExecutablesTableModelUpdater<T> getModelUpdater();

    public RobotKeywordCall createKeywordCall(final int index, final String name, final List<String> args,
            final String comment) {
        final int modelIndex = countRowsOfTypeUpTo(getExecutableRowModelType(), index);

        final RobotExecutableRow<?> robotExecutableRow = (RobotExecutableRow<?>) getModelUpdater()
                .createExecutableRow(getLinkedElement(), modelIndex, name, comment, args);

        final RobotKeywordCall call = new RobotKeywordCall(this, robotExecutableRow);
        getChildren().add(index, call);
        return call;
    }

    public RobotDefinitionSetting createSetting(final int index, final String settingName, final List<String> args,
            final String comment) {
        final AModelElement<?> newModelElement = getModelUpdater().createSetting(getLinkedElement(), settingName,
                comment, args);

        final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, newModelElement);
        getChildren().add(index, setting);

        return setting;
    }

    public void insertKeywordCall(final int index, final RobotKeywordCall call) {
        call.setParent(this);

        final int modelIndex = countRowsOfTypeUpTo(getExecutableRowModelType(), index);
        if (index == -1) {
            getChildren().add(call);
        } else {
            getChildren().add(index, call);
        }
        getModelUpdater().insert(getLinkedElement(), modelIndex, call.getLinkedElement());
    }

    @Override
    public void removeChild(final RobotKeywordCall child) {
        getChildren().remove(child);
        getModelUpdater().remove(getLinkedElement(), child.getLinkedElement());
    }

    public abstract void moveChildDown(final RobotKeywordCall keywordCall);

    public abstract void moveChildUp(final RobotKeywordCall keywordCall);

    protected abstract ModelType getExecutableRowModelType();

    public abstract RobotTokenType getSettingDeclarationTokenTypeFor(final String name);

    @Override
    public String getName() {
        return getLinkedElement().getDeclaration().getText();
    }

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
    public T getLinkedElement() {
        return linkedElement;
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
    public Position getPosition() {
        final FilePosition begin = linkedElement.getBeginPosition();
        final FilePosition end = linkedElement.getEndPosition();

        if (begin.isNotSet() || end.isNotSet()) {
            return new Position(0, 0);
        }
        return new Position(begin.getOffset(), end.getOffset() - begin.getOffset());
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        return new DefinitionPosition(linkedElement.getDeclaration().getFilePosition(),
                linkedElement.getDeclaration().getText().length());
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
    
    public RobotDefinitionSetting findSetting(final ModelType... modelTypes) {
        final List<RobotDefinitionSetting> settings = findSettings(modelTypes);
        return settings.isEmpty() ? null : settings.get(0);
    }

    protected final List<RobotDefinitionSetting> findSettings(final ModelType... modelTypes) {
        final Set<ModelType> typesToFind = newHashSet(modelTypes);
        final List<RobotDefinitionSetting> matchingSettings = new ArrayList<>();
        for (final RobotKeywordCall call : getChildren()) {
            if (call instanceof RobotDefinitionSetting
                    && typesToFind.contains(call.getLinkedElement().getModelType())) {
                matchingSettings.add((RobotDefinitionSetting) call);
            }
        }
        return matchingSettings;
    }
}
