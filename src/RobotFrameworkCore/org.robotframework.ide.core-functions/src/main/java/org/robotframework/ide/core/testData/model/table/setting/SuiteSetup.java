package org.robotframework.ide.core.testData.model.table.setting;

import org.robotframework.ide.core.testData.model.AKeywordBaseSetting;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SuiteSetup extends AKeywordBaseSetting {

    public SuiteSetup(final RobotToken declaration) {
        super(declaration);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.SUITE_SETUP;
    }
}
