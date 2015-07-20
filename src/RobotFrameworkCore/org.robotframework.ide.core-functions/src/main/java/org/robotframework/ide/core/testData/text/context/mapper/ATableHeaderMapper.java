package org.robotframework.ide.core.testData.text.context.mapper;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.iterator.ContextTokenIterator.SeparationType;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public abstract class ATableHeaderMapper implements IContextMapper {

    private final ElementType BUILD_TYPE;


    protected ATableHeaderMapper(final ElementType type) {
        this.BUILD_TYPE = type;
    }


    @Override
    public MapperOutput map(final MapperTemporaryStore store,
            final IContextElement thisContext) {
        MapperOutput mapOut = new MapperOutput();

        // extract elements from temp store
        ElementType lastType = store.getLastType();
        List<LineElement> lineElements = store.getCurrentLineElements();

        SeparationType separatorType = store.getSeparatorType();
        if (isContextCorrect(lineElements, separatorType)) {
            OneLineSingleRobotContextPart ctx = (OneLineSingleRobotContextPart) thisContext;
            List<RobotToken> contextTokens = ctx.getContextTokens();
            LineElement elem = new LineElement();
            FilePosition lastEnd = null;
            for (RobotToken t : contextTokens) {
                elem.appendValue(t.getText());
                lastEnd = t.getEndPosition();
            }
            List<ElementType> types = new LinkedList<>();
            types.add(BUILD_TYPE);
            elem.setElementTypes(types);
            lineElements.add(elem);

            mapOut.setMappedElementType(BUILD_TYPE);
            mapOut.setNextPosition(lastEnd);
        }

        return mapOut;
    }


    @VisibleForTesting
    protected boolean isContextCorrect(final List<LineElement> lineElements,
            final SeparationType separatorType) {
        boolean result = lineElements.isEmpty();

        int lineElementSize = lineElements.size();
        for (int i = 0; i < lineElementSize; i++) {
            LineElement e = lineElements.get(i);
            ElementType theMostImportantType = e.getElementTypes().get(0);

            if (theMostImportantType == ElementType.PIPE_SEPARATOR) {
                if (separatorType == SeparationType.PIPE) {
                    if (i == 0) {
                        result = true;
                    } else {
                        result = false;
                        break;
                    }
                } else {
                    result = false;
                    break;
                }
            } else if (theMostImportantType == ElementType.WHITESPACE_SEPARATOR) {
                if (separatorType == SeparationType.PIPE) {
                    result = true;
                } else {
                    result = false;
                    break;
                }
            } else if (theMostImportantType == ElementType.PRETTY_ALIGN) {
                if (separatorType == SeparationType.PIPE) {
                    result = true;
                } else {
                    if (i == 0) {
                        result = true;
                    } else {
                        result = false;
                        break;
                    }
                }
            } else {
                result = false;
                break;
            }
        }

        return result;
    }
}
