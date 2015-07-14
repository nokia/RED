package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.common.Text;


public class TestTimeout {

    private final TestTimeoutDeclaration testTimeoutWord;
    private Text timeout;


    public TestTimeout(final TestTimeoutDeclaration testTimeoutWord) {
        this.testTimeoutWord = testTimeoutWord;
    }
}
