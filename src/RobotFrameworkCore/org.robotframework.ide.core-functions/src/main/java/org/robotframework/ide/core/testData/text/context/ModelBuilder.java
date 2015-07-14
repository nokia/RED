package org.robotframework.ide.core.testData.text.context;

import org.robotframework.ide.core.testData.model.RobotTestDataFile;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;


public class ModelBuilder {

    public ModelOutput build(final ContextOutput contexts) {
        RobotTestDataFile fileModel = new RobotTestDataFile();
        ModelOutput output = new ModelOutput(fileModel);

        return output;
    }

    public static class ModelOutput {

        private final RobotTestDataFile fileModel;


        public ModelOutput(final RobotTestDataFile fileModel) {
            this.fileModel = fileModel;
        }


        public RobotTestDataFile getFileModel() {
            return fileModel;
        }

    }
}
