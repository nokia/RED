package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.iterator.ContextTokenIterator.SeparationType;
import org.robotframework.ide.core.testData.text.context.mapper.IContextMapper;
import org.robotframework.ide.core.testData.text.context.mapper.MapperOutput;
import org.robotframework.ide.core.testData.text.context.mapper.MapperTemporaryStore;
import org.robotframework.ide.core.testData.text.context.mapper.PrettyAlignMapper;
import org.robotframework.ide.core.testData.text.context.mapper.SettingTableHeaderMapper;
import org.robotframework.ide.core.testData.text.context.mapper.VariableTableHeaderMapper;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class ModelElementsMapper {

    private final Map<IContextElementType, IContextMapper> contextMappers = new LinkedHashMap<>();


    public ModelElementsMapper() {
        contextMappers.put(SimpleRobotContextType.SETTING_TABLE_HEADER,
                new SettingTableHeaderMapper());
        contextMappers.put(SimpleRobotContextType.VARIABLE_TABLE_HEADER,
                new VariableTableHeaderMapper());
        contextMappers.put(SimpleRobotContextType.TEST_CASE_TABLE_HEADER,
                new TestCaseTableHeaderMapper());
        contextMappers.put(SimpleRobotContextType.KEYWORD_TABLE_HEADER,
                new KeywordTableHeaderMapper());
        contextMappers.put(SimpleRobotContextType.PRETTY_ALIGN,
                new PrettyAlignMapper());
    }


    public MapperOutput map(final MapperTemporaryStore store) {
        MapperOutput mapOut = new MapperOutput();

        // extract elements from temp store
        ElementType lastType = store.getLastType();
        List<LineElement> currentLineElements = store.getCurrentLineElements();
        final List<IContextElement> nearestCtxs = store.getNearestContexts();
        final List<RobotToken> tokensWithoutContext = store
                .getTokensWithoutContext();
        SeparationType separatorType = store.getSeparatorType();
        final List<IContextElement> separatorContexts = store
                .getSeparatorContexts();
        if (tokensWithoutContext.isEmpty()) {
            if (nearestCtxs.isEmpty()) {
                throw new IllegalStateException(
                        "It is internal problem, because we have not any kind of context found and any garbage text.");
            } else {
                IContextElement ctx = nearestCtxs.get(0);
                IContextElementType ctxType = ctx.getType();
                if (isTableHeader(ctxType)) {
                    IContextMapper mapper = contextMappers.get(ctxType);
                    mapOut = mapper.map(store, ctx);
                }

                if (mapOut.getMappedElementType() == null
                        && ctxType == SimpleRobotContextType.PRETTY_ALIGN) {
                    IContextMapper mapper = contextMappers.get(ctxType);
                    mapOut = mapper.map(store, ctx);
                }

                if (mapOut.getMappedElementType() == null) {
                    OneLineSingleRobotContextPart ctxP = (OneLineSingleRobotContextPart) ctx;
                    wrongCaseHandler(mapOut, currentLineElements,
                            ctxP.getContextTokens(), separatorType,
                            separatorContexts);
                }
            }
        } else {
            wrongCaseHandler(mapOut, currentLineElements, tokensWithoutContext,
                    separatorType, separatorContexts);
        }

        return mapOut;
    }


    @VisibleForTesting
    protected void wrongCaseHandler(MapperOutput mapOut,
            List<LineElement> currentLineElements,
            final List<RobotToken> tokensWithoutContext,
            final SeparationType sepType,
            final List<IContextElement> separatorContexts) {
        RobotToken rt = tokensWithoutContext.get(0);
        IRobotTokenType type = rt.getType();
        if (type == RobotSingleCharTokenType.SINGLE_SPACE
                && tokensWithoutContext.size() == 1) {
            handlePrettyAlignAtTheBegin(mapOut, currentLineElements, rt);
        } else if (type == RobotSingleCharTokenType.CARRIAGE_RETURN
                || type == RobotSingleCharTokenType.LINE_FEED) {
            handleLineEnd(mapOut, currentLineElements, tokensWithoutContext);
        } else if (checkIfCouldBeHeaderUserColumns(currentLineElements)) {
            if (checkIfContainsMandatorySeparatorAsLast(currentLineElements)) {
                handleHeaderColumns(mapOut, currentLineElements,
                        tokensWithoutContext, sepType, separatorContexts);
            } else {
                // is part of keyword or some other context if it appears if
                // not this is just trash
                System.out.println("P");
            }
        } else {
            System.out.println("O");
        }
    }


    @VisibleForTesting
    protected boolean checkIfContainsMandatorySeparatorAsLast(
            final List<LineElement> currentLineElements) {
        boolean result = false;

        int size = currentLineElements.size() - 1;
        for (int i = size; i > 0; i--) {
            LineElement le = currentLineElements.get(i);
            ElementType elementType = le.getElementTypes().get(0);
            if (elementType == ElementType.PRETTY_ALIGN) {
                // nothing to do just skip
            } else if (elementType == ElementType.WHITESPACE_SEPARATOR
                    || elementType == ElementType.PIPE_SEPARATOR) {
                result = true;
                break;
            } else {
                result = false;
                break;
            }
        }

        return result;
    }


    @VisibleForTesting
    protected boolean checkIfCouldBeHeaderUserColumns(
            final List<LineElement> currentLineElements) {
        boolean result = false;

        for (LineElement le : currentLineElements) {
            ElementType theMostImportant = le.getElementTypes().get(0);
            if (isTableHeader(theMostImportant)) {
                result = true;
                break;
            }
        }

        return result;
    }


    @VisibleForTesting
    protected void handleHeaderColumns(MapperOutput mapOut,
            List<LineElement> currentLineElements,
            final List<RobotToken> tokensWithoutContext,
            final SeparationType separatorType,
            final List<IContextElement> separatorContexts) {
        LineElement lineElement = new LineElement();
        FilePosition end = null;

        int toCopy = getSeparatorIndexInColumnName(tokensWithoutContext,
                separatorContexts);
        if (toCopy == -1) {
            toCopy = tokensWithoutContext.size();
        }

        for (int i = 0; i < toCopy; i++) {
            RobotToken t = tokensWithoutContext.get(i);
            end = t.getEndPosition();
            lineElement.appendValue(t.getText());
        }
        mapOut.setNextPosition(end);
        List<ElementType> types = new LinkedList<>();
        types.add(ElementType.TABLE_COLUMN_NAME);
        mapOut.setMappedElementType(ElementType.TABLE_COLUMN_NAME);

        lineElement.setElementTypes(types);
        currentLineElements.add(lineElement);
    }


    @VisibleForTesting
    protected int getSeparatorIndexInColumnName(
            final List<RobotToken> tokensWithoutContext,
            final List<IContextElement> separatorContexts) {
        int index = -1;
        if (!separatorContexts.isEmpty()) {
            OneLineSingleRobotContextPart ctx = (OneLineSingleRobotContextPart) separatorContexts
                    .get(0);
            RobotToken sepFirstToken = ctx.getContextTokens().get(0);
            FilePosition pos = sepFirstToken.getStartPosition();
            int size = tokensWithoutContext.size();
            for (int i = 0; i < size; i++) {
                RobotToken t = tokensWithoutContext.get(i);
                if (t.getStartPosition().getColumn() == pos.getColumn()) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }


    @VisibleForTesting
    protected void handlePrettyAlignAtTheBegin(MapperOutput mapOut,
            List<LineElement> currentLineElements, RobotToken rt) {
        LineElement lineElement = new LineElement();
        lineElement.appendValue(rt.getText());
        FilePosition end = rt.getEndPosition();
        mapOut.setNextPosition(end);
        List<ElementType> types = new LinkedList<>();
        types.add(ElementType.PRETTY_ALIGN);
        mapOut.setMappedElementType(ElementType.PRETTY_ALIGN);

        lineElement.setElementTypes(types);
        currentLineElements.add(lineElement);
    }


    @VisibleForTesting
    protected void handleLineEnd(MapperOutput mapOut,
            List<LineElement> currentLineElements,
            final List<RobotToken> tokensWithoutContext) {
        LineElement lineElement = new LineElement();
        FilePosition end = null;
        for (RobotToken t : tokensWithoutContext) {
            lineElement.appendValue(t.getText());
            end = t.getEndPosition();
        }
        mapOut.setNextPosition(end);
        List<ElementType> types = new LinkedList<>();
        types.add(ElementType.LINE_END);
        mapOut.setMappedElementType(ElementType.LINE_END);

        lineElement.setElementTypes(types);
        currentLineElements.add(lineElement);
    }


    @VisibleForTesting
    protected ElementType mapTypes(IContextElementType type) {
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
    protected boolean isTableHeader(final ElementType type) {
        return (type == ElementType.SETTING_TABLE_HEADER
                || type == ElementType.VARIABLE_TABLE_HEADER
                || type == ElementType.TEST_CASE_TABLE_HEADER || type == ElementType.KEYWORD_TABLE_HEADER);
    }
}
