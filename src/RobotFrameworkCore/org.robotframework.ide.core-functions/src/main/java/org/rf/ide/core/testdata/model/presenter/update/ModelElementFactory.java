/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ModelElementFactory {

    public AModelElement<?> createNewSettingsElement(final ARobotSectionTable sectionTable, final String settingName,
            final String comment, final List<String> args) {

        if (sectionTable != null && sectionTable instanceof SettingTable) {
            final SettingTable settingsTable = (SettingTable) sectionTable;
            final RobotTokenType settingType = RobotTokenType.findTypeOfDeclarationForSettingTable(settingName);
            if (settingType == RobotTokenType.SETTING_SUITE_SETUP_DECLARATION) {
                return createNewSuiteSetupElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION) {
                return createNewSuiteTeardownElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_TEST_SETUP_DECLARATION) {
                return createNewTestSetupElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION) {
                return createNewTestTeardownElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION) {
                return createNewTestTemplateElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION) {
                return createNewTestTimeoutElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_FORCE_TAGS_DECLARATION) {
                return createNewForceTagsElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION) {
                return createNewDefaultTagsElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_DOCUMENTATION_DECLARATION) {
                return createNewDocumentationElement(settingsTable, args, comment);
            } 
            
            else if (settingType == RobotTokenType.SETTING_LIBRARY_DECLARATION) {
                return createNewLibraryElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_RESOURCE_DECLARATION) {
                return createNewResourceElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_VARIABLES_DECLARATION) {
                return createNewVariablesElement(settingsTable, args, comment);
            } else if (settingType == RobotTokenType.SETTING_METADATA_DECLARATION) {
                return createNewMetadataElement(settingsTable, args, comment);
            }
        }

        return null;
    }

    private AModelElement<?> createNewSuiteSetupElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        return setupNewKeywordBaseElement(settingsTable, settingsTable.newSuiteSetup(), args, comment);
    }

    private AModelElement<?> createNewSuiteTeardownElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        return setupNewKeywordBaseElement(settingsTable, settingsTable.newSuiteTeardown(), args, comment);
    }

    private AModelElement<?> createNewTestSetupElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        return setupNewKeywordBaseElement(settingsTable, settingsTable.newTestSetup(), args, comment);
    }

    private AModelElement<?> createNewTestTeardownElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        return setupNewKeywordBaseElement(settingsTable, settingsTable.newTestTeardown(), args, comment);
    }

    private AModelElement<?> createNewTestTemplateElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final TestTemplate newTestTemplate = settingsTable.newTestTemplate();
        if (!args.isEmpty()) {
            newTestTemplate.setKeywordName(createRobotToken(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            newTestTemplate.addUnexpectedTrashArgument(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newTestTemplate.addCommentPart(createRobotToken(comment));
        }
        return newTestTemplate;
    }

    private AModelElement<?> createNewTestTimeoutElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final TestTimeout newTestTimeout = settingsTable.newTestTimeout();
        if (!args.isEmpty()) {
            newTestTimeout.setTimeout(createRobotToken(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            newTestTimeout.addMessageArgument(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newTestTimeout.addCommentPart(createRobotToken(comment));
        }
        return newTestTimeout;
    }

    private AModelElement<?> createNewForceTagsElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final ForceTags newForceTags = settingsTable.newForceTag();

        for (int i = 0; i < args.size(); i++) {
            newForceTags.addTag(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newForceTags.addCommentPart(createRobotToken(comment));
        }
        return newForceTags;
    }

    private AModelElement<?> createNewDefaultTagsElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final DefaultTags newDefaultTags = settingsTable.newDefaultTag();

        for (int i = 0; i < args.size(); i++) {
            newDefaultTags.addTag(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newDefaultTags.addCommentPart(createRobotToken(comment));
        }
        return newDefaultTags;
    }
    
    private AModelElement<?> createNewDocumentationElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final SuiteDocumentation newSuiteDocumentation = settingsTable.newSuiteDocumentation();
        for (int i = 0; i < args.size(); i++) {
            newSuiteDocumentation.addDocumentationText(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newSuiteDocumentation.addCommentPart(createRobotToken(comment));
        }
        return newSuiteDocumentation;
    }
    
    private AModelElement<?> createNewMetadataElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final Metadata newMetadata = settingsTable.newMetadata();
        if (!args.isEmpty()) {
            newMetadata.setKey(createRobotToken(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            newMetadata.addValue(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newMetadata.addCommentPart(createRobotToken(comment));
        }
        return newMetadata;
    }
    
    private AModelElement<?> createNewLibraryElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final LibraryImport newLibraryImport = settingsTable.newLibraryImport();
        if (!args.isEmpty()) {
            newLibraryImport.setPathOrName(createRobotToken(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            newLibraryImport.addArgument(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newLibraryImport.addCommentPart(createRobotToken(comment));
        }
        return newLibraryImport;
    }
    
    private AModelElement<?> createNewResourceElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final ResourceImport newResourceImport = settingsTable.newResourceImport();
        if (!args.isEmpty()) {
            newResourceImport.setPathOrName(createRobotToken(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            newResourceImport.addUnexpectedTrashArgument(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newResourceImport.addCommentPart(createRobotToken(comment));
        }
        return newResourceImport;
    }
    
    private AModelElement<?> createNewVariablesElement(final SettingTable settingsTable, final List<String> args,
            final String comment) {
        final VariablesImport newVariablesImport = settingsTable.newVariablesImport();
        if (!args.isEmpty()) {
            newVariablesImport.setPathOrName(createRobotToken(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            newVariablesImport.addArgument(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newVariablesImport.addCommentPart(createRobotToken(comment));
        }
        return newVariablesImport;
    }

    private AModelElement<?> setupNewKeywordBaseElement(final SettingTable settingsTable,
            AKeywordBaseSetting<SettingTable> newKeywordBaseSetting, final List<String> args, final String comment) {
        if (!args.isEmpty()) {
            newKeywordBaseSetting.setKeywordName(createRobotToken(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            newKeywordBaseSetting.addArgument(createRobotToken(args.get(i)));
        }
        if (comment != null && !comment.isEmpty()) {
            newKeywordBaseSetting.addCommentPart(createRobotToken(comment));
        }
        return newKeywordBaseSetting;
    }

    private RobotToken createRobotToken(final String text) {
        final RobotToken token = new RobotToken();
        if (text != null) {
            token.setText(text);
        }
        return token;
    }
}
