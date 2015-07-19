package org.robotframework.ide.core.testData.text.context.mapper;

import org.robotframework.ide.core.testData.text.context.IContextElement;


public interface IContextMapper {

    MapperOutput map(final MapperTemporaryStore store,
            final IContextElement thisContext);
}
