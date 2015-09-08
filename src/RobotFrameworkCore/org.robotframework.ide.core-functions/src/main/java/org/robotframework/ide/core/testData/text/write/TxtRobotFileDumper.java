package org.robotframework.ide.core.testData.text.write;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.robotframework.ide.core.testData.IRobotFileDumper;
import org.robotframework.ide.core.testData.model.IRobotFile;


public class TxtRobotFileDumper implements IRobotFileDumper {

    @Override
    public void dump(final File destFile, final IRobotFile model)
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


    public StringBuilder dump(final IRobotFile model) {
        StringBuilder str = new StringBuilder();

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
