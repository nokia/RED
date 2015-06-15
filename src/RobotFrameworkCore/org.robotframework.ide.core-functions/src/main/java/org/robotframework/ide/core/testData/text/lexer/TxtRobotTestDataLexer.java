package org.robotframework.ide.core.testData.text.lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher;

import com.google.common.collect.LinkedListMultimap;


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


    public LinkedListMultimap<RobotType, RobotToken> extractTokens(
            final File robotTestDataFile) throws FileNotFoundException,
            IOException {
        LinkedListMultimap<RobotType, RobotToken> tokens;
        try (Reader reader = readersProvider.create(robotTestDataFile)) {
            tokens = extractTokens(reader);
        }

        return tokens;
    }


    private LinkedListMultimap<RobotType, RobotToken> extractTokens(
            Reader reader) throws IOException {
        LinkedListMultimap<RobotType, RobotToken> tokens = LinkedListMultimap
                .create();

        int line = LinearPositionMarker.THE_FIRST_LINE;
        int column = LinearPositionMarker.THE_FIRST_COLUMN;

        addTheFirstLineStartToken(tokens);

        int readLength = 0;
        while((readLength = reader.read(tempBuffer)) != -1) {
            for (int charIndex = 0; charIndex < readLength; charIndex++) {
                tokenMatcher.offerChar(tokens, tempBuffer, charIndex);
            }

            tempBuffer.clear();
        }

        addEndOfFileToken(tokens, line, column);

        return tokens;
    }


    private void addTheFirstLineStartToken(
            LinkedListMultimap<RobotType, RobotToken> tokens) {
        LinearPositionMarker theFirstPostion = LinearPositionMarker
                .createMarkerForFirstLineAndColumn();
        RobotToken theFirstLineStart = new RobotToken(theFirstPostion, null,
                theFirstPostion);
        theFirstLineStart.setType(RobotTokenType.START_LINE);
        tokens.put(RobotTokenType.START_LINE, theFirstLineStart);
    }


    private void addEndOfFileToken(
            LinkedListMultimap<RobotType, RobotToken> tokens, int line,
            int column) {
        LinearPositionMarker endOfFilePos = new LinearPositionMarker(line,
                column);
        RobotToken theEndOfFile = new RobotToken(endOfFilePos, null,
                endOfFilePos);
        theEndOfFile.setType(RobotTokenType.END_OF_FILE);
        tokens.put(RobotTokenType.END_OF_FILE, theEndOfFile);
    }
}
