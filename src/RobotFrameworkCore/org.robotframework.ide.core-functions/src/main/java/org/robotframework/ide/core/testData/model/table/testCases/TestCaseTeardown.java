package org.robotframework.ide.core.testData.model.table.testCases;

import org.robotframework.ide.core.testData.model.AKeywordBaseSetting;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCaseTeardown extends AKeywordBaseSetting {

    public TestCaseTeardown(RobotToken declaration) {
        super(declaration);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE_TEARDOWN;
    }
}
