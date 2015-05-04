package org.robotframework.ide.core.testData.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.robotframework.ide.core.testData.parser.AbstractRobotFrameworkFileParser;
import org.robotframework.ide.core.testData.parser.ITestDataParserProvider;
import org.robotframework.ide.core.testData.parser.testUtils.LetterCombinerGenerator;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see TxtRobotFrameworkParser#canAcceptFile(File)
 */
public class TestTxtRobotFrameworkParserMethodCanAcceptFile {

    private AbstractRobotFrameworkFileParser<ByteBufferInputStream> txtParser;


    @Test
    public void test_withDifferentCombinationOfFileNameExtension_txt_shouldAlwaysReturn_TRUE() {
        // prepare
        LetterCombinerGenerator combinationGenerator = new LetterCombinerGenerator();
        List<String> allPossibleExtensions = combinationGenerator
                .createCombination("txt");

        File txtFile = mock(File.class);
        InOrder order = inOrder(txtFile);

        when(txtFile.exists()).thenReturn(true);
        when(txtFile.canRead()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(true);
        when(txtFile.isDirectory()).thenReturn(false);

        for (String fileExtension : allPossibleExtensions) {
            when(txtFile.getName()).thenReturn("test_1." + fileExtension);

            // execute & verify
            assertThat(txtParser.canAcceptFile(txtFile)).isTrue();

        }

        order.verify(txtFile, times(1)).exists();
        order.verify(txtFile, times(1)).canRead();
        order.verify(txtFile, times(1)).isFile();
        order.verify(txtFile, times(8)).getName();
        order.verifyNoMoreInteractions();

        assertThat(allPossibleExtensions).hasSize(8);
    }


    @Test
    public void test_withFilenameCorrect_txt_isLowerCase_shouldReturn_TRUE() {
        // prepare
        File txtFile = mock(File.class);
        InOrder order = inOrder(txtFile);

        when(txtFile.exists()).thenReturn(true);
        when(txtFile.canRead()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(true);
        when(txtFile.isDirectory()).thenReturn(false);
        when(txtFile.getName()).thenReturn("test_1.txt");

        // execute & verify
        assertThat(txtParser.canAcceptFile(txtFile)).isTrue();

        order.verify(txtFile, times(1)).exists();
        order.verify(txtFile, times(1)).canRead();
        order.verify(txtFile, times(1)).isFile();
        order.verify(txtFile, times(1)).getName();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_withFilenameCorrectInSize_but_notTxt_shouldReturn_FALSE() {
        // prepare
        File txtFile = mock(File.class);
        InOrder order = inOrder(txtFile);

        when(txtFile.exists()).thenReturn(true);
        when(txtFile.canRead()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(true);
        when(txtFile.isDirectory()).thenReturn(false);
        when(txtFile.getName()).thenReturn("test_1.t2t");

        // execute & verify
        assertThat(txtParser.canAcceptFile(txtFile)).isFalse();

        order.verify(txtFile, times(1)).exists();
        order.verify(txtFile, times(1)).canRead();
        order.verify(txtFile, times(1)).isFile();
        order.verify(txtFile, times(1)).getName();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_withTooShortFilename_shouldReturn_FALSE() {
        // prepare
        File txtFile = mock(File.class);
        InOrder order = inOrder(txtFile);

        when(txtFile.exists()).thenReturn(true);
        when(txtFile.canRead()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(true);
        when(txtFile.isDirectory()).thenReturn(false);
        when(txtFile.getName()).thenReturn(".txt");

        // execute & verify
        assertThat(txtParser.canAcceptFile(txtFile)).isFalse();

        order.verify(txtFile, times(1)).exists();
        order.verify(txtFile, times(1)).canRead();
        order.verify(txtFile, times(1)).isFile();
        order.verify(txtFile, times(1)).getName();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_fileIsDirectory_shouldReturn_FALSE() {
        // prepare
        File txtFile = mock(File.class);
        InOrder order = inOrder(txtFile);

        when(txtFile.exists()).thenReturn(true);
        when(txtFile.canRead()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(false);
        when(txtFile.isDirectory()).thenReturn(true);

        // execute & verify
        assertThat(txtParser.canAcceptFile(txtFile)).isFalse();

        order.verify(txtFile, times(1)).exists();
        order.verify(txtFile, times(1)).canRead();
        order.verify(txtFile, times(1)).isFile();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_fileCannotBeRead_shouldReturn_FALSE() {
        // prepare
        File txtFile = mock(File.class);
        InOrder order = inOrder(txtFile);

        when(txtFile.exists()).thenReturn(true);
        when(txtFile.canRead()).thenReturn(false);
        when(txtFile.isFile()).thenReturn(true);
        when(txtFile.isDirectory()).thenReturn(false);

        // execute & verify
        assertThat(txtParser.canAcceptFile(txtFile)).isFalse();

        order.verify(txtFile, times(1)).exists();
        order.verify(txtFile, times(1)).canRead();
        order.verifyNoMoreInteractions();
    }


    @Test
    public void test_fileNotExists_shouldReturn_FALSE() {
        // prepare
        File txtFile = mock(File.class);
        InOrder order = inOrder(txtFile);

        when(txtFile.exists()).thenReturn(false);
        when(txtFile.canRead()).thenReturn(true);
        when(txtFile.isFile()).thenReturn(true);
        when(txtFile.isDirectory()).thenReturn(false);

        // execute & verify
        assertThat(txtParser.canAcceptFile(txtFile)).isFalse();

        order.verify(txtFile, times(1)).exists();
        order.verifyNoMoreInteractions();
    }


    @Before
    public void setUp() throws Exception {
        ITestDataParserProvider<ByteBufferInputStream> parsersProvider = new TxtTestDataParserProvider();
        txtParser = new TxtRobotFrameworkParser(parsersProvider);
    }


    @After
    public void tearDown() {
        txtParser = null;
    }
}
