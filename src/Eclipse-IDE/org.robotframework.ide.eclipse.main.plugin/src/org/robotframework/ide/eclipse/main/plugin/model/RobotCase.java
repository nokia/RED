/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.io.ObjectStreamException;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.TestCaseTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTemplate;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTimeout;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Optional;

public class RobotCase extends RobotCodeHoldingElement<TestCase> {

    private static final long serialVersionUID = 1L;

    RobotCase(final RobotCasesSection parent, final TestCase testCase) {
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
        // settings
        for (final TestDocumentation documentation : testCase.getDocumentation()) {
            getChildren().add(new RobotDefinitionSetting(this, documentation));
        }
        for (final TestCaseTags tags : testCase.getTags()) {
            getChildren().add(new RobotDefinitionSetting(this, tags));
        }
        for (final TestCaseSetup setup : testCase.getSetups()) {
            getChildren().add(new RobotDefinitionSetting(this, setup));
        }
        for (final TestCaseTeardown teardown : testCase.getTeardowns()) {
            getChildren().add(new RobotDefinitionSetting(this, teardown));
        }
        for (final TestCaseTemplate template : testCase.getTemplates()) {
            getChildren().add(new RobotDefinitionSetting(this, template));
        }
        for (final TestCaseTimeout timeout : testCase.getTimeouts()) {
            getChildren().add(new RobotDefinitionSetting(this, timeout));
        }
        for (final TestCaseUnknownSettings unknown : testCase.getUnknownSettings()) {
            getChildren().add(new RobotDefinitionSetting(this, unknown));
        }
        // executables
        for (final RobotExecutableRow<TestCase> execRow : testCase.getTestExecutionRows()) {
            getChildren().add(new RobotKeywordCall(this, execRow));
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
    
    public Optional<String> getTemplateInUse() {
        return Optional.fromNullable(getLinkedElement().getTemplateKeywordName());
    }

    @Override
    public void moveChildDown(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index + 1);

        @SuppressWarnings("unchecked")
        final RobotExecutableRow<TestCase> linkedCall = (RobotExecutableRow<TestCase>) keywordCall.getLinkedElement();
        getLinkedElement().moveDownExecutableRow(linkedCall);
    }

    @Override
    public void moveChildUp(final RobotKeywordCall keywordCall) {
        final int index = keywordCall.getIndex();
        Collections.swap(getChildren(), index, index - 1);

        @SuppressWarnings("unchecked")
        final RobotExecutableRow<TestCase> linkedCall = (RobotExecutableRow<TestCase>) keywordCall.getLinkedElement();
        getLinkedElement().moveUpExecutableRow(linkedCall);
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
