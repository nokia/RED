package org.robotframework.ide.eclipse.main.plugin.validation;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.validation.ArgumentsParser.InvalidArgumentsProvidedException;

public class ArgumentsParserTest {

    @Test(expected = InvalidArgumentsProvidedException.class)
    public void exceptionIsThrown_whenThereAreNoArgumentsProvided() {
        final ArgumentsParser parser = new ArgumentsParser();

        parser.parseArguments(new ArrayList<>());
    }

    @Test(expected = InvalidArgumentsProvidedException.class)
    public void exceptionIsThrown_whenUnrecognizedSwitchIsProvided() {
        final ArgumentsParser parser = new ArgumentsParser();

        parser.parseArguments(newArrayList("-something"));
    }

    @Test
    public void thereAreNoProjectsToImportIfNoPathIsProvided() {
        final ArgumentsParser parser = new ArgumentsParser();

        assertThat(parser.parseArguments(newArrayList("-import")).getProjectPathsToImport()).isEmpty();
        assertThat(parser.parseArguments(newArrayList("-import", "-noReport")).getProjectPathsToImport()).isEmpty();
    }

    @Test
    public void thereAreProjectsToImportIfPathsAreProvided() {
        final ArgumentsParser parser = new ArgumentsParser();

        assertThat(parser.parseArguments(newArrayList("-import", "p1")).getProjectPathsToImport())
                .containsExactly("p1");
        assertThat(parser.parseArguments(newArrayList("-import", "p1", "p2", "-noReport")).getProjectPathsToImport())
                .containsExactly("p1", "p2");
    }

    @Test
    public void thereAreNoProjectsIfNoNameIsProvided() {
        final ArgumentsParser parser = new ArgumentsParser();

        assertThat(parser.parseArguments(newArrayList("-projects")).getProjectNamesToValidate()).isEmpty();
        assertThat(parser.parseArguments(newArrayList("-projects", "-noReport")).getProjectNamesToValidate()).isEmpty();
    }

    @Test
    public void thereAreProjectsIfNamesAreProvided() {
        final ArgumentsParser parser = new ArgumentsParser();

        assertThat(parser.parseArguments(newArrayList("-projects", "project1")).getProjectNamesToValidate())
                .containsExactly("project1");
        assertThat(parser.parseArguments(newArrayList("-projects", "project1", "project2", "-noReport"))
                .getProjectNamesToValidate()).containsExactly("project1", "project2");
    }

    @Test
    public void defaultReportPathIsUsedIfNoFileIsProvided() {
        final ArgumentsParser parser = new ArgumentsParser();

        assertThat(parser.parseArguments(newArrayList("-projects", "project1")).getReportFilePath())
                .isEqualTo("report.xml");
    }

    @Test
    public void givenReportPathIsUsedIfProvided() {
        final ArgumentsParser parser = new ArgumentsParser();

        assertThat(parser.parseArguments(newArrayList("-report", "my_report.xml")).getReportFilePath())
                .isEqualTo("my_report.xml");
    }

    @Test
    public void norReportPathIsUsedIfNoReportIsSwitched() {
        final ArgumentsParser parser = new ArgumentsParser();

        assertThat(parser.parseArguments(newArrayList("-report", "my_report.xml", "-noReport")).getReportFilePath())
                .isNull();
    }
}
