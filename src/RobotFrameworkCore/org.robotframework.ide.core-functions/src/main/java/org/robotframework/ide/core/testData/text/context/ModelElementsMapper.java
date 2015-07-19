package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput.BuildMessage;
import org.robotframework.ide.core.testData.text.context.iterator.ContextTokenIterator.SeparationType;
import org.robotframework.ide.core.testData.text.context.mapper.IContextMapper;
import org.robotframework.ide.core.testData.text.context.mapper.MapperOutput;
import org.robotframework.ide.core.testData.text.context.mapper.MapperTemporaryStore;
import org.robotframework.ide.core.testData.text.context.mapper.SettingTableHeaderMapper;
import org.robotframework.ide.core.testData.text.context.mapper.VariableTableHeaderMapper;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class ModelElementsMapper {

    private final Map<IContextElementType, IContextMapper> contextMappers = new LinkedHashMap<>();
    private final ContextOperationHelper coh;


    public ModelElementsMapper() {
        this.coh = new ContextOperationHelper();
        contextMappers.put(SimpleRobotContextType.SETTING_TABLE_HEADER,
                new SettingTableHeaderMapper());
        contextMappers.put(SimpleRobotContextType.VARIABLE_TABLE_HEADER,
                new VariableTableHeaderMapper());
        contextMappers.put(SimpleRobotContextType.TEST_CASE_TABLE_HEADER,
                new TestCaseTableHeaderMapper());
        contextMappers.put(SimpleRobotContextType.KEYWORD_TABLE_HEADER,
                new KeywordTableHeaderMapper());
    }


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

        if (tokensWithoutContext.isEmpty()) {
            if (nearestCtxs.isEmpty()) {
                throw new IllegalStateException(
                        "It is internal problem, because we have not any kind of context found and any garbage text.");
            } else {
                IContextElement mostImportantContext = findMostImportantContext(nearestCtxs);
                if (mostImportantContext instanceof OneLineSingleRobotContextPart) {
                    OneLineSingleRobotContextPart c = (OneLineSingleRobotContextPart) mostImportantContext;
                    List<RobotToken> contextTokens = c.getContextTokens();
                    FilePosition start = contextTokens.get(0)
                            .getStartPosition();
                    FilePosition end = contextTokens.get(
                            contextTokens.size() - 1).getEndPosition();
                    IContextElementType ctxType = mostImportantContext
                            .getType();

                    if (isTableHeader(ctxType)) {
                        List<IContextElement> breakContextElems = findSeparatorContextsWhichBreakCurrentContext(
                                mostImportantContext, separatorCtxs,
                                separatorType);
                        if (breakContextElems.isEmpty()) {
                            IContextMapper mapper = contextMappers
                                    .get(mostImportantContext.getType());
                            if (mapper != null) {
                                mapOut = mapper
                                        .map(store, mostImportantContext);
                            } else {
                                throw new IllegalStateException(
                                        "Unsupported context found "
                                                + mostImportantContext);
                            }
                        } else {
                            if (lastType == null
                                    || lastType == ElementType.TRASH_DATA) {
                                handleBreakContextBySeparator(store, mapOut, c,
                                        model, start, end);
                            } else {
                                // ??!!
                            }
                        }
                    } else if (ctxType == SimpleRobotContextType.PRETTY_ALIGN) {
                        handlePrettyAlign(store, mapOut, contextTokens, model,
                                start, end);
                    } else {
                        // other contexts
                    }
                } else {
                    coh.reportProblemWithType(mostImportantContext);
                }
            }
        } else {
            RobotToken theFirstTrashToken = tokensWithoutContext.get(0);
            FilePosition trashDataBeginPosition = theFirstTrashToken
                    .getStartPosition();
            FilePosition trashDataEndPosition = tokensWithoutContext.get(
                    tokensWithoutContext.size() - 1).getEndPosition();
            if (lastType == null
                    && isSingleSpacePrettyAlign(tokensWithoutContext)) {
                handlePrettyAlign(store, mapOut, tokensWithoutContext, model,
                        trashDataBeginPosition, trashDataEndPosition);
            } else if (lastType == null || lastType == ElementType.TRASH_DATA) {
                handleTrashData(store, mapOut, tokensWithoutContext, model,
                        trashDataBeginPosition, trashDataEndPosition);
            } else if (theFirstTrashToken.getType() == RobotSingleCharTokenType.CARRIAGE_RETURN
                    || theFirstTrashToken.getType() == RobotSingleCharTokenType.LINE_FEED) {
                mapOut = mapLineEnd(store, tokensWithoutContext, model);
            } else {
                // could be inside test case or comment
            }
        }

        return mapOut;
    }


    @VisibleForTesting
    protected MapperOutput mapLineEnd(final MapperTemporaryStore store,
            final List<RobotToken> tokensEndLine, final ModelOutput model) {
        final MapperOutput mapOut = new MapperOutput();

        LineElement element = new LineElement();
        FilePosition lineEndPosition = null;
        for (RobotToken t : tokensEndLine) {
            element.appendValue(t.getText());
            lineEndPosition = t.getEndPosition();
        }
        List<ElementType> types = new LinkedList<>();
        types.add(ElementType.LINE_END);
        element.setElementTypes(types);
        store.appendCurrentLineElements(element);
        mapOut.setNextPosition(lineEndPosition);
        mapOut.setMappedElementType(ElementType.LINE_END);
        return mapOut;
    }


    @VisibleForTesting
    protected boolean isSingleSpacePrettyAlign(
            final List<RobotToken> tokensWithoutContext) {
        boolean result = false;
        if (tokensWithoutContext.size() == 1) {
            RobotToken robotToken = tokensWithoutContext.get(0);
            result = (robotToken.getType() == RobotSingleCharTokenType.SINGLE_SPACE);
        }

        return result;
    }


    private void handleBreakContextBySeparator(
            final MapperTemporaryStore store, final MapperOutput mapOut,
            final OneLineSingleRobotContextPart invalidContext,
            final ModelOutput model, FilePosition trashDataBeginPosition,
            FilePosition trashDataEndPosition) {
        model.addBuildMessage(BuildMessage.buildWarn("Section "
                + invalidContext.getType() + " was break by separator.",
                "section start " + trashDataBeginPosition + ", end "
                        + trashDataEndPosition));
        ElementType additionalType = mapTypes(invalidContext.getType());
        List<LineElement> trashData = convertToTrashData(invalidContext
                .getContextTokens());
        for (LineElement le : trashData) {
            le.addNewType(additionalType);
        }
        store.appendCurrentLineElements(trashData);
        mapOut.setNextPosition(trashDataEndPosition);
        mapOut.setMappedElementType(ElementType.TRASH_DATA);
    }


    private ElementType mapTypes(IContextElementType type) {
        ElementType ets = ElementType.VALUE;
        if (type == SimpleRobotContextType.SETTING_TABLE_HEADER) {
            ets = ElementType.SETTING_TABLE_HEADER;
        } else if (type == SimpleRobotContextType.VARIABLE_TABLE_HEADER) {
            ets = ElementType.VARIABLE_TABLE_HEADER;
        } else if (type == SimpleRobotContextType.TEST_CASE_TABLE_HEADER) {
            ets = ElementType.TEST_CASE_TABLE_HEADER;
        } else if (type == SimpleRobotContextType.KEYWORD_TABLE_HEADER) {
            ets = ElementType.KEYWORD_TABLE_HEADER;
        }

        return ets;
    }


    private void handlePrettyAlign(final MapperTemporaryStore store,
            MapperOutput mapOut, final List<RobotToken> tokensWithoutContext,
            final ModelOutput model, FilePosition dataBeginPosition,
            FilePosition dataEndPosition) {
        store.setCurrentLineElements(convertToPrettyAlign(tokensWithoutContext));
        mapOut.setNextPosition(dataEndPosition);
        mapOut.setMappedElementType(ElementType.PRETTY_ALIGN);
    }


    private void handleTrashData(final MapperTemporaryStore store,
            MapperOutput mapOut, final List<RobotToken> tokensWithoutContext,
            final ModelOutput model, FilePosition trashDataBeginPosition,
            FilePosition trashDataEndPosition) {
        addInformationAboutTrashDataFound(model, trashDataBeginPosition,
                trashDataEndPosition);
        store.setCurrentLineElements(convertToTrashData(tokensWithoutContext));
        mapOut.setNextPosition(trashDataEndPosition);
        mapOut.setMappedElementType(ElementType.TRASH_DATA);
    }


    @VisibleForTesting
    protected List<IContextElement> findSeparatorContextsWhichBreakCurrentContext(
            final IContextElement contextToCheck,
            final List<IContextElement> separators,
            final SeparationType separatorType) {
        List<IContextElement> breakContextSeparators = null;

        if (separatorType == SeparationType.PIPE) {
            breakContextSeparators = new LinkedList<>();
        } else {
            if (contextToCheck instanceof OneLineSingleRobotContextPart) {
                OneLineSingleRobotContextPart c = (OneLineSingleRobotContextPart) contextToCheck;
                List<RobotToken> contextTokens = c.getContextTokens();
                FilePosition start = contextTokens.get(0).getStartPosition();
                FilePosition end = contextTokens.get(contextTokens.size() - 1)
                        .getEndPosition();
                breakContextSeparators = findSeparatorsInsideContext(
                        separators,
                        SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED,
                        start, end);
            } else {
                coh.reportProblemWithType(contextToCheck);
            }
        }

        return breakContextSeparators;
    }


    @VisibleForTesting
    protected List<IContextElement> findSeparatorsInsideContext(
            final List<IContextElement> separators,
            final IContextElementType separatorType, final FilePosition start,
            final FilePosition end) {
        List<IContextElement> foundContextBetweenPosition = coh
                .findContextBetweenPosition(separators, start, end);
        return coh.filterByType(foundContextBetweenPosition, separatorType);
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
    protected List<LineElement> convertToPrettyAlign(
            final List<RobotToken> tokens) {
        List<LineElement> elems = new LinkedList<>();
        for (RobotToken rt : tokens) {
            LineElement le = new LineElement();
            List<ElementType> et = new LinkedList<>();
            et.add(0, ElementType.PRETTY_ALIGN);
            le.setElementTypes(et);
            le.setValue(rt.getText());
            elems.add(le);
        }

        return elems;
    }


    @VisibleForTesting
    protected List<LineElement> convertToTrashData(final List<RobotToken> tokens) {
        List<LineElement> elems = new LinkedList<>();
        for (RobotToken rt : tokens) {
            LineElement le = new LineElement();
            List<ElementType> et = new LinkedList<>();
            et.add(0, ElementType.TRASH_DATA);
            le.setElementTypes(et);
            le.setValue(rt.getText());
            elems.add(le);
        }

        return elems;
    }
}
