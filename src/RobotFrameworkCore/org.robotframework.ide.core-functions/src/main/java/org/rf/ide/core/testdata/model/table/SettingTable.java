/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.TemplateSetting;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TaskSetup;
import org.rf.ide.core.testdata.model.table.setting.TaskTeardown;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.model.table.setting.TaskTimeout;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.UnknownSetting;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.model.table.setting.views.DefaultTagsView;
import org.rf.ide.core.testdata.model.table.setting.views.ForceTagsView;
import org.rf.ide.core.testdata.model.table.setting.views.SuiteDocumentationView;
import org.rf.ide.core.testdata.model.table.setting.views.SuiteSetupView;
import org.rf.ide.core.testdata.model.table.setting.views.SuiteTeardownView;
import org.rf.ide.core.testdata.model.table.setting.views.TestSetupView;
import org.rf.ide.core.testdata.model.table.setting.views.TestTeardownView;
import org.rf.ide.core.testdata.model.table.setting.views.TestTemplateView;
import org.rf.ide.core.testdata.model.table.setting.views.TestTimeoutView;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SettingTable extends ARobotSectionTable {

    private final List<AImported> imports = new ArrayList<>();

    private final List<SuiteDocumentation> documentations = new ArrayList<>();

    private final List<Metadata> metadatas = new ArrayList<>();

    private final List<SuiteSetup> suiteSetups = new ArrayList<>();

    private final List<SuiteTeardown> suiteTeardowns = new ArrayList<>();

    private final List<ForceTags> forceTags = new ArrayList<>();

    private final List<DefaultTags> defaultTags = new ArrayList<>();

    private final List<TestSetup> testSetups = new ArrayList<>();

    private final List<TestTeardown> testTeardowns = new ArrayList<>();

    private final List<TestTemplate> testTemplates = new ArrayList<>();

    private final List<TestTimeout> testTimeouts = new ArrayList<>();

    private final List<TaskSetup> taskSetups = new ArrayList<>();

    private final List<TaskTeardown> taskTeardowns = new ArrayList<>();

    private final List<TaskTemplate> taskTemplates = new ArrayList<>();

    private final List<TaskTimeout> taskTimeouts = new ArrayList<>();

    private final List<UnknownSetting> unknownSettings = new ArrayList<>();

    public SettingTable(final RobotFile parent) {
        super(parent);
    }

    public boolean isEmpty() {
        return imports.isEmpty() && documentations.isEmpty() && metadatas.isEmpty() && suiteSetups.isEmpty()
                && suiteTeardowns.isEmpty() && forceTags.isEmpty() && defaultTags.isEmpty() && testSetups.isEmpty()
                && testTeardowns.isEmpty() && testTemplates.isEmpty() && testTimeouts.isEmpty()
                && unknownSettings.isEmpty();
    }

    public List<AImported> getImports() {
        return Collections.unmodifiableList(imports);
    }

    public List<VariablesImport> getVariablesImports() {
        return getImports().stream().filter(VariablesImport.class::isInstance).map(VariablesImport.class::cast).collect(
                toList());
    }

    public List<ResourceImport> getResourcesImports() {
        return getImports().stream().filter(ResourceImport.class::isInstance).map(ResourceImport.class::cast).collect(
                toList());
    }

    public List<LibraryImport> getLibraryImports() {
        return getImports().stream().filter(LibraryImport.class::isInstance).map(LibraryImport.class::cast).collect(
                toList());
    }

    public LibraryImport newLibraryImport() {
        return newLibraryImport(imports.size());
    }

    public LibraryImport newLibraryImport(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_LIBRARY_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final LibraryImport libImp = new LibraryImport(dec);
        addImported(libImp, index);

        return libImp;
    }

    public ResourceImport newResourceImport() {
        return newResourceImport(imports.size());
    }

    public ResourceImport newResourceImport(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_RESOURCE_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final ResourceImport resImp = new ResourceImport(dec);
        addImported(resImp, index);

        return resImp;
    }

    public VariablesImport newVariablesImport() {
        return newVariablesImport(imports.size());
    }

    public VariablesImport newVariablesImport(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_VARIABLES_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final VariablesImport varImp = new VariablesImport(dec);
        addImported(varImp, index);

        return varImp;
    }

    public void addImported(final AImported imported) {
        imported.setParent(this);
        imports.add(imported);
    }

    public void addImported(final AImported imported, final int position) {
        imported.setParent(this);
        imports.add(position, imported);
    }

    public void removeImported(final AImported imported) {
        imports.remove(imported);
    }

    @Override
    public boolean moveUpElement(final AModelElement<? extends ARobotSectionTable> element) {
        if (metadatas.contains(element)) {
            return MoveElementHelper.moveUp(metadatas, (Metadata) element);

        } else if (imports.contains(element)) {
            return MoveElementHelper.moveUp(imports, (AImported) element);
        }
        return false;
    }

    @Override
    public boolean moveDownElement(final AModelElement<? extends ARobotSectionTable> element) {
        if (metadatas.contains(element)) {
            return MoveElementHelper.moveDown(metadatas, (Metadata) element);

        } else if (imports.contains(element)) {
            return MoveElementHelper.moveDown(imports, (AImported) element);
        }
        return false;
    }

    public Optional<SuiteDocumentation> documentation() {
        return MultipleSettingsViewsCreator.createView(documentations, SuiteDocumentationView::new);
    }

    public List<SuiteDocumentation> getDocumentation() {
        return Collections.unmodifiableList(documentations);
    }

    public SuiteDocumentation newSuiteDocumentation() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_DOCUMENTATION_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final SuiteDocumentation suiteDoc = new SuiteDocumentation(dec);
        addDocumentation(suiteDoc);

        return suiteDoc;
    }

    public void addDocumentation(final SuiteDocumentation doc) {
        doc.setParent(this);
        documentations.add(doc);
        getParent().getParent().getDocumentationCacher().register(doc);
    }

    public void removeDocumentation() {
        for (final SuiteDocumentation doc : documentations) {
            getParent().getParent().getDocumentationCacher().unregister(doc);
        }
        documentations.clear();
    }

    public List<Metadata> getMetadatas() {
        return Collections.unmodifiableList(metadatas);
    }

    public Metadata newMetadata() {
        return newMetadata(metadatas.size());
    }

    public Metadata newMetadata(final int index) {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_METADATA_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final Metadata metadata = new Metadata(dec);
        addMetadata(metadata, index);

        return metadata;
    }

    public void addMetadata(final Metadata metadata) {
        metadata.setParent(this);
        metadatas.add(metadata);
    }

    public void addMetadata(final Metadata metadata, final int position) {
        metadata.setParent(this);
        metadatas.add(position, metadata);
    }

    public void removeMetadata(final Metadata metadata) {
        metadatas.remove(metadata);
    }

    public List<SuiteSetup> getSuiteSetupsViews() {
        final RobotVersion version = getVersion();
        return MultipleSettingsViewsCreator.createView(version, this, suiteSetups, SuiteSetupView::new);
    }

    public List<SuiteSetup> getSuiteSetups() {
        return Collections.unmodifiableList(suiteSetups);
    }

    public SuiteSetup newSuiteSetup() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final SuiteSetup suiteSetup = new SuiteSetup(dec);
        addSuiteSetup(suiteSetup);

        return suiteSetup;
    }

    public void addSuiteSetup(final SuiteSetup suiteSetup) {
        suiteSetup.setParent(this);
        suiteSetups.add(suiteSetup);
    }

    public void removeSuiteSetup() {
        suiteSetups.clear();
    }

    public List<SuiteTeardown> getSuiteTeardownsViews() {
        final RobotVersion version = getVersion();
        return MultipleSettingsViewsCreator.createView(version, this, suiteTeardowns, SuiteTeardownView::new);
    }

    public List<SuiteTeardown> getSuiteTeardowns() {
        return Collections.unmodifiableList(suiteTeardowns);
    }

    public SuiteTeardown newSuiteTeardown() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final SuiteTeardown suiteTeardown = new SuiteTeardown(dec);
        addSuiteTeardown(suiteTeardown);

        return suiteTeardown;
    }

    public void addSuiteTeardown(final SuiteTeardown suiteTeardown) {
        suiteTeardown.setParent(this);
        suiteTeardowns.add(suiteTeardown);
    }

    public void removeSuiteTeardown() {
        suiteTeardowns.clear();
    }

    public ForceTags newForceTag() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_FORCE_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final ForceTags tags = new ForceTags(dec);
        addForceTags(tags);

        return tags;
    }

    public List<ForceTags> getForceTagsViews() {
        final RobotVersion version = getVersion();
        return MultipleSettingsViewsCreator.createView(version, this, forceTags, ForceTagsView::new);
    }

    public List<ForceTags> getForceTags() {
        return Collections.unmodifiableList(forceTags);
    }

    public void addForceTags(final ForceTags tags) {
        tags.setParent(this);
        forceTags.add(tags);
    }

    public void removeForceTags() {
        forceTags.clear();
    }

    public List<DefaultTags> getDefaultTagsViews() {
        final RobotVersion version = getVersion();
        return MultipleSettingsViewsCreator.createView(version, this, defaultTags, DefaultTagsView::new);
    }

    public List<DefaultTags> getDefaultTags() {
        return Collections.unmodifiableList(defaultTags);
    }

    public DefaultTags newDefaultTag() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final DefaultTags tags = new DefaultTags(dec);
        addDefaultTags(tags);

        return tags;
    }

    public void addDefaultTags(final DefaultTags tags) {
        tags.setParent(this);
        defaultTags.add(tags);
    }

    public void removeDefaultTags() {
        defaultTags.clear();
    }

    public List<TestSetup> getTestSetupsViews() {
        final RobotVersion version = getVersion();
        return MultipleSettingsViewsCreator.createView(version, this, testSetups, TestSetupView::new);
    }

    public List<TestSetup> getTestSetups() {
        return Collections.unmodifiableList(testSetups);
    }

    public TestSetup newTestSetup() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_SETUP_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final TestSetup testSetup = new TestSetup(dec);
        addTestSetup(testSetup);

        return testSetup;
    }

    public void addTestSetup(final TestSetup testSetup) {
        testSetup.setParent(this);
        testSetups.add(testSetup);
    }

    public void removeTestSetup() {
        testSetups.clear();
    }

    public List<TaskSetup> getTaskSetups() {
        return Collections.unmodifiableList(taskSetups);
    }

    public TaskSetup newTaskSetup() {
        final RobotToken dec = RobotToken
                .create(RobotTokenType.SETTING_TASK_SETUP_DECLARATION.getTheMostCorrectOneRepresentation(getVersion())
                        .getRepresentation());

        final TaskSetup taskSetup = new TaskSetup(dec);
        addTaskSetup(taskSetup);
        return taskSetup;
    }

    public void addTaskSetup(final TaskSetup taskSetup) {
        taskSetup.setParent(this);
        taskSetups.add(taskSetup);
    }

    public void removeTaskSetup() {
        taskSetups.clear();
    }

    public List<TestTeardown> getTestTeardownsViews() {
        final RobotVersion version = getVersion();
        return MultipleSettingsViewsCreator.createView(version, this, testTeardowns, TestTeardownView::new);
    }

    public List<TestTeardown> getTestTeardowns() {
        return Collections.unmodifiableList(testTeardowns);
    }

    public TestTeardown newTestTeardown() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final TestTeardown testTeardown = new TestTeardown(dec);
        addTestTeardown(testTeardown);

        return testTeardown;
    }

    public void addTestTeardown(final TestTeardown testTeardown) {
        testTeardown.setParent(this);
        testTeardowns.add(testTeardown);
    }

    public void removeTestTeardown() {
        testTeardowns.clear();
    }

    public List<TaskTeardown> getTaskTeardowns() {
        return Collections.unmodifiableList(taskTeardowns);
    }

    public TaskTeardown newTaskTeardown() {
        final RobotToken dec = RobotToken.create(
                RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION.getTheMostCorrectOneRepresentation(getVersion())
                        .getRepresentation());

        final TaskTeardown testTeardown = new TaskTeardown(dec);
        addTaskTeardown(testTeardown);
        return testTeardown;
    }

    public void addTaskTeardown(final TaskTeardown taskTeardown) {
        taskTeardown.setParent(this);
        taskTeardowns.add(taskTeardown);
    }

    public void removeTaskTeardown() {
        taskTeardowns.clear();
    }

    public List<TestTemplate> getTestTemplatesViews() {
        final RobotVersion version = getVersion();
        return MultipleSettingsViewsCreator.createView(version, this, testTemplates, TestTemplateView::new);
    }

    public List<TestTemplate> getTestTemplates() {
        return Collections.unmodifiableList(testTemplates);
    }

    public String getTestTemplateInUse() {
        final RobotVersion robotVersion = getVersion();
        if (robotVersion.isOlderThan(new RobotVersion(3, 0))) {
            return testTemplates.stream()
                    .flatMap(SettingTable::tokensOf)
                    .filter(t -> t != null)
                    .map(RobotToken::getText)
                    .collect(joining(" "));

        } else if (testTemplates.size() == 1) {
            final TestTemplate template = testTemplates.get(0);

            if (robotVersion.isNewerOrEqualTo(new RobotVersion(3, 2)) && !template.getUnexpectedArguments().isEmpty()) {
                return null;
            } else {
                return tokensOf(template).filter(t -> t != null).map(RobotToken::getText).collect(joining(" "));
            }
        }
        return null;
    }

    private static Stream<RobotToken> tokensOf(final TemplateSetting template) {
        return Stream.concat(Stream.of(template.getKeywordName()), template.getUnexpectedArguments().stream());
    }

    public TestTemplate newTestTemplate() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final TestTemplate testTemplate = new TestTemplate(dec);
        addTestTemplate(testTemplate);

        return testTemplate;
    }

    public void addTestTemplate(final TestTemplate testTemplate) {
        testTemplate.setParent(this);
        testTemplates.add(testTemplate);
    }

    public void removeTestTemplate() {
        testTemplates.clear();
    }

    public List<TaskTemplate> getTaskTemplates() {
        return Collections.unmodifiableList(taskTemplates);
    }

    public String getTaskTemplateInUse() {
        if (taskTemplates.size() == 1) {
            final RobotVersion robotVersion = getVersion();

            final TaskTemplate template = taskTemplates.get(0);

            if (robotVersion.isNewerOrEqualTo(new RobotVersion(3, 2)) && !template.getUnexpectedArguments().isEmpty()) {
                return null;
            } else {
                return tokensOf(template).filter(t -> t != null).map(RobotToken::getText).collect(joining(" "));
            }
        }
        return null;
    }

    public TaskTemplate newTaskTemplate() {
        final RobotToken dec = RobotToken.create(
                RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION.getTheMostCorrectOneRepresentation(getVersion())
                        .getRepresentation());

        final TaskTemplate taskTemplate = new TaskTemplate(dec);
        addTaskTemplate(taskTemplate);
        return taskTemplate;
    }

    public void addTaskTemplate(final TaskTemplate taskTemplate) {
        taskTemplate.setParent(this);
        taskTemplates.add(taskTemplate);
    }

    public void removeTaskTemplate() {
        taskTemplates.clear();
    }

    public List<TestTimeout> getTestTimeoutsViews() {
        final RobotVersion version = getVersion();
        return MultipleSettingsViewsCreator.createView(version, this, testTimeouts, TestTimeoutView::new);
    }

    public List<TestTimeout> getTestTimeouts() {
        return Collections.unmodifiableList(testTimeouts);
    }

    public TestTimeout newTestTimeout() {
        final RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION
                .getTheMostCorrectOneRepresentation(getVersion()).getRepresentation());

        final TestTimeout testTimeout = new TestTimeout(dec);
        addTestTimeout(testTimeout);

        return testTimeout;
    }

    public void addTestTimeout(final TestTimeout testTimeout) {
        testTimeout.setParent(this);
        testTimeouts.add(testTimeout);
    }

    public void removeTestTimeout() {
        testTimeouts.clear();
    }

    public List<TaskTimeout> getTaskTimeouts() {
        return Collections.unmodifiableList(taskTimeouts);
    }

    public TaskTimeout newTaskTimeout() {
        final RobotToken dec = RobotToken
                .create(RobotTokenType.SETTING_TASK_TIMEOUT_DECLARATION.getTheMostCorrectOneRepresentation(getVersion())
                        .getRepresentation());

        final TaskTimeout taskTimeout = new TaskTimeout(dec);
        addTaskTimeout(taskTimeout);
        return taskTimeout;
    }

    public void addTaskTimeout(final TaskTimeout taskTimeout) {
        taskTimeout.setParent(this);
        taskTimeouts.add(taskTimeout);
    }

    public void removeTaskTimeout() {
        taskTimeouts.clear();
    }

    public List<UnknownSetting> getUnknownSettings() {
        return Collections.unmodifiableList(unknownSettings);
    }

    public void addUnknownSetting(final UnknownSetting unknownSetting) {
        unknownSetting.setParent(this);
        unknownSettings.add(unknownSetting);
    }

    private RobotVersion getVersion() {
        return getParent().getParent().getRobotVersion();
    }
}
