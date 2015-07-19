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
        System.out.println("N: " + normalCtxs);
        System.out.println("SEP: " + separatorCtxs);
        System.out.println("NEAR: " + nearestCtxs);
        System.out.println("GD: " + tokensWithoutContext);
        System.out.println("----");

        if (tokensWithoutContext.isEmpty()) {
            if (nearestCtxs.isEmpty()) {
                throw new IllegalStateException(
                        "It is internal problem, because we have not any kind of context found and any garbage text.");
            } else {
                IContextElement mostImportantContext = findMostImportantContext(nearestCtxs);

            }
        } else {
            FilePosition trashDataBeginPosition = tokensWithoutContext.get(0)
                    .getStartPosition();
            FilePosition trashDataEndPosition = tokensWithoutContext.get(
                    tokensWithoutContext.size() - 1).getEndPosition();
            if (lastType == null || lastType == ElementType.TRASH_DATA) {
                addInformationAboutTrashDataFound(model,
                        trashDataBeginPosition, trashDataEndPosition);
                store.setCurrentLineElements(convertToTrashData(tokensWithoutContext));
                mapOut.setNextPosition(trashDataEndPosition);
                mapOut.setMappedElementType(ElementType.TRASH_DATA);
            } else {

            }
        }

        return mapOut;
    }


    @VisibleForTesting
    protected IContextElement findMostImportantContext(
            final List<IContextElement> contexts) {
        final int TABLE_HEADER_PRIO = 100;
        final int EMPTY_LINE = TABLE_HEADER_PRIO - 1;
        final int ESCAPED = EMPTY_LINE - 1;
        final int DECLARED_COMMENT = ESCAPED - 1;
        final int QUOTES_SENTENCE = DECLARED_COMMENT - 1;
        final int CONTINUE_PREV_LINE = QUOTES_SENTENCE - 1;
        final int EMPTY_CELL = CONTINUE_PREV_LINE - 1;
        final int PRETTY_ALIGNS = EMPTY_CELL - 1;
        final int UNICODE = PRETTY_ALIGNS - 1;
        final int VARIABLE = UNICODE - 1;
        final int POS_IN_COLLECTION = VARIABLE - 1;
        final int CONTINUE_LOOP = POS_IN_COLLECTION - 1;
        final int FOR_LOOP = CONTINUE_LOOP - 1;
        final int NOT_FOUND = -1;

        int priority = NOT_FOUND;
        IContextElement theMostImportantContext = null;
        for (IContextElement c : contexts) {
            IContextElementType type = c.getType();
            if (isTableHeader(type)) {
                if (priority < TABLE_HEADER_PRIO) {
                    theMostImportantContext = c;
                    priority = TABLE_HEADER_PRIO;
                    break;
                }
            } else if (type == SimpleRobotContextType.EMPTY_LINE) {
                if (priority < EMPTY_LINE) {
                    theMostImportantContext = c;
                    priority = EMPTY_LINE;
                }
            } else if (isEscapedData(type)) {
                if (priority < ESCAPED) {
                    theMostImportantContext = c;
                    priority = ESCAPED;
                }
            } else if (type == SimpleRobotContextType.DECLARED_COMMENT) {
                if (priority < DECLARED_COMMENT) {
                    theMostImportantContext = c;
                    priority = DECLARED_COMMENT;
                }
            } else if (type == SimpleRobotContextType.QUOTES_SENTENCE) {
                if (priority < QUOTES_SENTENCE) {
                    theMostImportantContext = c;
                    priority = QUOTES_SENTENCE;
                }
            } else if (type == SimpleRobotContextType.CONTINUE_PREVIOUS_LINE_DECLARATION) {
                if (priority < CONTINUE_PREV_LINE) {
                    theMostImportantContext = c;
                    priority = CONTINUE_PREV_LINE;
                }
            } else if (type == SimpleRobotContextType.EMPTY_CELL) {
                if (priority < EMPTY_CELL) {
                    theMostImportantContext = c;
                    priority = EMPTY_CELL;
                }
            } else if (isPrettyAlignment(type)) {
                if (priority < PRETTY_ALIGNS) {
                    theMostImportantContext = c;
                    priority = PRETTY_ALIGNS;
                }
            } else if (isUnicode(type)) {
                if (priority < UNICODE) {
                    theMostImportantContext = c;
                    priority = UNICODE;
                }
            } else if (isVariable(type)) {
                if (priority < VARIABLE) {
                    theMostImportantContext = c;
                    priority = VARIABLE;
                }
            } else if (type == SimpleRobotContextType.COLLECTION_TYPE_VARIABLE_POSITION) {
                if (priority < POS_IN_COLLECTION) {
                    theMostImportantContext = c;
                    priority = POS_IN_COLLECTION;
                }
            } else if (type == SimpleRobotContextType.CONTINOUE_LOOP_DECLARATION) {
                if (priority < CONTINUE_LOOP) {
                    theMostImportantContext = c;
                    priority = CONTINUE_LOOP;
                }
            } else if (type == SimpleRobotContextType.FOR_LOOP_DECLARATION) {
                if (priority < FOR_LOOP) {
                    theMostImportantContext = c;
                    priority = FOR_LOOP;
                }
            } else {
                if (priority == NOT_FOUND) {
                    theMostImportantContext = c;
                    priority = 0;
                }
            }
        }

        return theMostImportantContext;
    }


    @VisibleForTesting
    protected boolean isVariable(final IContextElementType type) {
        return (type == SimpleRobotContextType.SCALAR_VARIABLE
                || type == SimpleRobotContextType.LIST_VARIABLE
                || type == SimpleRobotContextType.DICTIONARY_VARIABLE || type == SimpleRobotContextType.ENVIRONMENT_VARIABLE);
    }


    @VisibleForTesting
    protected boolean isUnicode(final IContextElementType type) {
        return (type == SimpleRobotContextType.CHAR_WITH_BYTE_HEX_VALUE
                || type == SimpleRobotContextType.CHAR_WITH_SHORT_HEX_VALUE || type == SimpleRobotContextType.UNICODE_CHAR_WITH_HEX_VALUE);
    }


    @VisibleForTesting
    protected boolean isPrettyAlignment(final IContextElementType type) {
        return (type == SimpleRobotContextType.PRETTY_ALIGN
                || type == SimpleRobotContextType.LINE_FEED_TEXT || type == SimpleRobotContextType.TABULATOR_TEXT);
    }


    @VisibleForTesting
    protected boolean isEscapedData(final IContextElementType type) {
        return (type == SimpleRobotContextType.ESCAPED_DOLLAR_SIGN
                || type == SimpleRobotContextType.ESCAPED_AT_SIGN
                || type == SimpleRobotContextType.ESCAPED_PROCENT_SIGN
                || type == SimpleRobotContextType.ESCAPED_HASH_SIGN
                || type == SimpleRobotContextType.ESCAPED_EQUALS_SIGN
                || type == SimpleRobotContextType.ESCAPED_PIPE_SIGN
                || type == SimpleRobotContextType.ESCAPED_BACKSLASH_SIGN || type == SimpleRobotContextType.ESCAPED_SPACE);
    }


    @VisibleForTesting
    protected boolean isTableHeader(final IContextElementType type) {
        return (type == SimpleRobotContextType.SETTING_TABLE_HEADER
                || type == SimpleRobotContextType.VARIABLE_TABLE_HEADER
                || type == SimpleRobotContextType.TEST_CASE_TABLE_HEADER || type == SimpleRobotContextType.KEYWORD_TABLE_HEADER);
    }


    @VisibleForTesting
    protected void addInformationAboutTrashDataFound(final ModelOutput model,
            final FilePosition trashBegin, final FilePosition trashEnd) {
        model.addBuildMessage(BuildMessage
                .buildWarn(
                        "Garbage data found in file. It is recommand to use hash sign ('#') to comment out some user text without meaning.",
                        "start " + trashBegin + ", end " + trashEnd));
    }


    @VisibleForTesting
    protected List<LineElement> convertToTrashData(final List<RobotToken> tokens) {
        List<LineElement> elems = new LinkedList<>();
        for (RobotToken rt : tokens) {
            LineElement le = new LineElement();
            le.setElementTypes(Arrays.asList(ElementType.TRASH_DATA));
            le.setValue(rt.getText());
            elems.add(le);
        }

        return elems;
    }
}
