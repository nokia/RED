/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotExpressions;
import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.model.table.setting.TaskTimeout;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;

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
            elements.add(new RobotSetting(this, SettingsGroup.METADATA, metadataSetting));
        }
        for (final AImported importSetting : settingsTable.getImports()) {
            SettingsGroup group = SettingsGroup.NO_GROUP;
            if (importSetting instanceof LibraryImport) {
                group = SettingsGroup.LIBRARIES;
            } else if (importSetting instanceof ResourceImport) {
                group = SettingsGroup.RESOURCES;
            } else if (importSetting instanceof VariablesImport) {
                group = SettingsGroup.VARIABLES;
            }
            elements.add(new RobotSetting(this, group, importSetting));
        }
        final Optional<SuiteDocumentation> documentationSetting = settingsTable.documentation();
        if (documentationSetting.isPresent()) {
            elements.add(new RobotSetting(this, documentationSetting.get()));
        }
        for (final AKeywordBaseSetting<?> keywordSetting : getKeywordBasedSettings(settingsTable)) {
            elements.add(new RobotSetting(this, keywordSetting));
        }
        for (final ATags<?> tagSetting : getTagsSettings(settingsTable)) {
            elements.add(new RobotSetting(this, tagSetting));
        }
        for (final TestTemplate templateSetting : settingsTable.getTestTemplatesViews()) {
            elements.add(new RobotSetting(this, templateSetting));
        }
        for (final TaskTemplate templateSetting : settingsTable.getTaskTemplates()) {
            elements.add(new RobotSetting(this, templateSetting));
        }
        for (final TestTimeout timeoutSetting : settingsTable.getTestTimeoutsViews()) {
            elements.add(new RobotSetting(this, timeoutSetting));
        }
        for (final TaskTimeout timeoutSetting : settingsTable.getTaskTimeouts()) {
            elements.add(new RobotSetting(this, timeoutSetting));
        }
        Collections.sort(elements, (o1, o2) -> Integer.compare(o1.getDefinitionPosition().getOffset(),
                o2.getDefinitionPosition().getOffset()));
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

    @Override
    public String getDefaultChildName() {
        return "Setting";
    }

    @Override
    public RobotFileInternalElement createChild(final int index, final String name) {
        return createSetting(name, "");
    }

    public RobotSetting createSetting(final String name, final String comment, final String... args) {
        final List<String> settingArgs = newArrayList(args);

        final AModelElement<?> newModelElement = settingTableModelUpdater.create(getLinkedElement(), -1, name, comment,
                settingArgs);

        final RobotSetting setting = newSetting(name, newModelElement);
        elements.add(setting);

        return setting;
    }

    @Override
    public void insertChild(final int index, final RobotFileInternalElement element) {
        throw new IllegalStateException("Not implemented for settings section");
    }

    @Override
    public void removeChildren(final List<? extends RobotFileInternalElement> elementsToRemove) {
        throw new IllegalStateException("Not implemented for settings section");
    }

    public RobotSetting insertSetting(final RobotKeywordCall call, final int allSettingsElementsIndex) {

        final RobotSetting setting = (RobotSetting) call;
        final int tableIndex = countRowsOfGroupUpTo(setting.getGroup(), allSettingsElementsIndex);
        settingTableModelUpdater.insert(getLinkedElement(), tableIndex, setting.getLinkedElement());

        if (allSettingsElementsIndex >= 0 && allSettingsElementsIndex <= elements.size()) {
            call.setParent(this);
            elements.add(allSettingsElementsIndex, setting);
        }

        return setting;
    }

    private RobotSetting newSetting(final String name, final AModelElement<?> newModelElement) {
        RobotSetting setting;
        if (name.equals(SettingsGroup.METADATA.getName())) {
            setting = new RobotSetting(this, SettingsGroup.METADATA, newModelElement);
        } else if (name.equals(SettingsGroup.LIBRARIES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.LIBRARIES, newModelElement);
        } else if (name.equals(SettingsGroup.RESOURCES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.RESOURCES, newModelElement);
        } else if (name.equals(SettingsGroup.VARIABLES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.VARIABLES, newModelElement);
        } else {
            setting = new RobotSetting(this, SettingsGroup.NO_GROUP, newModelElement);
        }
        return setting;
    }

    @Override
    public void removeChild(final RobotKeywordCall child) {
        getChildren().remove(child);
        new SettingTableModelUpdater().remove(getLinkedElement(), child.getLinkedElement());
    }

    public List<RobotSetting> getMetadataSettings() {
        return getSettingsFromGroup(SettingsGroup.METADATA);
    }

    public List<RobotSetting> getGeneralSettings() {
        return getSettingsFromGroup(SettingsGroup.NO_GROUP);
    }

    public List<RobotSetting> getResourcesSettings() {
        return getSettingsFromGroup(SettingsGroup.RESOURCES);
    }

    public List<RobotSetting> getVariablesSettings() {
        return getSettingsFromGroup(SettingsGroup.VARIABLES);
    }

    public List<RobotSetting> getLibrariesSettings() {
        return getSettingsFromGroup(SettingsGroup.LIBRARIES);
    }

    public List<RobotSetting> getImportSettings() {
        return getChildren().stream()
                .filter(RobotSetting.class::isInstance)
                .map(RobotSetting.class::cast)
                .filter(RobotSetting::isImportSetting)
                .collect(toList());
    }

    private List<RobotSetting> getSettingsFromGroup(final SettingsGroup group) {
        return getChildren().stream()
                .filter(RobotSetting.class::isInstance)
                .map(RobotSetting.class::cast)
                .filter(setting -> setting.getGroup() == group)
                .collect(toList());
    }

    public synchronized Optional<RobotSetting> getSetting(final ModelType type) {
        for (final RobotKeywordCall setting : getChildren()) {
            if (setting.getLinkedElement().getModelType() == type) {
                return Optional.of((RobotSetting) setting);
            }
        }
        return Optional.empty();
    }

    public synchronized RobotSetting getSetting(final String name) {
        for (final RobotKeywordCall setting : getChildren()) {
            if (name.equals(setting.getName())) {
                return (RobotSetting) setting;
            }
        }
        return null;
    }

    public List<String> getResourcesPaths() {
        return getImportPaths(getResourcesSettings());
    }

    public List<String> getVariablesPaths() {
        return getImportPaths(getVariablesSettings());
    }

    private static List<String> getImportPaths(final List<RobotSetting> importSettings) {
        return importSettings.stream()
                .map(RobotSetting::getArguments)
                .filter(not(Collection::isEmpty))
                .map(l -> l.get(0))
                .map(RobotExpressions::unescapeSpaces)
                .collect(toList());
    }

    private static List<? extends AKeywordBaseSetting<?>> getKeywordBasedSettings(final SettingTable settingTable) {
        final List<AKeywordBaseSetting<?>> elements = new ArrayList<>();

        settingTable.getSuiteSetupsViews().stream().findFirst().ifPresent(elements::add);
        settingTable.getSuiteTeardownsViews().stream().findFirst().ifPresent(elements::add);
        settingTable.getTestSetupsViews().stream().findFirst().ifPresent(elements::add);
        settingTable.getTaskSetups().stream().findFirst().ifPresent(elements::add);
        settingTable.getTestTeardownsViews().stream().findFirst().ifPresent(elements::add);
        settingTable.getTaskTeardowns().stream().findFirst().ifPresent(elements::add);

        return elements;
    }

    private static List<? extends ATags<?>> getTagsSettings(final SettingTable settingTable) {
        final List<ATags<?>> elements = new ArrayList<>();

        settingTable.getForceTagsViews().forEach(elements::add);
        settingTable.getDefaultTagsViews().forEach(elements::add);

        return elements;
    }

    private int countRowsOfGroupUpTo(final SettingsGroup group, final int toIndex) {
        int index = 0;
        int count = 0;
        for (final RobotKeywordCall call : getChildren()) {
            if (index >= toIndex) {
                break;
            }
            final String name = call.getName();
            if ((SettingsGroup.getImportsGroupsSet().contains(group)
                    && (name.equals(SettingsGroup.LIBRARIES.getName()) || name.equals(SettingsGroup.RESOURCES.getName())
                            || name.equals(SettingsGroup.VARIABLES.getName())))
                    || name.equals(group.getName())) {
                count++;
            }
            index++;
        }
        return count;
    }
}
