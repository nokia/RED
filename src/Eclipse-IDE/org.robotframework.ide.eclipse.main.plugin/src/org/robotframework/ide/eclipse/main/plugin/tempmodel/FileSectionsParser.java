package org.robotframework.ide.eclipse.main.plugin.tempmodel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;

public class FileSectionsParser {

    private final boolean readOnly;
    private IFile file;
    private InputStream stream;

    public FileSectionsParser(final IFile file) {
        this.file = file;
        this.readOnly = file.isReadOnly();
    }

    public FileSectionsParser(final InputStream stream, final boolean readOnly) {
        this.stream = stream;
        this.readOnly = readOnly;
    }

    public RobotSuiteFile parseRobotSuiteFile() throws IOException {
        final RobotSuiteFile robotSuite = file != null ? RobotFramework.getModelManager().createSuiteFile(file) : new RobotSuiteFile(null, null);
        parseRobotFileSections(robotSuite);
        return robotSuite;
    }

    public List<RobotElement> parseRobotFileSections(final RobotSuiteFile parent) throws IOException {
        final List<RobotElement> sections = new ArrayList<>();

        InputStream inputStream = null;
        try {
            inputStream = file != null ? file.getContents(true) : stream;
        } catch (final CoreException e) {
            throw new IOException("Unable to get content of file: " + file.getLocation().toString(), e);
        }

        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line = bufferedReader.readLine();

            RobotSuiteFileSection varSection = null;
            while (line != null) {
                if (line.trim().isEmpty()) {
                    line = bufferedReader.readLine();
                    continue;
                } else if (line.startsWith("*")) {
                    varSection = null;
                    final String sectionName = extractSectionName(line);
                    if ("Variables".equals(sectionName)) {
                        varSection = new RobotSuiteFileSection(parent, sectionName, readOnly);
                        sections.add(varSection);
                    } else {
                        sections.add(new RobotSuiteFileSection(parent, sectionName, readOnly));
                    }
                } else if (varSection != null) {
                    final String name = extractVarName(line);
                    final String value = extractVarValue(line);
                    final String comment = extractVarComment(line);

                    if (line.startsWith("@")) {
                        varSection.createListVariable(name, value, comment);
                    } else {
                        varSection.createScalarVariable(name, value, comment);
                    }
                }
                line = bufferedReader.readLine();
            }
        }
        return sections;
    }

    private String extractVarComment(final String line) {
        final int indexOfComment = line.indexOf('#');
        if (indexOfComment < 0) {
            return "";
        }
        return line.substring(line.indexOf('#')).trim();
    }

    private String extractVarValue(final String line) {
        final int indexOfComment = line.indexOf('#');
        if (indexOfComment < 0) {
            return line.substring(line.indexOf('}') + 1).trim();
        } else {
            return line.substring(line.indexOf('}') + 1, indexOfComment).trim();
        }
    }

    private String extractVarName(final String line) {
        return line.substring(2, line.indexOf('}')).trim();
    }

    private String extractSectionName(final String line) {
        return line.replaceAll("\\*", " ").trim();
    }

}
