package org.robotframework.ide.core.testData.text.context;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput.BuildMessage;
import org.robotframework.ide.core.testData.text.context.iterator.ContextTokenIterator.SeparationType;
import org.robotframework.ide.core.testData.text.context.mapper.MapperOutput;
import org.robotframework.ide.core.testData.text.context.mapper.MapperTemporaryStore;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class ModelElementsMapper {

    public MapperOutput map(final MapperTemporaryStore store) {
        MapperOutput mapOut = new MapperOutput();

        // extract elements from temp store
        ElementType lastType = store.getLastType();
        final List<LineElement> lineElements = store.getCurrentLineElements();
        final List<IContextElement> nearestCtxs = store.getNearestContexts();
        final List<IContextElement> normalCtxs = store.getNormalContexts();
        final List<IContextElement> separatorCtxs = store
                .getSeparatorContexts();
        final List<RobotToken> tokensWithoutContext = store
                .getTokensWithoutContext();
        final SeparationType separatorType = store.getSeparatorType();
        final ModelOutput model = store.getModel();

        // System.out.println(nearestCtxs);
        return mapOut;
    }


    @VisibleForTesting
    protected boolean isTableHeaderContext(final IContextElementType type) {
        return (type == SimpleRobotContextType.SETTING_TABLE_HEADER
                || type == SimpleRobotContextType.VARIABLE_TABLE_HEADER
                || type == SimpleRobotContextType.TEST_CASE_TABLE_HEADER || type == SimpleRobotContextType.KEYWORD_TABLE_HEADER);
    }


    @VisibleForTesting
    protected FilePosition getLastPositionForTrashToken(
            final List<RobotToken> tokensWithoutContext) {
        return tokensWithoutContext.get(tokensWithoutContext.size() - 1)
                .getEndPosition();
    }


    @VisibleForTesting
    protected void addInfoMessageAboutTrashData(
            final List<RobotToken> tokensWithoutContext,
            final ModelOutput model, final FilePosition theLastTokenPositionEnd) {
        FilePosition theFirstToken = tokensWithoutContext.get(0)
                .getStartPosition();
        model.addBuildMessage(BuildMessage
                .buildInfo(
                        "Some garbage line found, better is to use syntax like comments start from '#'.",
                        "Starts in line " + theFirstToken.getLine()
                                + " column " + theFirstToken.getColumn()
                                + ", ends in line "
                                + theLastTokenPositionEnd.getLine()
                                + " column "
                                + theLastTokenPositionEnd.getColumn()));
    }


    @VisibleForTesting
    protected List<LineElement> convertTrashTokensToElements(
            final List<RobotToken> tokens) {
        List<LineElement> trash = new LinkedList<>();

        for (RobotToken t : tokens) {
            LineElement e = new LineElement();
            e.setValue(t.getText());
            e.setElementTypes(Arrays.asList(ElementType.TRASH_DATA));
            trash.add(e);
        }

        return trash;
    }
}
