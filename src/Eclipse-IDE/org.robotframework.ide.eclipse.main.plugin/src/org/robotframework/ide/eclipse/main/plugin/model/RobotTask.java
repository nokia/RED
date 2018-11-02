/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toSet;

import java.io.ObjectStreamException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.TaskTableModelUpdater;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RobotTask extends RobotCodeHoldingElement<Task> {

    private static final long serialVersionUID = 1L;

    public RobotTask(final RobotTasksSection parent, final Task task) {
        super(parent, task);
    }

    @Override
    public IExecutablesTableModelUpdater<Task> getModelUpdater() {
        return new TaskTableModelUpdater();
    }

    @Override
    protected ModelType getExecutableRowModelType() {
        return ModelType.TASK_EXECUTABLE_ROW;
    }

    @Override
    public RobotTokenType getSettingDeclarationTokenTypeFor(final String name) {
        return RobotTokenType.findTypeOfDeclarationForTaskSettingTable(name);
    }

    public void link() {
        final Task task = getLinkedElement();

        for (final AModelElement<Task> el : task.getAllElements()) {
            if (el instanceof RobotExecutableRow) {
                getChildren().add(new RobotKeywordCall(this, el));
            } else if (el instanceof RobotEmptyRow) {
                getChildren().add(new RobotEmptyLine(this, el));
            } else {
                getChildren().add(new RobotDefinitionSetting(this, el));
            }
        }
    }

    @Override
    public RobotTasksSection getParent() {
        return (RobotTasksSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        final Task task = getLinkedElement();
        return task != null && task.isDataDrivenTask() ? RedImages.getTemplatedRpaTaskImage()
                : RedImages.getRpaTaskImage();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeUnitSettings(final RobotKeywordCall call) {
        getLinkedElement().removeElement((AModelElement<Task>) call.getLinkedElement());
    }

    public String getDocumentation() {
        return findSetting(ModelType.TASK_DOCUMENTATION).map(RobotKeywordCall::getLinkedElement)
                .map(elem -> (LocalSetting<?>) elem)
                .map(docSetting -> docSetting.adaptTo(IDocumentationHolder.class))
                .map(DocumentationServiceHandler::toShowConsolidated)
                .orElse("<not documented>");
    }

    public Documentation createDocumentation() {
        // TODO : provide format depending on source
        final Set<String> keywords = getSuiteFile().getUserDefinedKeywords()
                .stream()
                .map(RobotKeywordDefinition::getName)
                .collect(toSet());
        return new Documentation(DocFormat.ROBOT, getDocumentation(), keywords);
    }

    @Override
    public Optional<String> getTemplateInUse() {
        return Optional.ofNullable(getLinkedElement().getTemplateKeywordName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void moveChildDown(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index + 1);
        getLinkedElement().moveElementDown((AModelElement<Task>) keywordCall.getLinkedElement());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void moveChildUp(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index - 1);
        getLinkedElement().moveElementUp((AModelElement<Task>) keywordCall.getLinkedElement());
    }

    @SuppressWarnings("unchecked")
    private Object readResolve() throws ObjectStreamException {
        // after deserialization we fix parent relationship in direct children
        for (final RobotKeywordCall call : getChildren()) {
            ((AModelElement<Task>) call.getLinkedElement()).setParent(getLinkedElement());
            call.setParent(this);
        }
        return this;
    }

    @Override
    public String toString() {
        // for debugging purposes only
        return getName();
    }
}
