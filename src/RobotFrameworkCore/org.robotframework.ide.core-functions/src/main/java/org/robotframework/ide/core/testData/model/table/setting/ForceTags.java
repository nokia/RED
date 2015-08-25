package org.robotframework.ide.core.testData.model.table.setting;

import org.robotframework.ide.core.testData.model.ATags;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class ForceTags extends ATags {

    public ForceTags(RobotToken declaration) {
        super(declaration);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.FORCE_TAGS_SETTING;
    }
}
