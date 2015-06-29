package org.robotframework.ide.eclipse.main.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;


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


    List<RobotElement> parseRobotFileSections(final RobotSuiteFile parent)
            throws IOException {
        final List<RobotElement> sections = new ArrayList<>();

        InputStream inputStream = null;
        IMarker[] markers;
        try {
            inputStream = file != null ? file.getContents(true) : stream;
            markers = file == null ? new IMarker[0] : file.findMarkers(
                    RobotProblem.TYPE_ID, true, 1);
        } catch (final CoreException e) {
            if (file != null) {
                throw new IOException("Unable to get content of file: " + file.getLocation().toString(), e);
            } else {
                throw new IOException("Unable to read content from stream", e);
            }
        }
        final Multimap<Integer, IMarker> groupedMarkers = groupMarkersByLine(markers);

        try (final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"))) {
            String line = bufferedReader.readLine();

            RobotCasesSection casesSection = null;
            RobotCase testCase = null;
            RobotKeywordsSection keywordsSection = null;
            RobotKeywordDefinition keywordDef = null;
            RobotSuiteSettingsSection settingSection = null;
            RobotVariablesSection varSection = null;
            int lineNumber = 1;
            while(line != null) {
                if (line.trim().isEmpty()) {
                    line = bufferedReader.readLine();
                    lineNumber++;
                    continue;
                } else if (line.startsWith("*")) {
                    casesSection = null;
                    testCase = null;
                    keywordsSection = null;
                    keywordDef = null;
                    settingSection = null;
                    varSection = null;
                    final String sectionName = extractSectionName(line);
                    if (RobotVariablesSection.SECTION_NAME.equals(sectionName)) {
                        varSection = new RobotVariablesSection(parent, readOnly);
                        sections.add(varSection);
                    } else if (RobotCasesSection.SECTION_NAME
                            .equals(sectionName)) {
                        casesSection = new RobotCasesSection(parent, readOnly);
                        sections.add(casesSection);
                    } else if (RobotKeywordsSection.SECTION_NAME.equals(sectionName)) {
                        keywordsSection = new RobotKeywordsSection(parent, readOnly);
                        sections.add(keywordsSection);
                    } else if (RobotSuiteSettingsSection.SECTION_NAME
                            .equals(sectionName)) {
                        settingSection = new RobotSuiteSettingsSection(parent,
                                readOnly);
                        sections.add(settingSection);
                    } else {
                        sections.add(new RobotSuiteFileSection(parent,
                                sectionName, readOnly));
                    }
                } else if (varSection != null) {
                    final String comment = extractComment(line);
                    final String lineWithoutComment = removeComment(line);

                    final String name = extractVarName(lineWithoutComment);
                    String value = extractVarValue(lineWithoutComment);
                    value = (value == null) ? "" : value;

                    if (name != null) {
                        if (lineWithoutComment.startsWith("@")) {
                            varSection.createListVariable(name, value, comment);
                        } else if (lineWithoutComment.startsWith("&")) {
                            varSection.createDictionaryVariable(name, value,
                                    comment);
                        } else {
                            varSection.createScalarVariable(name, value,
                                    comment);
                        }
                    }
                } else if (casesSection != null) {
                    if (line.startsWith("  ") && testCase != null) {
                        final String comment = extractComment(line);
                        final String lineWithoutComment = removeComment(line);

                        final String[] split = lineWithoutComment.split("  +");
                        final String name = split[0];
                        final String[] args = split.length == 1 ? new String[0]
                                : Arrays.copyOfRange(split, 1, split.length);

                        if (name.startsWith("[") && name.endsWith("]")) {
                            testCase.createSettting(name, args, comment);
                        } else {
                            testCase.createKeywordCall(name, args, comment,
                                    groupedMarkers.get(lineNumber));
                        }
                    } else {
                        testCase = casesSection.createTestCase(line);
                    }
                } else if (keywordsSection != null) {
                    if (line.startsWith("  ") && keywordDef != null) {
                        final String comment = extractComment(line);
                        final String lineWithoutComment = removeComment(line);

                        final String[] split = lineWithoutComment.split("  +");
                        final String name = split[0];
                        final String[] args = split.length == 1 ? new String[0] : Arrays.copyOfRange(split, 1,
                                split.length);

                        keywordDef.createKeywordCall(name, args, comment);
                    } else {
                        keywordDef = keywordsSection.createKeywordDefinition(line);
                    }
                } else if (settingSection != null) {
                    final String comment = extractComment(line);
                    final String lineWithoutComment = removeComment(line);

                    final String[] split = lineWithoutComment.split("  +");
                    final String name = split[0];
                    final String[] args = split.length == 1 ? new String[0]
                            : Arrays.copyOfRange(split, 1, split.length);

                    settingSection.createSetting(name, comment, args);
                }
                line = bufferedReader.readLine();
                lineNumber++;
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


    private static Multimap<Integer, IMarker> groupMarkersByLine(
            final IMarker[] markers) {
        final Multimap<Integer, IMarker> groupedMarkers = LinkedListMultimap
                .create();
        for (final IMarker marker : markers) {
            final int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
            groupedMarkers.put(line, marker);
        }
        return groupedMarkers;
    }

}
