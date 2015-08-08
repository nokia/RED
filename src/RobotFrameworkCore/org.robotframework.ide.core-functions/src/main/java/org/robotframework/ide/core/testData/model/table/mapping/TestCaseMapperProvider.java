package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseNameMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseSetupMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTagsMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTeardownMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseTimeoutMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestDocumentationMapper;


public class TestCaseMapperProvider {

    private static final List<IParsingMapper> mappers = new LinkedList<>();
    static {
        mappers.add(new TestCaseNameMapper());
        mappers.add(new TestCaseSetupMapper());
        mappers.add(new TestCaseTagsMapper());
        mappers.add(new TestCaseTeardownMapper());
        mappers.add(new TestCaseTagsMapper());
        mappers.add(new TestCaseTimeoutMapper());
        mappers.add(new TestDocumentationMapper());
    }


    public List<IParsingMapper> getMappers() {
        return mappers;
    }
}
