package org.robotframework.ide.core.testData;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.text.write.TxtRobotFileDumper;


public class RobotFileDumper {

    private static final List<IRobotFileDumper> availableFormatDumpers = new LinkedList<>();
    static {
        availableFormatDumpers.add(new TxtRobotFileDumper());
    }


    public void dump(final File file, final RobotFileOutput output)
            throws Exception {
        IRobotFileDumper dumperToUse = null;
        for (IRobotFileDumper dumper : availableFormatDumpers) {
            if (dumper.canDumpFile(file)) {
                dumperToUse = dumper;
                break;
            }
        }

        if (dumperToUse != null) {
            dumperToUse.dump(file, output.getFileModel());
        }
    }
}
