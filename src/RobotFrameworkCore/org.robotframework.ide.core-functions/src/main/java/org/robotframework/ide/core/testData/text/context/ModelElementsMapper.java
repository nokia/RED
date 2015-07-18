package org.robotframework.ide.core.testData.text.context;

import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.context.mapper.MapperOutput;
import org.robotframework.ide.core.testData.text.context.mapper.MapperTemporaryStore;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;


public class ModelElementsMapper {

    public MapperOutput map(final MapperTemporaryStore store) {
        MapperOutput mapOut = new MapperOutput();

        // extract elements from temp store
        ElementType lastType = store.getLastType();
        final List<LineElement> lineElements = store.getCurrentLineElements();
        final List<IContextElement> nearestCtxs = store.getNearestContexts();
        final List<IContextElement> separatorsAndNormalCtxs = store
                .getSeparatorsAndNormalCtxs();
        final List<RobotToken> tokensWithoutContext = store
                .getTokensWithoutContext();
        final ModelOutput model = store.getModel();

        System.out.println(tokensWithoutContext);
        System.out.println(nearestCtxs);
        System.exit(0);

        return mapOut;
    }
}
