package org.robotframework.ide.core.testData.text.context;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotTestDataFile;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput.BuildMessage.Level;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class ModelBuilder {

    public ModelOutput build(final ContextOutput contexts) {
        ModelOutput output = new ModelOutput();

        List<AggregatedOneLineRobotContexts> lineContexts = contexts
                .getContexts();

        ElementType etLast = null;
        for (AggregatedOneLineRobotContexts ctx : lineContexts) {
            etLast = mapLine(output, ctx, etLast);
        }

        return output;
    }


    @VisibleForTesting
    protected ElementType mapLine(final ModelOutput model,
            final AggregatedOneLineRobotContexts ctx, final ElementType etLast) {
        final Iterator<? extends IContextElement> separatorBaseIterator = createSeparatorBaseIterator(ctx);
        // while(separatorBaseIterator.hasNext()) {
        IContextElement nextSeparator = separatorBaseIterator.next();

        // search for matching position
        // find correct base on context
        // perform actions
        // }

        return etLast;
    }


    @VisibleForTesting
    protected Iterator<? extends IContextElement> createSeparatorBaseIterator(
            final AggregatedOneLineRobotContexts ctx) {
        Iterator<? extends IContextElement> iterator = null;

        RobotLineSeparatorsContexts separators = ctx.getSeparators();
        IContextElement theFirstPipeInLine = getFirstSeparatorContextFrom(
                separators, RobotLineSeparatorsContexts.PIPE_SEPARATOR_TYPE);
        if (isPipeSeparatedLine(theFirstPipeInLine)) {
            iterator = new PipeSeparableIterator(ctx);
        } else {
            iterator = new WhitespaceSeparableIterator(ctx);
        }

        return iterator;
    }


    private boolean isPipeSeparatedLine(IContextElement theFirstPipeInLine) {
        boolean result = false;
        if (theFirstPipeInLine != null) {
            if (theFirstPipeInLine instanceof OneLineSingleRobotContextPart) {
                OneLineSingleRobotContextPart ctx = (OneLineSingleRobotContextPart) theFirstPipeInLine;
                List<RobotToken> contextTokens = ctx.getContextTokens();
                if (contextTokens != null && !contextTokens.isEmpty()) {
                    RobotToken robotToken = contextTokens.get(0);
                    IRobotTokenType type = robotToken.getType();
                    if (type == RobotSingleCharTokenType.SINGLE_PIPE) {
                        result = (FilePosition.THE_FIRST_COLUMN == robotToken
                                .getStartPosition().getColumn());
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "Pipe separator element has incorrect type "
                                + ((theFirstPipeInLine != null) ? theFirstPipeInLine
                                        .getClass() : " null"));
            }
        }

        return result;
    }


    @VisibleForTesting
    protected IContextElement getFirstSeparatorContextFrom(
            final RobotLineSeparatorsContexts separators,
            final IContextElementType expectedContextType) {
        IContextElement theFirstContext = null;

        List<IContextElement> separatorContext = separators
                .getFoundSeperatorsExcludeType().get(expectedContextType);
        if (separatorContext != null && !separatorContext.isEmpty()) {
            theFirstContext = separatorContext.get(0);
        }

        return theFirstContext;
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
                INFO, WARN, ERROR;
            }
        }

    }
}
