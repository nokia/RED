package org.robotframework.ide.core.testData.text.lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;

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


    public TxtRobotTestDataLexer() {
        this(new ReadersProvider());
    }


    public TxtRobotTestDataLexer(final ReadersProvider readersProvider) {
        this.readersProvider = readersProvider;
        this.tempBuffer = readersProvider.newCharBuffer(charBufferSize);
    }


    public LinkedListMultimap<RobotTokenType, RobotToken> extractTokens(
            final File robotTestDataFile) throws FileNotFoundException,
            IOException {
        LinkedListMultimap<RobotTokenType, RobotToken> tokens;
        try (Reader reader = readersProvider.create(robotTestDataFile)) {
            tokens = extractTokens(reader);
        }

        return tokens;
    }


    private LinkedListMultimap<RobotTokenType, RobotToken> extractTokens(
            Reader reader) throws IOException {
        LinkedListMultimap<RobotTokenType, RobotToken> tokens = LinkedListMultimap
                .create();

        int line = LinearPositionMarker.THE_FIRST_LINE;
        int column = LinearPositionMarker.THE_FIRST_COLUMN;

        addTheFirstLineStartToken(tokens);

        int readLength = 0;
        while((readLength = reader.read(tempBuffer)) != -1) {
            for (int charIndex = 0; charIndex < readLength; charIndex++) {

            }

            tempBuffer.clear();
        }

        addEndOfFileToken(tokens, line, column);

        return tokens;
    }


    private void addTheFirstLineStartToken(
            LinkedListMultimap<RobotTokenType, RobotToken> tokens) {
        LinearPositionMarker theFirstPostion = LinearPositionMarker
                .createMarkerForFirstLineAndColumn();
        RobotToken theFirstLineStart = new RobotToken(theFirstPostion, null,
                theFirstPostion);
        theFirstLineStart.setType(RobotTokenType.START_LINE);
        tokens.put(RobotTokenType.START_LINE, theFirstLineStart);
    }


    private void addEndOfFileToken(
            LinkedListMultimap<RobotTokenType, RobotToken> tokens, int line,
            int column) {
        LinearPositionMarker endOfFilePos = new LinearPositionMarker(line,
                column);
        RobotToken theEndOfFile = new RobotToken(endOfFilePos, null,
                endOfFilePos);
        theEndOfFile.setType(RobotTokenType.END_OF_FILE);
        tokens.put(RobotTokenType.END_OF_FILE, theEndOfFile);
    }
}
