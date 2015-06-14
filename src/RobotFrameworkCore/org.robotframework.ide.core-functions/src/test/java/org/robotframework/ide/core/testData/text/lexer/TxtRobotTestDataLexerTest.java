package org.robotframework.ide.core.testData.text.lexer;

import java.io.Reader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robotframework.ide.core.testData.text.lexer.helpers.ReadersProvider;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


public class TxtRobotTestDataLexerTest {

    @ForClean
    @Mock
    private ReadersProvider readersProvider;
    @ForClean
    @Mock
    private Reader reader;


    @Test
    public void test_extractTokens_emptyFile_shouldReturn_onlyTwoTokensStartLineAndEndOfFileTokens() {

    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
