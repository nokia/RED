package org.robotframework.ide.core.testData.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.RobotLine;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;

import com.google.common.annotations.VisibleForTesting;


public class TxtRobotFileWriter {

    public void dump(final ModelOutput model, final StringWriter destReceiver)
            throws IOException {
        dump(destReceiver, model);
    }


    public void dump(final ModelOutput model, final File destFile)
            throws IOException {
        File tempFile = null;
        Writer dumper = null;

        try {
            tempFile = File.createTempFile("temp_" + destFile.getName(), "");
            dumper = createWriter(tempFile);
            dump(dumper, model);

            Files.move(tempFile.toPath(), destFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (dumper != null) {
                dumper.close();
            }
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }


    @VisibleForTesting
    protected void dump(final Writer pw, final ModelOutput model)
            throws IOException {
        List<RobotLine> fileLines = model.getFileModel().getContent();
        for (RobotLine line : fileLines) {
            pw.write(dump(line));
        }

        pw.flush();
    }


    @VisibleForTesting
    protected String dump(final RobotLine line) {
        StringBuilder text = new StringBuilder();
        List<LineElement> elems = line.getElements();
        for (LineElement elem : elems) {
            text.append(elem);
        }

        return text.toString();
    }


    @VisibleForTesting
    protected Writer createWriter(final File file) throws FileNotFoundException {
        OutputStream outStream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(outStream,
                Charset.forName("utf-8"));

        return new BufferedWriter(new PrintWriter(writer));
    }
}
