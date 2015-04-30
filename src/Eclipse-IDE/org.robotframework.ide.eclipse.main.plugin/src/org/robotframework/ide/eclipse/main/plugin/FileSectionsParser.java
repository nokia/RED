package org.robotframework.ide.eclipse.main.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

class FileSectionsParser {

    private final boolean readOnly;
    private IFile file;
    private InputStream stream;

    FileSectionsParser(final IFile file) {
        this.file = file;
        this.readOnly = file.isReadOnly();
    }

    FileSectionsParser(final InputStream stream, final boolean readOnly) {
        this.stream = stream;
        this.readOnly = readOnly;
    }

    List<RobotElement> parseRobotFileSections(final RobotSuiteFile parent) throws IOException {
        final List<RobotElement> sections = new ArrayList<>();

        InputStream inputStream = null;
        try {
            inputStream = file != null ? file.getContents(true) : stream;
        } catch (final CoreException e) {
            throw new IOException("Unable to get content of file: " + file.getLocation().toString(), e);
        }

        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line = bufferedReader.readLine();

            RobotVariablesSection varSection = null;
            RobotCasesSection casesSection = null;
            RobotSuiteSettingsSection settingSection = null;
            while (line != null) {
                if (line.trim().isEmpty()) {
                    line = bufferedReader.readLine();
                    continue;
                } else if (line.startsWith("*")) {
                    varSection = null;
                    casesSection = null;
                    final String sectionName = extractSectionName(line);
                    if (RobotVariablesSection.SECTION_NAME.equals(sectionName)) {
                        varSection = new RobotVariablesSection(parent, readOnly);
                        sections.add(varSection);
                    } else if (RobotCasesSection.SECTION_NAME.equals(sectionName)) {
                        casesSection = new RobotCasesSection(parent, readOnly);
                        sections.add(casesSection);
                    } else if (RobotSuiteSettingsSection.SECTION_NAME.equals(sectionName)) {
                        settingSection = new RobotSuiteSettingsSection(parent, readOnly);
                        sections.add(settingSection);
                    } else {
                        sections.add(new RobotSuiteFileSection(parent, sectionName, readOnly));
                    }
                } else if (varSection != null) {
                    final String comment = extractComment(line);
                    final String lineWithoutComment = removeComment(line);

                    final String name = extractVarName(lineWithoutComment);
                    final String value = extractVarValue(lineWithoutComment);

                    if (lineWithoutComment.startsWith("@")) {
                        varSection.createListVariable(name, value, comment);
                    } else {
                        varSection.createScalarVariable(name, value, comment);
                    }
                } else if (casesSection != null) {
                    // parse cases
                } else if (settingSection != null) {
                    final String comment = extractComment(line);
                    final String lineWithoutComment = removeComment(line);
                    
                    final String[] split = lineWithoutComment.split("  +");
                    final String name = split[0];
                    final String[] args = split.length == 1 ? new String[0] : Arrays.copyOfRange(split, 1, split.length);
                    
                    settingSection.createSetting(name, comment, args);
                }
                line = bufferedReader.readLine();
            }
        }
        return sections;
    }

    private String removeComment(final String line) {
        final int indexOfComment = line.indexOf('#');
        if (indexOfComment < 0) {
            return line.trim();
        }
        return line.substring(0, indexOfComment).trim();
    }

    private String extractComment(final String line) {
        final int indexOfComment = line.indexOf('#');
        if (indexOfComment < 0) {
            return "";
        }
        return line.substring(line.indexOf('#')).replaceFirst("#", "").trim();
    }

    private String extractVarValue(final String line) {
        return line.substring(line.indexOf('}') + 1).trim();
    }

    private String extractVarName(final String line) {
        return line.substring(2, line.indexOf('}')).trim();
    }

    private String extractSectionName(final String line) {
        return line.replaceAll("\\*", " ").trim();
    }

}
