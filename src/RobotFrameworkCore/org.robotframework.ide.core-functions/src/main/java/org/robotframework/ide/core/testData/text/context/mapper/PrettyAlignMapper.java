package org.robotframework.ide.core.testData.text.context.mapper;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;


public class PrettyAlignMapper implements IContextMapper {

    @Override
    public MapperOutput map(MapperTemporaryStore store,
            IContextElement thisContext) {
        MapperOutput mapOut = new MapperOutput();

        // restore from temp
        ElementType lastType = store.getLastType();
        final List<LineElement> elems = store.getCurrentLineElements();

        if (lastType == null) {
            OneLineSingleRobotContextPart ctx = (OneLineSingleRobotContextPart) thisContext;
            FilePosition theLast = null;
            List<RobotToken> contextTokens = ctx.getContextTokens();

            for (RobotToken t : contextTokens) {
                LineElement elem = new LineElement();
                List<ElementType> types = new LinkedList<>();
                types.add(ElementType.PRETTY_ALIGN);
                elem.setElementTypes(types);
                elems.add(elem);
                theLast = t.getEndPosition();
            }

            mapOut.setMappedElementType(ElementType.PRETTY_ALIGN);
            mapOut.setNextPosition(theLast);
        }

        return mapOut;
    }

}
