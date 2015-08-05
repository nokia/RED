package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.DefaultTags;
import org.robotframework.ide.core.testData.model.table.setting.ForceTags;
import org.robotframework.ide.core.testData.model.table.setting.Metadata;
import org.robotframework.ide.core.testData.model.table.setting.SuiteDocumentation;
import org.robotframework.ide.core.testData.model.table.setting.SuiteSetup;
import org.robotframework.ide.core.testData.model.table.setting.SuiteTeardown;
import org.robotframework.ide.core.testData.model.table.setting.TestSetup;
import org.robotframework.ide.core.testData.model.table.setting.TestTeardown;
import org.robotframework.ide.core.testData.model.table.setting.TestTemplate;
import org.robotframework.ide.core.testData.model.table.setting.TestTimeout;
import org.robotframework.ide.core.testData.model.table.setting.UnknownSetting;


public class SettingTable extends ARobotSectionTable {

    private final List<AImported> imports = new LinkedList<>();
    private final List<SuiteDocumentation> documentations = new LinkedList<>();
    private final List<Metadata> metadatas = new LinkedList<>();
    private final List<SuiteSetup> suiteSetups = new LinkedList<>();
    private final List<SuiteTeardown> suiteTeardowns = new LinkedList<>();
    private final List<ForceTags> forceTags = new LinkedList<>();
    private final List<DefaultTags> defaultTags = new LinkedList<>();
    private final List<TestSetup> testSetups = new LinkedList<>();
    private final List<TestTeardown> testTeardowns = new LinkedList<>();
    private final List<TestTemplate> testTemplates = new LinkedList<>();
    private final List<TestTimeout> testTimeouts = new LinkedList<>();
    private final List<UnknownSetting> unknownSettings = new LinkedList<>();


    public boolean isEmpty() {
        return imports.isEmpty() && documentations.isEmpty()
                && metadatas.isEmpty() && suiteSetups.isEmpty()
                && suiteTeardowns.isEmpty() && forceTags.isEmpty()
                && defaultTags.isEmpty() && testSetups.isEmpty()
                && testTeardowns.isEmpty() && testTemplates.isEmpty()
                && testTimeouts.isEmpty();
    }


    public List<AImported> getImports() {
        return imports;
    }


    public void addImported(final AImported imported) {
        imports.add(imported);
    }


    public List<SuiteDocumentation> getDocumentation() {
        return documentations;
    }


    public void addDocumentation(final SuiteDocumentation doc) {
        documentations.add(doc);
    }


    public List<Metadata> getMetadatas() {
        return metadatas;
    }


    public void addMetadata(final Metadata metadata) {
        metadatas.add(metadata);
    }


    public List<SuiteSetup> getSuiteSetups() {
        return suiteSetups;
    }


    public void addSuiteSetup(final SuiteSetup suiteSetup) {
        suiteSetups.add(suiteSetup);
    }


    public List<SuiteTeardown> getSuiteTeardowns() {
        return suiteTeardowns;
    }


    public void addSuiteTeardown(final SuiteTeardown suiteTeardown) {
        suiteTeardowns.add(suiteTeardown);
    }


    public List<ForceTags> getForceTags() {
        return forceTags;
    }


    public void addForceTags(final ForceTags tags) {
        forceTags.add(tags);
    }


    public List<DefaultTags> getDefaultTags() {
        return defaultTags;
    }


    public void addDefaultTags(final DefaultTags tags) {
        defaultTags.add(tags);
    }


    public List<TestSetup> getTestSetups() {
        return testSetups;
    }


    public void addTestSetup(final TestSetup testSetup) {
        testSetups.add(testSetup);
    }


    public List<TestTeardown> getTestTeardowns() {
        return testTeardowns;
    }


    public void addTestTeardown(final TestTeardown testTeardown) {
        testTeardowns.add(testTeardown);
    }


    public List<TestTemplate> getTestTemplates() {
        return testTemplates;
    }


    public void addTestTemplate(final TestTemplate testTemplate) {
        testTemplates.add(testTemplate);
    }


    public List<TestTimeout> getTestTimeouts() {
        return testTimeouts;
    }


    public void addTestTimeout(final TestTimeout testTimeout) {
        testTimeouts.add(testTimeout);
    }


    public List<UnknownSetting> getUnknownSettings() {
        return unknownSettings;
    }


    public void addUnknownSetting(final UnknownSetting unknownSetting) {
        unknownSettings.add(unknownSetting);
    }
}
