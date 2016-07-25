/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RobotSettingsSection extends RobotSuiteFileSection implements IRobotCodeHoldingElement {

    public static final String SECTION_NAME = "Settings";

    private final SettingTableModelUpdater settingTableModelUpdater;

    RobotSettingsSection(final RobotSuiteFile parent, final SettingTable settingTable) {
        super(parent, SECTION_NAME, settingTable);
        settingTableModelUpdater = new SettingTableModelUpdater();
    }

    @Override
    public void link() {
        final SettingTable settingsTable = getLinkedElement();

        for (final Metadata metadataSetting : settingsTable.getMetadatas()) {
            final String name = metadataSetting.getDeclaration().getText().toString();
            final RobotToken metadataKey = metadataSetting.getKey();
            final List<String> args = newArrayList();
            if (metadataKey != null) {
                args.add(metadataKey.getText().toString());
            }
            args.addAll(Lists.transform(metadataSetting.getValues(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(metadataSetting,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotSetting setting = new RobotSetting(this, SettingsGroup.METADATA, name, args, comment);
            setting.link(metadataSetting);
            elements.add(setting);
        }
        for (final AImported importSetting : settingsTable.getImports()) {
            final String comment = CommentServiceHandler.consolidate(importSetting,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            if (importSetting instanceof LibraryImport) {

                final LibraryImport libraryImport = (LibraryImport) importSetting;

                final String name = libraryImport.getDeclaration().getText().toString();
                final RobotToken pathOrName = libraryImport.getPathOrName();
                final List<String> args = newArrayList();
                if (pathOrName != null) {
                    args.add(pathOrName.getText().toString());
                }
                args.addAll(Lists.transform(libraryImport.getArguments(), TokenFunctions.tokenToString()));
                args.addAll(
                        Lists.transform(libraryImport.getAlias().getElementTokens(), TokenFunctions.tokenToString()));
                final RobotSetting setting = new RobotSetting(this, SettingsGroup.LIBRARIES, name, args, comment);
                setting.link(libraryImport);
                elements.add(setting);
            } else if (importSetting instanceof ResourceImport) {

                final ResourceImport resourceImport = (ResourceImport) importSetting;

                final String name = resourceImport.getDeclaration().getText().toString();
                final RobotToken pathOrName = resourceImport.getPathOrName();
                final List<String> args = newArrayList();
                if (pathOrName != null) {
                    args.add(pathOrName.getText().toString());
                }

                final RobotSetting setting = new RobotSetting(this, SettingsGroup.RESOURCES, name, args, comment);
                setting.link(resourceImport);
                elements.add(setting);
            } else if (importSetting instanceof VariablesImport) {

                final VariablesImport variablesImport = (VariablesImport) importSetting;

                final String name = variablesImport.getDeclaration().getText().toString();
                final RobotToken pathOrName = variablesImport.getPathOrName();
                final List<String> args = newArrayList();
                if (pathOrName != null) {
                    args.add(pathOrName.getText().toString());
                }
                args.addAll(Lists.transform(variablesImport.getArguments(), TokenFunctions.tokenToString()));

                final RobotSetting setting = new RobotSetting(this, SettingsGroup.VARIABLES, name, args, comment);
                setting.link(variablesImport);
                elements.add(setting);
            }
        }
        final Optional<SuiteDocumentation> documentationSetting = settingsTable.documentation();
        if (documentationSetting.isPresent()) {
            final SuiteDocumentation suiteDocumentation = documentationSetting.get();
            final String name = suiteDocumentation.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(suiteDocumentation.getDocumentationText(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(suiteDocumentation,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotSetting setting = new RobotSetting(this, name, args, comment);
            setting.link(suiteDocumentation);
            elements.add(setting);
        }
        for (final AKeywordBaseSetting<?> keywordSetting : getKeywordBasedSettings(settingsTable)) {
            final String name = keywordSetting.getDeclaration().getText().toString();
            final RobotToken settingKeywordName = keywordSetting.getKeywordName();
            final List<String> args = newArrayList();
            if (settingKeywordName != null) {
                args.add(settingKeywordName.getText().toString());
            }
            args.addAll(Lists.transform(keywordSetting.getArguments(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(keywordSetting,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotSetting setting = new RobotSetting(this, name, args, comment);
            setting.link(keywordSetting);
            elements.add(setting);
        }
        for (final ATags<?> tagSetting : getTagsSettings(settingsTable)) {
            final String name = tagSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(tagSetting.getTags(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(tagSetting,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotSetting setting = new RobotSetting(this, name, args, comment);
            setting.link(tagSetting);
            elements.add(setting);
        }
        for (final TestTemplate templateSetting : settingsTable.getTestTemplates()) {
            final String name = templateSetting.getDeclaration().getText().toString();
            final RobotToken templateKeyword = templateSetting.getKeywordName();
            final List<String> args = newArrayList();
            if (templateKeyword != null) {
                args.add(templateKeyword.getText().toString());
            }
            args.addAll(Lists.transform(templateSetting.getUnexpectedTrashArguments(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(templateSetting,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotSetting setting = new RobotSetting(this, name, args, comment);
            setting.link(templateSetting);
            elements.add(setting);
        }
        final Optional<TestTimeout> timeoutSetting = settingsTable.testTimeout();
        if (timeoutSetting.isPresent()) {
            final TestTimeout testTimeout = timeoutSetting.get();
            final String name = testTimeout.getDeclaration().getText().toString();
            final RobotToken timeout = testTimeout.getTimeout();
            final List<String> args = newArrayList();
            if (timeout != null) {
                args.add(timeout.getText().toString());
            }
            args.addAll(Lists.transform(testTimeout.getMessageArguments(), TokenFunctions.tokenToString()));
            final String comment = CommentServiceHandler.consolidate(testTimeout,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
            final RobotSetting setting = new RobotSetting(this, name, args, comment);
            setting.link(testTimeout);
            elements.add(setting);
        }
        elements.sort(new Comparator<RobotFileInternalElement>() {

            @Override
            public int compare(final RobotFileInternalElement o1, final RobotFileInternalElement o2) {
                final RobotSetting s1 = (RobotSetting) o1;
                final RobotSetting s2 = (RobotSetting) o2;
                return Integer.compare(s1.getDefinitionPosition().getOffset(), s2.getDefinitionPosition().getOffset());
            }
        });
    }

    @Override
    public SettingTable getLinkedElement() {
        return (SettingTable) super.getLinkedElement();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotKeywordCall> getChildren() {
        return (List<RobotKeywordCall>) super.getChildren();
    }

    public RobotSetting createSetting(final String name, final String comment, final String... args) {
        final List<String> settingArgs = newArrayList(args);

        final AModelElement<?> newModelElement = settingTableModelUpdater.create(getLinkedElement(), -1, name, comment,
                settingArgs);

        String consolidatedComment = comment;
        if (!comment.isEmpty()) {
            consolidatedComment = CommentServiceHandler.consolidate((ICommentHolder) newModelElement,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
        }
        final RobotSetting setting = newSetting(name, consolidatedComment, settingArgs);
        setting.link(newModelElement);

        elements.add(setting);

        return setting;
    }

    public void insertSetting(final String name, final String comment, final List<String> args, final int tableIndex,
            final int allSettingsElementsIndex) {
        final RobotSetting setting = newSetting(name, comment, args);

        final AModelElement<?> newModelElement = settingTableModelUpdater.create(getLinkedElement(), tableIndex, name,
                comment, args);
        setting.link(newModelElement);

        if (allSettingsElementsIndex >= 0 && allSettingsElementsIndex <= elements.size()) {
            elements.add(allSettingsElementsIndex, setting);
        }
    }

    private RobotSetting newSetting(final String name, final String comment, final List<String> settingArgs) {
        RobotSetting setting;
        if (name.equals(SettingsGroup.METADATA.getName())) {
            setting = new RobotSetting(this, SettingsGroup.METADATA, name, settingArgs, comment);
        } else if (name.equals(SettingsGroup.LIBRARIES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.LIBRARIES, name, settingArgs, comment);
        } else if (name.equals(SettingsGroup.RESOURCES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.RESOURCES, name, settingArgs, comment);
        } else if (name.equals(SettingsGroup.VARIABLES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.VARIABLES, name, settingArgs, comment);
        } else {
            setting = new RobotSetting(this, SettingsGroup.NO_GROUP, name, settingArgs, comment);
        }
        return setting;
    }

    public List<RobotKeywordCall> getMetadataSettings() {
        return getSettingsFromGroup(SettingsGroup.METADATA);
    }

    public List<RobotKeywordCall> getGeneralSettings() {
        return getSettingsFromGroup(SettingsGroup.NO_GROUP);
    }

    public List<RobotKeywordCall> getResourcesSettings() {
        return getSettingsFromGroup(SettingsGroup.RESOURCES);
    }

    public List<RobotKeywordCall> getVariablesSettings() {
        return getSettingsFromGroup(SettingsGroup.VARIABLES);
    }

    public List<RobotKeywordCall> getImportSettings() {
        return newArrayList(Iterables.filter(getChildren(), new Predicate<RobotKeywordCall>() {

            @Override
            public boolean apply(final RobotKeywordCall element) {
                return SettingsGroup.getImportsGroupsSet().contains((((RobotSetting) element).getGroup()));
            }
        }));
    }

    private List<RobotKeywordCall> getSettingsFromGroup(final SettingsGroup group) {
        return newArrayList(Iterables.filter(getChildren(), new Predicate<RobotKeywordCall>() {

            @Override
            public boolean apply(final RobotKeywordCall element) {
                return (((RobotSetting) element).getGroup() == group);
            }
        }));
    }

    public RobotSetting getSetting(final String name) {
        for (final RobotKeywordCall setting : getChildren()) {
            if (name.equals(setting.getName())) {
                return (RobotSetting) setting;
            }
        }
        return null;
    }

    public List<IPath> getResourcesPaths() {
        final List<RobotKeywordCall> resources = getResourcesSettings();
        final List<IPath> paths = newArrayList();
        for (final RobotElement element : resources) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if (!args.isEmpty()) {
                paths.add(new org.eclipse.core.runtime.Path(args.get(0)));
            }
        }
        return paths;
    }

    public List<IPath> getVariablesPaths() {
        final List<RobotKeywordCall> variables = getVariablesSettings();
        final List<IPath> paths = newArrayList();
        for (final RobotElement element : variables) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if (!args.isEmpty()) {
                paths.add(new org.eclipse.core.runtime.Path(args.get(0)));
            }
        }
        return paths;
    }

    private static List<? extends AKeywordBaseSetting<?>> getKeywordBasedSettings(final SettingTable settingTable) {
        final List<AKeywordBaseSetting<?>> elements = newArrayList();
        final Optional<SuiteSetup> suiteSetup = settingTable.suiteSetup();
        if (suiteSetup.isPresent()) {
            elements.add(suiteSetup.get());
        }
        final Optional<SuiteTeardown> suiteTeardown = settingTable.suiteTeardown();
        if (suiteTeardown.isPresent()) {
            elements.add(suiteTeardown.get());
        }
        final Optional<TestSetup> testSetup = settingTable.testSetup();
        if (testSetup.isPresent()) {
            elements.add(testSetup.get());
        }
        final Optional<TestTeardown> testTeardown = settingTable.testTeardown();
        if (testTeardown.isPresent()) {
            elements.add(testTeardown.get());
        }
        return elements;
    }

    private static List<? extends ATags<?>> getTagsSettings(final SettingTable settingTable) {
        final List<ATags<?>> elements = newArrayList();
        final Optional<ForceTags> forceTags = settingTable.forceTags();
        if (forceTags.isPresent()) {
            elements.add(forceTags.get());
        }
        final Optional<DefaultTags> defaultTags = settingTable.defaultTags();
        if (defaultTags.isPresent()) {
            elements.add(defaultTags.get());
        }
        return elements;
    }
}
