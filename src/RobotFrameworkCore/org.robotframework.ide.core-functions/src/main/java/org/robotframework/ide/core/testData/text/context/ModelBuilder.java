package org.robotframework.ide.core.testData.text.context;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;
import org.robotframework.ide.core.testData.model.RobotTestDataFile;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput.BuildMessage;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput.BuildMessage.Level;
import org.robotframework.ide.core.testData.text.context.iterator.ContextTokenIterator;
import org.robotframework.ide.core.testData.text.context.iterator.RobotSeparatorIteratorOutput;
import org.robotframework.ide.core.testData.text.context.iterator.SeparatorBaseIteratorBuilder;
import org.robotframework.ide.core.testData.text.context.mapper.MapperOutput;
import org.robotframework.ide.core.testData.text.context.mapper.MapperTemporaryStore;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class ModelBuilder {

    private final SeparatorBaseIteratorBuilder separatorIterBuilder;
    private final ContextOperationHelper ctxHelper;
    private final ModelElementsMapper mapper;


    public ModelBuilder() {
        this.separatorIterBuilder = new SeparatorBaseIteratorBuilder();
        this.ctxHelper = new ContextOperationHelper();
        this.mapper = new ModelElementsMapper();
    }


    public ModelOutput build(final ContextOutput contexts) {
        ModelOutput output = new ModelOutput();

        List<AggregatedOneLineRobotContexts> lineContexts = contexts
                .getContexts();
        ElementType globalLineContext = null;
        for (AggregatedOneLineRobotContexts ctx : lineContexts) {
            globalLineContext = mapLine(output, ctx, globalLineContext);
        }

        System.out.println(output.getFileModel().getContent());
        return output;
    }


    @VisibleForTesting
    protected ElementType mapLine(final ModelOutput model,
            final AggregatedOneLineRobotContexts ctx, final ElementType etLast) {
        // prepare for mapping line
        RobotLine currentLine = new RobotLine();
        ElementType mappedType = etLast;
        final ContextTokenIterator separatorBaseIterator = separatorIterBuilder
                .createSeparatorBaseIterator(ctx);
        List<RobotToken> lineTokens = ctxHelper.getWholeLineTokens(ctx);

        RobotLineSeparatorsContexts separators = ctx.getSeparators();
        List<IContextElement> separatorsContexts = separators
                .getFoundSeperatorsExcludeType().values();
        List<IContextElement> childContexts = ctx.getChildContexts();

        FilePosition fp = FilePosition.createMarkerForFirstColumn(separators
                .getLineNumber());
        List<LineElement> elems = new LinkedList<>();
        MapperTemporaryStore mappingTempStore = new MapperTemporaryStore(model,
                elems, currentLine);
        mappingTempStore.setSeparatorType(separatorBaseIterator
                .getSeparatorType());
        while(separatorBaseIterator.hasNext(fp)) {
            RobotSeparatorIteratorOutput next = separatorBaseIterator.next(fp);
            if (next != null) {
                if (next.hasSeparator()) {
                    // if this is separator we are adding it to elems list
                    // representing one line of Robot test data file
                    fp = addSeparator(fp, elems, next);
                } else {
                    // search for contexts, which have current column position
                    // equal or greater than current position in line
                    childContexts = ctxHelper.filterContextsByColumn(
                            childContexts, fp);
                    separatorsContexts = ctxHelper.filterContextsByColumn(
                            separatorsContexts, fp);

                    mappingTempStore.setNormalContexts(childContexts);
                    mappingTempStore.setSeparatorContexts(separatorsContexts);

                    // search for contexts which are the nearest to our current
                    // position
                    List<IContextElement> separatorsAndNormalCtxs = new LinkedList<>(
                            childContexts);
                    separatorsAndNormalCtxs.addAll(separatorsContexts);
                    List<IContextElement> nearestCtxs = ctxHelper
                            .findNearestContexts(separatorsAndNormalCtxs, fp);
                    mappingTempStore.setNearestContexts(nearestCtxs);

                    List<RobotToken> gapTokens = findGapTokens(nearestCtxs,
                            lineTokens, fp);
                    mappingTempStore.setTokensWithoutContext(gapTokens);
                    mappingTempStore.setLastType(mappedType);
                    MapperOutput mapOut = mapper.map(mappingTempStore);
                    mappedType = mapOut.getMappedElementType();
                    fp = mapOut.getNextPosition();
                }
            } else {
                model.addBuildMessage(BuildMessage.buildError(
                        "Internall error in " + separatorBaseIterator
                                + " returns null as next position", "line: "
                                + separators.getLineNumber() + ", position: "
                                + fp));
                break;
            }
        }
        currentLine.setElements(elems);
        model.getFileModel().addNextRobotLine(currentLine);

        return mappedType;
    }


    @VisibleForTesting
    protected FilePosition addSeparator(FilePosition fp,
            List<LineElement> elems, RobotSeparatorIteratorOutput next) {
        int separatorLength = 0;
        if (next.hasLeftPrettyAlign()) {
            LineElement leftAlign = new LineElement();
            leftAlign.setValue(next.getLeftPrettyAlign());
            leftAlign.setElementTypes(Arrays.asList(ElementType.PRETTY_ALIGN));
            elems.add(leftAlign);

            separatorLength += leftAlign.getValue().length();
        }

        LineElement separator = new LineElement();
        separator.setValue(next.getSeparator());
        separator.setElementTypes(getSeparatorTypes(next));
        separatorLength += separator.getValue().length();
        elems.add(separator);

        if (next.hasRightPrettyAlign()) {
            LineElement rightAlign = new LineElement();
            rightAlign.setValue(next.getRightPrettyAlign());
            rightAlign.setElementTypes(Arrays.asList(ElementType.PRETTY_ALIGN));
            elems.add(rightAlign);

            separatorLength += rightAlign.getValue().length();
        }

        return new FilePosition(fp.getLine(), fp.getColumn() + separatorLength);
    }


    @VisibleForTesting
    protected List<RobotToken> findGapTokens(
            final List<IContextElement> nearestCtxs,
            final List<RobotToken> tokens, final FilePosition fp) {
        List<RobotToken> gapTokens = new LinkedList<>();

        int column = fp.getColumn();
        int gapEndPosition = findGapEndPosition(nearestCtxs, tokens);
        for (RobotToken token : tokens) {
            int tokenStartColumn = token.getStartPosition().getColumn();
            if (column <= tokenStartColumn) {
                int tokenEndColumn = token.getEndPosition().getColumn();
                if (gapEndPosition >= tokenEndColumn) {
                    gapTokens.add(token);
                } else {
                    // all tokens from gap region collected
                    break;
                }
            }
        }

        return gapTokens;
    }


    @VisibleForTesting
    protected int findGapEndPosition(final List<IContextElement> nearestCtxs,
            List<RobotToken> lineTokens) {
        final int NOT_FOUND_POSITION = -1;
        int gapEndPosition = NOT_FOUND_POSITION;
        if (!nearestCtxs.isEmpty()) {
            // every contexts are have the same start position and contains
            // minimum one token
            for (IContextElement ctx : nearestCtxs) {
                if (ctx instanceof OneLineSingleRobotContextPart) {
                    OneLineSingleRobotContextPart currentCtx = (OneLineSingleRobotContextPart) ctx;
                    if (currentCtx.getType() != SimpleRobotContextType.UNDECLARED_COMMENT) {
                        List<RobotToken> contextTokens = currentCtx
                                .getContextTokens();
                        RobotToken token = contextTokens.get(0);
                        gapEndPosition = token.getStartPosition().getColumn();
                        break;
                    }
                } else {
                    ctxHelper.reportProblemWithType(ctx);
                }
            }
        }

        if (gapEndPosition == NOT_FOUND_POSITION) {
            FilePosition lastTokenEnd = lineTokens.get(lineTokens.size() - 1)
                    .getEndPosition();
            gapEndPosition = lastTokenEnd.getColumn();
        }

        return gapEndPosition;
    }


    private List<ElementType> getSeparatorTypes(
            final RobotSeparatorIteratorOutput next) {
        List<ElementType> type = new LinkedList<>();
        if (next.getSeparatorType() == SimpleRobotContextType.PIPE_SEPARATED) {
            type.add(ElementType.PIPE_SEPARATOR);
        } else {
            type.add(ElementType.WHITESPACE_SEPARATOR);
        }

        return type;
    }

    public static class ModelOutput {

        private final RobotTestDataFile fileModel;
        private final List<BuildMessage> buildMessages = new LinkedList<>();


        public ModelOutput() {
            this.fileModel = new RobotTestDataFile();
        }


        public ModelOutput(final RobotTestDataFile fileModel) {
            this.fileModel = fileModel;
        }


        public RobotTestDataFile getFileModel() {
            return fileModel;
        }


        public List<BuildMessage> getBuildMessages() {
            return buildMessages;
        }


        public void addBuildMessage(BuildMessage message) {
            buildMessages.add(message);
        }


        public boolean wasError() {
            boolean result = false;
            for (BuildMessage msg : buildMessages) {
                if (msg == null || msg.getType() == Level.ERROR) {
                    result = true;
                    break;
                }
            }

            return result;
        }

        public static class BuildMessage {

            private String message;
            private String localization;
            private Level type = Level.INFO;


            private BuildMessage(String message, String localization, Level type) {
                this.message = message;
                this.localization = localization;
                this.type = type;
            }


            public String getMessage() {
                return message;
            }


            public void setMessage(String message) {
                this.message = message;
            }


            public String getLocalization() {
                return localization;
            }


            public void setLocalization(String localization) {
                this.localization = localization;
            }


            public Level getType() {
                return type;
            }


            public void setType(Level type) {
                this.type = type;
            }


            public static BuildMessage buildDebug(String message,
                    String localization) {
                return new BuildMessage(message, localization, Level.DEBUG);
            }


            public static BuildMessage buildInfo(String message,
                    String localization) {
                return new BuildMessage(message, localization, Level.INFO);
            }


            public static BuildMessage buildWarn(String message,
                    String localization) {
                return new BuildMessage(message, localization, Level.WARN);
            }


            public static BuildMessage buildError(String message,
                    String localization) {
                return new BuildMessage(message, localization, Level.ERROR);
            }

            public static enum Level {
                DEBUG, INFO, WARN, ERROR;
            }


            @Override
            public String toString() {
                return String.format(
                        "BuildMessage [message=%s, localization=%s, type=%s]",
                        message, localization, type);
            }

        }


        @Override
        public String toString() {
            return String.format(
                    "ModelOutput [fileModel=%s, buildMessages=%s]", fileModel,
                    buildMessages);
        }

    }
}
