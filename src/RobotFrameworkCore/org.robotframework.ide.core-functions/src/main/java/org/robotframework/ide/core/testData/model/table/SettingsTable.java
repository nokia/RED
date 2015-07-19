package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.settings.AExternalImported;
import org.robotframework.ide.core.testData.model.table.settings.doc.Documentation;
import org.robotframework.ide.core.testData.model.table.settings.metadata.Metadata;
import org.robotframework.ide.core.testData.model.table.settings.suite.SuitePostcondition;
import org.robotframework.ide.core.testData.model.table.settings.suite.SuitePrecondition;
import org.robotframework.ide.core.testData.model.table.settings.suite.SuiteSetup;
import org.robotframework.ide.core.testData.model.table.settings.suite.SuiteTeardown;
import org.robotframework.ide.core.testData.model.table.settings.tags.DefaultTags;
import org.robotframework.ide.core.testData.model.table.settings.tags.ForceTags;
import org.robotframework.ide.core.testData.model.table.settings.test.TestPostcondition;
import org.robotframework.ide.core.testData.model.table.settings.test.TestPrecondition;
import org.robotframework.ide.core.testData.model.table.settings.test.TestSetup;
import org.robotframework.ide.core.testData.model.table.settings.test.TestTeardown;
import org.robotframework.ide.core.testData.model.table.settings.test.TestTemplate;
import org.robotframework.ide.core.testData.model.table.settings.test.TestTimeout;


public class SettingsTable extends ATableModel {

    private final List<AExternalImported> imports = new LinkedList<>();
    private final List<Documentation> documentation = new LinkedList<>();
    private final List<Metadata> metadata = new LinkedList<>();
    private final List<SuiteSetup> suiteSetup = new LinkedList<>();
    private final List<SuitePrecondition> suitePreconditon = new LinkedList<>();
    private final List<SuiteTeardown> suiteTeardown = new LinkedList<>();
    private final List<SuitePostcondition> suitePostcondition = new LinkedList<>();
    private final List<TestSetup> testSetup = new LinkedList<>();
    private final List<TestPrecondition> testPreconditon = new LinkedList<>();
    private final List<TestTeardown> testTeardown = new LinkedList<>();
    private final List<TestPostcondition> testPostcondition = new LinkedList<>();
    private final List<TestTemplate> testTemplate = new LinkedList<>();
    private final List<TestTimeout> testTimeout = new LinkedList<>();
    private final List<ForceTags> forceTags = new LinkedList<>();
    private final List<DefaultTags> defaultTags = new LinkedList<>();


    public SettingsTable(final TableHeader header) {
        super(header, "Settings");
    }
}
