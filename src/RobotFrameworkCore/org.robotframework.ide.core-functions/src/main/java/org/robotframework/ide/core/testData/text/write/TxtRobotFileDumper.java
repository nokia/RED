package org.robotframework.ide.core.testData.text.write;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.IRobotFileDumper;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.RobotLine;

import com.google.common.annotations.VisibleForTesting;


public class TxtRobotFileDumper implements IRobotFileDumper {

    private final LineSeparatorFixer lineSeparatorFixer;
    private final static List<IElementDumper> dumpers = new LinkedList<>();
    static {

    }


    public TxtRobotFileDumper() {
        this.lineSeparatorFixer = new LineSeparatorFixer();
    }


    @Override
    public void dump(final File destFile, final RobotFile model)
            throws Exception {
        Path tempFile = Files.createTempFile(
                destFile.getName() + System.currentTimeMillis(), "txt_temp");
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                tempFile.toFile()));
        List<RobotLine> fileContent = model.getFileContent();
        for (RobotLine line : fileContent) {
            writer.write(dumpLine(line));
            writer.newLine();
        }

        writer.flush();
        writer.close();

        Files.move(tempFile, destFile.toPath(), StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
        Files.delete(tempFile);
    }


    public StringBuilder dump(final RobotFile model) {
        StringBuilder str = new StringBuilder();
        List<RobotLine> fileContent = model.getFileContent();
        for (RobotLine line : fileContent) {
            str.append(dumpLine(line));
            str.append(System.lineSeparator());
        }

        return str;
    }


    @VisibleForTesting
    protected String dumpLine(final RobotLine line) {
        StringBuilder output = new StringBuilder();

        lineSeparatorFixer.separateLine(line);
        List<IRobotLineElement> lineElements = line.getLineElements();
        int size = lineElements.size();
        for (int i = 0; i < size; i++) {
            IRobotLineElement elem = lineElements.get(i);
            IElementDumper dumper = findDumper(elem);
            output.append(dumper.dump(line, i));
        }

        return output.toString();
    }


    @VisibleForTesting
    protected IElementDumper findDumper(final IRobotLineElement elem) {
        IElementDumper dumper = new OneToOneDumper();
        for (IElementDumper d : dumpers) {
            if (d.canDump(elem)) {
                dumper = d;
                break;
            }
        }

        return dumper;
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
