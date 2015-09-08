package org.robotframework.ide.core.testData.model.table.setting.mapping.test;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.TestTimeout;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class TestTimeoutValueMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public TestTimeoutValueMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            IRobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_TEST_TIMEOUT_VALUE);
        rt.setText(new StringBuilder(text));

        SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        List<TestTimeout> timeouts = settings.getTestTimeouts();
        if (!timeouts.isEmpty()) {
            timeouts.get(timeouts.size() - 1).setTimeout(rt);
        } else {
            // FIXME: some internal error
        }
        processingState.push(ParsingState.SETTING_TEST_TIMEOUT_VALUE);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(IRobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = utility.getCurrentStatus(processingState);

        if (state == ParsingState.SETTING_TEST_TIMEOUT) {
            List<TestTimeout> testTimeouts = robotFileOutput.getFileModel()
                    .getSettingTable().getTestTimeouts();
            result = !checkIfHasAlreadyValue(testTimeouts);
        }

        return result;
    }


    @VisibleForTesting
    protected boolean checkIfHasAlreadyValue(List<TestTimeout> testTimeouts) {
        boolean result = false;
        for (TestTimeout setting : testTimeouts) {
            result = (setting.getTimeout() != null);
            result = result || !setting.getMessageArguments().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }
}
