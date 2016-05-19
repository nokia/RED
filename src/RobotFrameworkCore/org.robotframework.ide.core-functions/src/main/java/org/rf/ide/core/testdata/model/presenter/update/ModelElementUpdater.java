/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ModelElementUpdater {

    public void updateModelElement(final AModelElement<?> modelElement, final int index, final String value) {
        if (modelElement != null) {
            ModelType modelType = modelElement.getModelType();
            if (modelType == ModelType.SUITE_SETUP) {
                updateSuiteSetupElement(modelElement, index, value);
            } else if (modelType == ModelType.SUITE_TEARDOWN) {
                updateSuiteTeardownElement(modelElement, index, value);
            } else if (modelType == ModelType.SUITE_TEST_SETUP) {
                updateTestSetupElement(modelElement, index, value);
            } else if (modelType == ModelType.SUITE_TEST_TEARDOWN) {
                updateTestTeardownElement(modelElement, index, value);
            } else if (modelType == ModelType.SUITE_TEST_TEMPLATE) {
                updateTestTemplateElement(modelElement, index, value);
            } else if (modelType == ModelType.SUITE_TEST_TIMEOUT) {
                updateTestTimeoutElement(modelElement, index, value);
            } else if (modelType == ModelType.FORCE_TAGS_SETTING) {
                updateForceTagsElement(modelElement, index, value);
            } else if (modelType == ModelType.DEFAULT_TAGS_SETTING) {
                updateDefaultTagsElement(modelElement, index, value);
            } else if (modelType == ModelType.SUITE_DOCUMENTATION) {
                updateDocumentationElement(modelElement, index, value);
            }

            else if (modelType == ModelType.LIBRARY_IMPORT_SETTING) {
                updateLibraryImportElement(modelElement, index, value);
            } else if (modelType == ModelType.RESOURCE_IMPORT_SETTING) {
                updateResourceImportElement(modelElement, index, value);
            } else if (modelType == ModelType.VARIABLES_IMPORT_SETTING) {
                updateVariablesImportElement(modelElement, index, value);
            } else if (modelType == ModelType.METADATA_SETTING) {
                updateMetadataElement(modelElement, index, value);
            }

        }
    }

    public void updateModelElementComment(final AModelElement<?> modelElement, final String comment) {
        updateModelElement(modelElement, -1, comment);
    }
    
    public void removeModelElements(final ARobotSectionTable sectionTable, final List<AModelElement<?>> modelElements) {
        if (sectionTable != null && sectionTable instanceof SettingTable) {
            final SettingTable settingsTable = (SettingTable) sectionTable;
            for (AModelElement<?> modelElement : modelElements) {
                ModelType modelType = modelElement.getModelType();
                if (modelElement instanceof AImported) {
                    settingsTable.removeImported((AImported) modelElement);
                } else if (modelType == ModelType.SUITE_SETUP) {
                    settingsTable.removeSuiteSetup();
                } else if (modelType == ModelType.SUITE_TEARDOWN) {
                    settingsTable.removeSuiteTeardown();
                } else if (modelType == ModelType.SUITE_TEST_SETUP) {
                    settingsTable.removeTestSetup();
                } else if (modelType == ModelType.SUITE_TEST_TEARDOWN) {
                    settingsTable.removeTestTeardown();
                } else if (modelType == ModelType.SUITE_TEST_TEMPLATE) {
                    settingsTable.removeTestTemplate();
                } else if (modelType == ModelType.SUITE_TEST_TIMEOUT) {
                    settingsTable.removeTestTimeout();
                } else if (modelType == ModelType.FORCE_TAGS_SETTING) {
                    settingsTable.removeForceTags();
                } else if (modelType == ModelType.DEFAULT_TAGS_SETTING) {
                    settingsTable.removeDefaultTags();
                } else if (modelType == ModelType.SUITE_DOCUMENTATION) {
                    settingsTable.removeDocumentation();
                } else if (modelType == ModelType.METADATA_SETTING) {
                    settingsTable.removeMetadata((Metadata) modelElement);
                }
            }
        }
    }

    private void updateSuiteSetupElement(final AModelElement<?> modelElement, final int index, final String value) {
        final SuiteSetup suiteSetup = (SuiteSetup) modelElement;
        updateKeywordBaseSetting(suiteSetup, index, value);
    }

    private void updateSuiteTeardownElement(final AModelElement<?> modelElement, final int index, final String value) {
        final SuiteTeardown suiteTeardown = (SuiteTeardown) modelElement;
        updateKeywordBaseSetting(suiteTeardown, index, value);
    }

    private void updateTestSetupElement(final AModelElement<?> modelElement, final int index, final String value) {
        final TestSetup testSetup = (TestSetup) modelElement;
        updateKeywordBaseSetting(testSetup, index, value);
    }

    private void updateTestTeardownElement(final AModelElement<?> modelElement, final int index, final String value) {
        final TestTeardown testTeardown = (TestTeardown) modelElement;
        updateKeywordBaseSetting(testTeardown, index, value);
    }

    private void updateTestTemplateElement(final AModelElement<?> modelElement, final int index, final String value) {
        final TestTemplate testTemplate = (TestTemplate) modelElement;
        if (index == 0) {
            testTemplate.setKeywordName(value);
        } else if (index > 0) {
            testTemplate.setUnexpectedTrashArgument(index - 1, value);
        } else {
            testTemplate.setComment(createRobotToken(value));
        }
    }

    private void updateTestTimeoutElement(final AModelElement<?> modelElement, final int index, final String value) {
        final TestTimeout testTimeout = (TestTimeout) modelElement;
        if (index == 0) {
            testTimeout.setTimeout(createRobotToken(value));
        } else if (index > 0) {
            testTimeout.setMessageArgument(index - 1, value);
        } else {
            testTimeout.setComment(createRobotToken(value));
        }
    }

    private void updateForceTagsElement(final AModelElement<?> modelElement, final int index, final String value) {
        final ForceTags forceTags = (ForceTags) modelElement;
        if (index >= 0) {
            forceTags.setTag(index, value);
        } else {
            forceTags.setComment(createRobotToken(value));
        }
    }

    private void updateDefaultTagsElement(final AModelElement<?> modelElement, final int index, final String value) {
        final DefaultTags defaultTags = (DefaultTags) modelElement;
        if (index >= 0) {
            defaultTags.setTag(index, value);
        } else {
            defaultTags.setComment(createRobotToken(value));
        }
    }

    private void updateDocumentationElement(final AModelElement<?> modelElement, final int index, final String value) {
        final SuiteDocumentation suiteDocumentation = (SuiteDocumentation) modelElement;
        if (index >= 0) {
            suiteDocumentation.setDocumentationText(index, value);
        } else {
            suiteDocumentation.setComment(createRobotToken(value));
        }
    }

    private void updateLibraryImportElement(final AModelElement<?> modelElement, final int index, final String value) {
        final LibraryImport libraryImport = (LibraryImport) modelElement;
        if (index == 0) {
            libraryImport.setPathOrName(value);
        } else if (index > 0) {
            libraryImport.setArguments(index - 1, value);
        } else {
            libraryImport.setComment(createRobotToken(value));
        }
    }

    private void updateResourceImportElement(final AModelElement<?> modelElement, final int index, final String value) {
        final ResourceImport resourceImport = (ResourceImport) modelElement;
        if (index == 0) {
            resourceImport.setPathOrName(value);
        } else if (index > 0) {
            resourceImport.setUnexpectedTrashArguments(index - 1, value);
        } else {
            resourceImport.setComment(createRobotToken(value));
        }
    }

    private void updateVariablesImportElement(final AModelElement<?> modelElement, final int index,
            final String value) {
        final VariablesImport variablesImport = (VariablesImport) modelElement;
        if (index == 0) {
            variablesImport.setPathOrName(value);
        } else if (index > 0) {
            variablesImport.setArguments(index - 1, value);
        } else {
            variablesImport.setComment(createRobotToken(value));
        }
    }

    private void updateMetadataElement(final AModelElement<?> modelElement, final int index, final String value) {
        final Metadata metadata = (Metadata) modelElement;
        if (index == 0) {
            metadata.setKey(value);
        } else if (index > 0) {
            metadata.setValues(index - 1, value);
        } else {
            metadata.setComment(createRobotToken(value));
        }
    }

    private void updateKeywordBaseSetting(final AKeywordBaseSetting<?> setting, final int index, final String value) {
        if (index == 0) {
            setting.setKeywordName(value);
        } else if (index > 0) {
            setting.setArgument(index - 1, value);
        } else {
            setting.setComment(createRobotToken(value));
        }
    }

    private RobotToken createRobotToken(final String text) {
        final RobotToken token = new RobotToken();
        if (text != null) {
            token.setText(text);
        }
        return token;
    }
}
