package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;


public class FileSectionsParser {

    private IFile file;
    private InputStream stream;


    public FileSectionsParser(final IFile file) {
        this.file = file;
    }


    public FileSectionsParser(final InputStream stream) {
        this.stream = stream;
    }


    public void parseRobotFileSections(final RobotSuiteFile parent)
            throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = file != null ? file.getContents(true) : stream;
        } catch (final CoreException e) {
            if (file != null) {
                throw new IOException("Unable to get content of file: " + file.getLocation().toString(), e);
            } else {
                throw new IOException("Unable to read content from stream", e);
            }
        }

        try (final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"))) {
            String line = bufferedReader.readLine();

            RobotCase testCase = null;
            RobotKeywordDefinition keywordDef = null;
            RobotSuiteFileSection currentSection = null;

            while(line != null) {
                if (line.trim().isEmpty()) {
                    line = bufferedReader.readLine();
                    continue;
                } else if (line.startsWith("*")) {
                    testCase = null;
                    keywordDef = null;

                    final String sectionName = extractSectionName(line);
                    currentSection = parent.createRobotSection(sectionName);
                } else if (currentSection != null
                        && currentSection.getName().equals(RobotVariablesSection.SECTION_NAME)) {
                    final String comment = extractComment(line);
                    final String lineWithoutComment = removeComment(line);

                    final String name = extractVarName(lineWithoutComment);
                    String value = extractVarValue(lineWithoutComment);
                    value = (value == null) ? "" : value;

                    if (name != null) {
                        final RobotVariablesSection variablesSection = (RobotVariablesSection) currentSection;
                        if (lineWithoutComment.startsWith("@")) {
                            variablesSection.createListVariable(name, value, comment);
                        } else if (lineWithoutComment.startsWith("&")) {
                            variablesSection.createDictionaryVariable(name, value, comment);
                        } else {
                            variablesSection.createScalarVariable(name, value, comment);
                        }
                    }
                } else if (currentSection != null && currentSection.getName().equals(RobotCasesSection.SECTION_NAME)) {
                    if (line.startsWith("  ") && testCase != null) {
                        final String comment = extractComment(line);
                        final String lineWithoutComment = removeComment(line);

                        final String[] split = lineWithoutComment.split("  +");
                        final String name = split[0];
                        final String[] args = split.length == 1 ? new String[0]
                                : Arrays.copyOfRange(split, 1, split.length);

                        testCase.createKeywordCall(name, newArrayList(args), comment);
                    } else {
                        final RobotCasesSection casesSection = (RobotCasesSection) currentSection;
                        testCase = casesSection.createTestCase(line, extractComment(line));
                    }
                } else if (currentSection != null && currentSection.getName().equals(RobotKeywordsSection.SECTION_NAME)) {
                    if (line.startsWith("  ") && keywordDef != null) {
                        final String comment = extractComment(line);
                        final String lineWithoutComment = removeComment(line);

                        final String[] split = lineWithoutComment.split("  +");
                        final String name = split[0];
                        final String[] args = split.length == 1 ? new String[0] : Arrays.copyOfRange(split, 1,
                                split.length);

                        keywordDef.createKeywordCall(name, newArrayList(args), comment);
                    } else {
                        final String comment = extractComment(line);
                        final String lineWithoutComment = removeComment(line);

                        final String[] split = lineWithoutComment.split("  +");
                        final String name = split[0];
                        final String[] args = split.length == 1 ? new String[0] : Arrays.copyOfRange(split, 1,
                                split.length);

                        final RobotKeywordsSection keywordsSection = (RobotKeywordsSection) currentSection;
                        keywordDef = keywordsSection.createKeywordDefinition(name, newArrayList(args), comment);
                    }
                } else if (currentSection != null
                        && currentSection.getName().equals(RobotSettingsSection.SECTION_NAME)) {
                    final String comment = extractComment(line);
                    final String lineWithoutComment = removeComment(line);

                    final String[] split = lineWithoutComment.split("  +");
                    final String name = split[0];
                    final String[] args = split.length == 1 ? new String[0]
                            : Arrays.copyOfRange(split, 1, split.length);

                    final RobotSettingsSection settingSection = (RobotSettingsSection) currentSection;
                    settingSection.createSetting(name, comment, args);
                }
                line = bufferedReader.readLine();
            }
        }
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
        String varValue = null;
        final int index = line.indexOf('}');
        if (index > -1) {
            varValue = line.substring(index + 1).trim();
        }

        return varValue;
    }


    private String extractVarName(final String line) {
        String varName = null;
        final int index = line.indexOf('}');

        if (index > -1) {
            varName = line.substring(2, index).trim();
        }

        return varName;
    }


    private String extractSectionName(final String line) {
        return line.replaceAll("\\*", " ").trim();
    }

    // private static Multimap<Integer, IMarker> groupMarkersByLine(
    // final IMarker[] markers) {
    // final Multimap<Integer, IMarker> groupedMarkers = LinkedListMultimap
    // .create();
    // for (final IMarker marker : markers) {
    // final int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
    // groupedMarkers.put(line, marker);
    // }
    // return groupedMarkers;
    // }
}
