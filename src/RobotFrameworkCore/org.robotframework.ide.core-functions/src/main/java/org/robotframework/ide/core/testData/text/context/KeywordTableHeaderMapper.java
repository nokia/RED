package org.robotframework.ide.core.testData.text.context;

import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.mapper.ATableHeaderMapper;


public class KeywordTableHeaderMapper extends ATableHeaderMapper {

    private static final ElementType BUILD_TYPE = ElementType.KEYWORD_TABLE_HEADER;


    public KeywordTableHeaderMapper() {
        super(BUILD_TYPE);
    }
}
