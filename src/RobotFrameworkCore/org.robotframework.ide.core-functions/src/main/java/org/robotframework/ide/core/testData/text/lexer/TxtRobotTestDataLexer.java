package org.robotframework.ide.core.testData.text.lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class TxtRobotTestDataLexer {

    private int charBufferSize = 4 * 1024;
    private final ReadersProvider readersProvider;
    private final CharBuffer tempBuffer;
    private final RobotTokenMatcher tokenMatcher;


    public TxtRobotTestDataLexer() {
        this(new ReadersProvider());
    }


    public TxtRobotTestDataLexer(final ReadersProvider readersProvider) {
        this(readersProvider, new RobotTokenMatcher());
    }


    public TxtRobotTestDataLexer(final ReadersProvider readersProvider,
            final RobotTokenMatcher matcher) {
        this.readersProvider = readersProvider;
        this.tempBuffer = readersProvider.newCharBuffer(charBufferSize);
        this.tokenMatcher = matcher;
    }


    public TokenOutput extractTokens(final File robotTestDataFile)
            throws FileNotFoundException, IOException {
        TokenOutput out = new TokenOutput();
        try (Reader reader = readersProvider.create(robotTestDataFile)) {
            out = extractTokens(reader);
        }

        return out;
    }


    private TokenOutput extractTokens(Reader reader) throws IOException {
        int readLength = 0;
        while((readLength = reader.read(tempBuffer)) != -1) {
            for (int charIndex = 0; charIndex < readLength; charIndex++) {
                tokenMatcher.offerChar(tempBuffer, charIndex);
            }

            tempBuffer.clear();
        }

        TokenOutput buildTokens = tokenMatcher.buildTokens();

        return buildTokens;
    }
}
