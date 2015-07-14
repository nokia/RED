package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.common.Text;


public class Timeout {

    private final TimeoutDeclaration timeoutWord;
    private Text timeout;
    private Text optionalMessage;


    public Timeout(final TimeoutDeclaration timeoutWord) {
        this.timeoutWord = timeoutWord;
    }
}
