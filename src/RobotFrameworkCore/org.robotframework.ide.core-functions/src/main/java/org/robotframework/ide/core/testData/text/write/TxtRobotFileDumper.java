package org.robotframework.ide.core.testData.text.write;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.robotframework.ide.core.testData.IRobotFileDumper;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.text.write.sections.RobotFileSectionSplitter;
import org.robotframework.ide.core.testData.text.write.sections.Section;


public class TxtRobotFileDumper implements IRobotFileDumper {

    private final RobotFileSectionSplitter sectionSplitter;


    public TxtRobotFileDumper() {
        this.sectionSplitter = new RobotFileSectionSplitter();
    }


    @Override
    public void dump(final File destFile, final RobotFile model)
            throws Exception {
        Path tempFile = Files.createTempFile(
                destFile.getName() + System.currentTimeMillis(), "txt_temp");
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                tempFile.toFile()));

        writer.write(dump(model).toString());

        writer.flush();
        writer.close();

        Files.move(tempFile, destFile.toPath(), StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
        Files.delete(tempFile);
    }


    public StringBuilder dump(final RobotFile model) {
        StringBuilder str = new StringBuilder();
        for (Section s : sectionSplitter.getSections(model)) {
            System.out.println("Type: " + s.getType() + ": "
                    + s.getSectionContent());
        }
        return str;
    }


    @Override
    public boolean canDumpFile(File file) {
        boolean result = false;

        if (file != null && file.isFile()) {
            String fileName = file.getName().toLowerCase();
            result = (fileName.endsWith(".txt") || fileName.endsWith(".robot"));
        }

        return result;
    }
}
