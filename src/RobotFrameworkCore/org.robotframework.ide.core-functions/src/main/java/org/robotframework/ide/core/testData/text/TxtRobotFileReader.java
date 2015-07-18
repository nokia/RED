package org.robotframework.ide.core.testData.text;

import java.io.File;
import java.io.IOException;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.ModelBuilder;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput.BuildMessage;
import org.robotframework.ide.core.testData.text.lexer.TxtRobotTestDataLexer;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


public class TxtRobotFileReader {

    private ModelOutput parse(final TokenOutput extractedTokens) {
        ModelOutput model = null;

        ContextBuilder contextBuilder = new ContextBuilder();
        ContextOutput buildContexts = contextBuilder
                .buildContexts(extractedTokens);
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.build(buildContexts);

        return model;
    }


    public ModelOutput parse(final StringBuilder robotFileContent,
            final String pathToFile) {
        ModelOutput model = null;

        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer();
        try {
            TokenOutput extractedTokens = lexer.extractTokens(robotFileContent);
            model = parse(extractedTokens);
        } catch (IOException e) {
            if (model == null) {
                model = new ModelOutput();
            }

            model.addBuildMessage(BuildMessage.buildError("" + e, pathToFile));
        }

        return model;
    }


    public ModelOutput parse(final File robotOrTxtFile) {
        ModelOutput model = null;

        TxtRobotTestDataLexer lexer = new TxtRobotTestDataLexer();
        try {
            TokenOutput extractedTokens = lexer.extractTokens(robotOrTxtFile);
            model = parse(extractedTokens);
        } catch (IOException e) {
            if (model == null) {
                model = new ModelOutput();
            }

            model.addBuildMessage(BuildMessage.buildError("" + e,
                    robotOrTxtFile.getAbsolutePath()));
        }

        return model;
    }
}
