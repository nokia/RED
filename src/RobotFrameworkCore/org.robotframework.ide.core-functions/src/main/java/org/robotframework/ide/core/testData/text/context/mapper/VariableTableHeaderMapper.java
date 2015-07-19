package org.robotframework.ide.core.testData.text.context.mapper;

import org.robotframework.ide.core.testData.model.LineElement.ElementType;


public class VariableTableHeaderMapper extends ATableHeaderMapper {

    private static final ElementType BUILD_TYPE = ElementType.VARIABLE_TABLE_HEADER;


    public VariableTableHeaderMapper() {
        super(BUILD_TYPE);
    }
}
