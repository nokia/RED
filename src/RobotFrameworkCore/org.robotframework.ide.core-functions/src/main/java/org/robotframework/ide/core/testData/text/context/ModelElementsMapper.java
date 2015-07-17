package org.robotframework.ide.core.testData.text.context;

import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class ModelElementsMapper {

    public MapperOutput map(final ModelOutput model,
            final AggregatedOneLineRobotContexts ctx,
            final List<IContextElement> nearestCtxs,
            final List<RobotToken> gapTokens, final List<LineElement> elems,
            final ElementType etLast) {
        MapperOutput mapOut = new MapperOutput();

        if (gapTokens.isEmpty()) {
            List<RobotToken> contextTokens = ((OneLineSingleRobotContextPart) nearestCtxs
                    .get(0)).getContextTokens();
            mapOut.setNextPosition(contextTokens.get(contextTokens.size() - 1)
                    .getEndPosition());
        } else {
            if (etLast == null) {
                // nothing previously exists it is just comment not declared
                for (RobotToken token : gapTokens) {
                    LineElement elem = new LineElement();

                }

            } else {

            }

            mapOut.setNextPosition(gapTokens.get(gapTokens.size() - 1)
                    .getEndPosition());
        }

        return mapOut;
    }


    @VisibleForTesting
    protected ElementType mapTokenType(IRobotTokenType type,
            final ElementType etLast) {
        ElementType et = ElementType.VALUE;

        // etLast for column
        return et;
    }


    @VisibleForTesting
    protected ElementType mapTokenType(IContextElementType type,
            final ElementType etLast) {
        ElementType et = ElementType.VALUE;

        // etLast for column
        return et;
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
