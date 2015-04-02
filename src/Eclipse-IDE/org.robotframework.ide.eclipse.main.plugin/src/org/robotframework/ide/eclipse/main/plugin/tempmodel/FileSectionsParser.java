package org.robotframework.ide.eclipse.main.plugin.tempmodel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class FileSectionsParser {

    public FileSection[] parseSections(final IFile file) throws CoreException, IOException {
        final List<FileSection> sections = new ArrayList<>();

        final InputStream inputStream = file.getContents();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

        String line = bufferedReader.readLine();

        while (line != null) {
            if (line.startsWith("*")) {
                sections.add(new FileSection(extractSectionName(line)));
            }
            line = bufferedReader.readLine();
        }
        return sections.toArray(new FileSection[0]);
    }

    private String extractSectionName(final String line) {
        return line.replaceAll("\\*", " ").trim();
    }

}
