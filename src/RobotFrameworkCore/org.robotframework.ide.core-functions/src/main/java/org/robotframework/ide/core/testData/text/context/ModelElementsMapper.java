package org.robotframework.ide.core.testData.text.context;

import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;


public class ModelElementsMapper {

    public MapperOutput map(final ModelOutput model,
            final AggregatedOneLineRobotContexts ctx,
            final List<IContextElement> nearestCtxs,
            final List<RobotToken> gapTokens, final ElementType etLast) {
        MapperOutput mapOut = new MapperOutput();

        if (gapTokens.isEmpty()) {
            List<RobotToken> contextTokens = ((OneLineSingleRobotContextPart) nearestCtxs
                    .get(1)).getContextTokens();
            mapOut.setNextPosition(contextTokens.get(contextTokens.size() - 1)
                    .getEndPosition());
        } else {
            mapOut.setNextPosition(gapTokens.get(gapTokens.size() - 1)
                    .getEndPosition());
        }

        return mapOut;
    }

    public static class MapperOutput {

        private ElementType etLast;
        private FilePosition fp;


        public ElementType getMappedElementType() {
            return etLast;
        }


        public void setMappedElementType(ElementType etLast) {
            this.etLast = etLast;
        }


        public FilePosition getNextPosition() {
            return fp;
        }


        public void setNextPosition(FilePosition fp) {
            this.fp = fp;
        }

    }
}
