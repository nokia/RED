/*
 * Copyright 2015 Nokia Solutions and Networks
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
import org.rf.ide.core.testdata.model.presenter.update.TestCaseTableModelUpdater;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RobotCase extends RobotCodeHoldingElement<TestCase> {

    private static final long serialVersionUID = 1L;

    public RobotCase(final RobotCasesSection parent, final TestCase testCase) {
        super(parent, testCase);
    }

    @Override
    public IExecutablesTableModelUpdater<TestCase> getModelUpdater() {
        return new TestCaseTableModelUpdater();
    }

    @Override
    protected ModelType getExecutableRowModelType() {
        return ModelType.TEST_CASE_EXECUTABLE_ROW;
    }

    @Override
    public RobotTokenType getSettingDeclarationTokenTypeFor(final String name) {
        return RobotTokenType.findTypeOfDeclarationForTestCaseSettingTable(name);
    }

    public void link() {
        final TestCase testCase = getLinkedElement();

        for (final AModelElement<TestCase> el : testCase.getElements()) {
            getChildren().add(new RobotKeywordCall(this, el));
        }
    }

    @Override
    public RobotCasesSection getParent() {
        return (RobotCasesSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        final TestCase testCase = getLinkedElement();
        return testCase != null && testCase.getTemplateKeywordName().isPresent() ? RedImages.getTemplatedTestCaseImage()
                : RedImages.getTestCaseImage();
    }

    public String getDocumentation() {
        return findSetting(ModelType.TEST_CASE_DOCUMENTATION).map(RobotKeywordCall::getLinkedElement)
                .map(setting -> (LocalSetting<?>) setting)
                .map(setting -> setting.adaptTo(IDocumentationHolder.class))
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
        return getLinkedElement().getTemplateKeywordName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void moveChildDown(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index + 1);
        getLinkedElement().moveElementDown((AModelElement<TestCase>) keywordCall.getLinkedElement());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void moveChildUp(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index - 1);
        getLinkedElement().moveElementUp((AModelElement<TestCase>) keywordCall.getLinkedElement());
    }

    @SuppressWarnings("unchecked")
    private Object readResolve() throws ObjectStreamException {
        // after deserialization we fix parent relationship in direct children
        for (final RobotKeywordCall call : getChildren()) {
            ((AModelElement<TestCase>) call.getLinkedElement()).setParent(getLinkedElement());
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
