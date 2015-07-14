package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.testCases.settings.Documentation;
import org.robotframework.ide.core.testData.model.table.testCases.settings.Postcondition;
import org.robotframework.ide.core.testData.model.table.testCases.settings.Precondition;
import org.robotframework.ide.core.testData.model.table.testCases.settings.Setup;
import org.robotframework.ide.core.testData.model.table.testCases.settings.Tags;
import org.robotframework.ide.core.testData.model.table.testCases.settings.Teardown;
import org.robotframework.ide.core.testData.model.table.testCases.settings.Template;
import org.robotframework.ide.core.testData.model.table.testCases.settings.Timeout;


public class TestCase {

    private final TestCaseName testCaseName;
    private List<Documentation> documentation = new LinkedList<>();
    private List<Tags> tags = new LinkedList<>();
    private List<Setup> setup = new LinkedList<>();
    private List<Precondition> precondition = new LinkedList<>();
    private List<Teardown> teardown = new LinkedList<>();
    private List<Postcondition> postcondition = new LinkedList<>();
    private List<Template> template = new LinkedList<>();
    private List<Timeout> timeout = new LinkedList<>();


    public TestCase(final TestCaseName testCaseName) {
        this.testCaseName = testCaseName;
    }
}
