package org.robotframework.ide.core.testData.text.lexer.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.robotframework.ide.core.testData.text.lexer.TxtRobotTestDataLexer;


/**
 * Extracted class to give ability to test reading method
 * {@link TxtRobotTestDataLexer#extractTokens(File)} . This class gives
 * assumption that robot test data files are encoded in {@code UTF-8}.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class ReadersProvider {

    private final Charset charset;


    public ReadersProvider() {
        this.charset = Charset.forName("UTF-8");
    }


    public Reader create(final File f) throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(f), charset);
    }


    public CharBuffer newCharBuffer(int charBufferSize) {
        return CharBuffer.allocate(charBufferSize);
    }
}
