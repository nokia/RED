/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
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
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class RobotCase extends RobotCodeHoldingElement {

    private static final long serialVersionUID = 1L;

    private final TestCase testCase;

    RobotCase(final RobotCasesSection parent, final TestCase testCase) {
        super(parent);
        this.testCase = testCase;
    }

    @Override
    public TestCase getLinkedElement() {
        return testCase;
    }

    @Override
    public String getName() {
        return testCase.getDeclaration().getText();
    }

    public void link() {
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
    public RobotKeywordCall createKeywordCall(final int index, final String name, final List<String> args,
            final String comment) {
        final int modelIndex = countRowsOfTypeUpTo(ModelType.TEST_CASE_EXECUTABLE_ROW, index);

        @SuppressWarnings("unchecked")
        final RobotExecutableRow<TestCase> robotExecutableRow = (RobotExecutableRow<TestCase>) new TestCaseTableModelUpdater()
                .createExecutableRow(getLinkedElement(), modelIndex, name, comment, args);

        final RobotKeywordCall call = new RobotKeywordCall(this, robotExecutableRow);
        getChildren().add(index, call);
        return call;
    }

    @Override
    public RobotDefinitionSetting createSetting(final int index, final String settingName, final List<String> args,
            final String comment) {
        final AModelElement<?> newModelElement = new TestCaseTableModelUpdater().createSetting(getLinkedElement(),
                settingName, comment, args);

        final RobotDefinitionSetting setting = new RobotDefinitionSetting(this, newModelElement);

        getChildren().add(index, setting);

        return setting;
    }

    @Override
    public void insertKeywordCall(final int index, final RobotKeywordCall call) {
        call.setParent(this);

        final int modelIndex = countRowsOfTypeUpTo(ModelType.TEST_CASE_EXECUTABLE_ROW, index);
        if (index == -1) {
            getChildren().add(call);
        } else {
            getChildren().add(index, call);
        }
        new TestCaseTableModelUpdater().insert(testCase, modelIndex, call.getLinkedElement());
    }

    public void removeChild(final RobotKeywordCall child) {
        getChildren().remove(child);
        new TestCaseTableModelUpdater().remove(testCase, child.getLinkedElement());
    }

    @Override
    public RobotCasesSection getParent() {
        return (RobotCasesSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        return testCase != null && testCase.isDataDrivenTestCase() ? RedImages.getTemplatedTestCaseImage()
                : RedImages.getTestCaseImage();
    }

    public List<RobotDefinitionSetting> getTagsSetting() {
        return findSettings(ModelType.TEST_CASE_TAGS);
    }
    
    public List<RobotDefinitionSetting> getDocumentationSetting() {
        return findSettings(ModelType.TEST_CASE_DOCUMENTATION);
    }

    private List<RobotDefinitionSetting> findSettings(final ModelType modelType) {
        final List<RobotDefinitionSetting> matchingSettings = new ArrayList<>();
        for (final RobotKeywordCall call : getChildren()) {
            if (call instanceof RobotDefinitionSetting && call.getLinkedElement().getModelType() == modelType) {
                matchingSettings.add((RobotDefinitionSetting) call);
            }
        }
        return matchingSettings;
    }

    public Optional<String> getTemplateInUse() {
        return Optional.fromNullable(testCase.getTemplateKeywordName());
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        return new DefinitionPosition(testCase.getTestName().getFilePosition(),
                testCase.getTestName().getText().length());
    }

    @Override
    public Position getPosition() {
        if (testCase != null) {
            final FilePosition begin = testCase.getBeginPosition();
            final FilePosition end = testCase.getEndPosition();
            return new Position(begin.getOffset(), end.getOffset() - begin.getOffset());
        }
        return new Position(0);
    }

    public int findExecutableRowIndex(final RobotKeywordCall call) {
        return getExecutableRows(0, getChildren().size()).indexOf(call);
    }

    public List<RobotKeywordCall> getExecutableRows(final int from, final int to) {
        return newArrayList(filter(getChildren().subList(from, to), new Predicate<RobotKeywordCall>() {
            @Override
            public boolean apply(final RobotKeywordCall call) {
                return call.getLinkedElement().getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW;
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private Object readResolve() throws ObjectStreamException {
        // after deserialization we fix parent relationship in direct children
        for (final RobotKeywordCall call : getChildren()) {
            ((AModelElement<TestCase>) call.getLinkedElement()).setParent(testCase);
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
