package org.robotframework.ide.core.testData.text.context;

import org.robotframework.ide.core.testData.text.context.mapper.MapperOutput;
import org.robotframework.ide.core.testData.text.context.mapper.MapperTemporaryStore;


public class ModelElementsMapper {

    public MapperOutput map(final MapperTemporaryStore store) {
        MapperOutput mapOut = new MapperOutput();

        System.out.println(store);
        System.exit(0);
        return mapOut;
    }

}
