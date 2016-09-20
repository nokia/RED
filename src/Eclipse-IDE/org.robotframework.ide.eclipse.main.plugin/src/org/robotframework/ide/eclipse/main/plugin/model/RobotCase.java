/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IExecutablesTableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.TestCaseTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
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

        final List<PrioriterizedCaseSettings> settings = newArrayList(EnumSet.allOf(PrioriterizedCaseSettings.class));
        Collections.sort(settings);

        // settings
        for (final PrioriterizedCaseSettings setting : settings) {
            for (final AModelElement<TestCase> element : setting.getModelElements(testCase)) {
                getChildren().add(new RobotDefinitionSetting(this, element));
            }
        }

        // executables
        for (final RobotExecutableRow<TestCase> execRow : testCase.getTestExecutionRows()) {
            getChildren().add(new RobotKeywordCall(this, execRow));
        }
    }

    @Override
    public void fixChildrenOrder() {
        Collections.sort(getChildren(), new Comparator<RobotKeywordCall>() {

            @Override
            public int compare(final RobotKeywordCall call1, final RobotKeywordCall call2) {
                if (call1 instanceof RobotDefinitionSetting && call2 instanceof RobotDefinitionSetting) {
                    final ModelType modelType1 = call1.getLinkedElement().getModelType();
                    final ModelType modelType2 = call2.getLinkedElement().getModelType();
                    return PrioriterizedCaseSettings.from(modelType1).compareTo(PrioriterizedCaseSettings.from(modelType2));
                } else if (call1 instanceof RobotDefinitionSetting && !(call2 instanceof RobotDefinitionSetting)) {
                    return -1;
                } else if (!(call1 instanceof RobotDefinitionSetting) && call2 instanceof RobotDefinitionSetting) {
                    return 1;
                } else {
                    // sort is stable so we don't care about those cases
                    return 0;
                }
            }
        });
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

    static enum PrioriterizedCaseSettings {
        // the order is defined by enums definitions order (see Enum.compareTo() method javadoc)
        DOCUMENTATION,
        TAGS,
        SETUP,
        TEARDOWN,
        TEMPLATE,
        TIMEOUT,
        UNKNOWN;

        public List<? extends AModelElement<TestCase>> getModelElements(final TestCase testCase) {
            switch (this) {
                case DOCUMENTATION: return testCase.getDocumentation();
                case TAGS: return testCase.getTags();
                case SETUP: return testCase.getSetups();
                case TEARDOWN: return testCase.getTeardowns();
                case TEMPLATE: return testCase.getTemplates();
                case TIMEOUT: return testCase.getTimeouts();
                case UNKNOWN: return testCase.getUnknownSettings();
                default: return new ArrayList<>();
            }
        }

        public static PrioriterizedCaseSettings from(final ModelType modelType) {
            switch (modelType) {
                case TEST_CASE_DOCUMENTATION: return DOCUMENTATION;
                case TEST_CASE_TAGS: return TAGS;
                case TEST_CASE_SETUP: return SETUP;
                case TEST_CASE_TEARDOWN: return TEARDOWN;
                case TEST_CASE_TEMPLATE: return TEMPLATE;
                case TEST_CASE_TIMEOUT: return TIMEOUT;
                case TEST_CASE_SETTING_UNKNOWN: return UNKNOWN;
                default:
                    throw new IllegalArgumentException("No setting defined for " + modelType.name());
            }
        }
    }
}
