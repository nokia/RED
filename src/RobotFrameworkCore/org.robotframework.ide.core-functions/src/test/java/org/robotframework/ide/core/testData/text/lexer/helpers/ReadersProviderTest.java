package org.robotframework.ide.core.testData.text.lexer.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.CharBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ReadersProvider
 */
public class ReadersProviderTest {

    @ForClean
    private ReadersProvider readersProvider;


    @Test
    public void test_createReader_forThisClassAsInputFile()
            throws FileNotFoundException, URISyntaxException {
        // prepare
        File f = new File(this.getClass()
                .getResource(this.getClass().getSimpleName() + ".class")
                .toURI());

        // execute
        Reader r = readersProvider.create(f);

        // verify
        assertThat(r).isNotNull();
        assertThat(r).isInstanceOf(InputStreamReader.class);
    }


    @Test
    public void test_createNewCharBuffer_forValidSize() {
        // prepare
        int charBufferSize = 10;

        // execute
        CharBuffer newCharBuffer = readersProvider
                .newCharBuffer(charBufferSize);
        assertNotNull(newCharBuffer);
        assertThat(newCharBuffer.capacity()).isEqualTo(charBufferSize);
        assertThat(newCharBuffer.position()).isEqualTo(0);
    }


    @Before
    public void setUp() {
        readersProvider = new ReadersProvider();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
