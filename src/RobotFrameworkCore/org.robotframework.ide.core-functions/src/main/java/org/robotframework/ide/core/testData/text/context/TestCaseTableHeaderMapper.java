package org.robotframework.ide.core.testData.text.context;

import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.mapper.ATableHeaderMapper;


public class TestCaseTableHeaderMapper extends ATableHeaderMapper {

    private static final ElementType BUILD_TYPE = ElementType.TEST_CASE_TABLE_HEADER;


    public TestCaseTableHeaderMapper() {
        super(BUILD_TYPE);
    }
}
