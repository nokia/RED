/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toCollection;

import java.io.ObjectStreamException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.TestCaseTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
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

        for (final AModelElement<TestCase> el : testCase.getAllElements()) {
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
    public RobotCasesSection getParent() {
        return (RobotCasesSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        final TestCase testCase = getLinkedElement();
        return testCase != null && testCase.isDataDrivenTestCase() ? RedImages.getTemplatedTestCaseImage()
                : RedImages.getTestCaseImage();
    }

    public List<RobotDefinitionSetting> getTagsSetting() {
        return findSettings(ModelType.TEST_CASE_TAGS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeUnitSettings(final RobotKeywordCall call) {
        getLinkedElement().removeElement((AModelElement<TestCase>) call.getLinkedElement());
    }

    public String getDocumentation() {
        return findSetting(ModelType.TEST_CASE_DOCUMENTATION).map(RobotKeywordCall::getLinkedElement)
                .map(TestDocumentation.class::cast)
                .map(DocumentationServiceHandler::toShowConsolidated)
                .orElse("<not documented>");
    }

    public Documentation createDocumentation() {
        // TODO : provide format depending on source
        final Set<String> keywords = getSuiteFile().getUserDefinedKeywords()
                .stream()
                .map(RobotKeywordDefinition::getName)
                .collect(toCollection(HashSet::new));
        return new Documentation(DocFormat.ROBOT, getDocumentation(), keywords);
    }
    
    public Optional<String> getTemplateInUse() {
        return Optional.ofNullable(getLinkedElement().getTemplateKeywordName());
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
