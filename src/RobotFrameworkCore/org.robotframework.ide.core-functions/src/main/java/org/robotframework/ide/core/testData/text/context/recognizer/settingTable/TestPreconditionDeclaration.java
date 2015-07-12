package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Suite Precondition
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class TestPreconditionDeclaration extends ATableElementRecognizer {

    public TestPreconditionDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_TEST_PRECONDITION,
                createExpectedWithOptionalColonAsLast(RobotWordType.TEST_WORD,
                        RobotWordType.PRECONDITION_WORD));
    }
}
