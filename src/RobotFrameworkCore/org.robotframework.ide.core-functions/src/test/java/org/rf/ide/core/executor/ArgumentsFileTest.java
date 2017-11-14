/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ArgumentsFileTest {

    @Test
    public void emptyContentIsGenerated_whenNoLineWasAdded() {
        final ArgumentsFile file = new ArgumentsFile();
        assertThat(file.generateContent()).isEmpty();
    }

    @Test
    public void singleLineContentIsGenerated_whenSingleLineWasAdded() {
        final ArgumentsFile file = new ArgumentsFile();
        file.addLine("arg", "val");
        assertThat(file.generateContent()).isEqualTo("arg val");
    }

    @Test
    public void multilineContentIsGeneratedWithColumns_whenMultipleLinesWereAdded() {
        final ArgumentsFile file = new ArgumentsFile();
        file.addLine("a", "1");
        file.addLine("ab", "2");
        file.addLine("abc", "3");
        file.addLine("d", "4");
        file.addLine("e");
        assertThat(file.generateContent()).isEqualTo("a   1\nab  2\nabc 3\nd   4\ne");
    }

    @Test
    public void singleLineCommentIsGenerated() {
        final ArgumentsFile file = new ArgumentsFile();
        file.addCommentLine("comment");

        assertThat(file.generateContent()).isEqualTo("# comment");
    }

    @Test
    public void multilineContentWithCommentsIsGenerated() {
        final ArgumentsFile file = new ArgumentsFile();
        file.addLine("a", "1");
        file.addCommentLine("comment");
        file.addLine("ab", "2");
        file.addLine("abc", "3");
        file.addCommentLine("#c");
        file.addLine("d", "4");
        assertThat(file.generateContent()).isEqualTo("a   1\n# comment\nab  2\nabc 3\n#c\nd   4");
    }

    @Test
    public void argumentFileIsGeneratedProperly() throws IOException {
        final ArgumentsFile file = new ArgumentsFile();
        file.addLine("a", "1");
        file.addLine("ab", "2");
        file.addLine("abc", "3");

        final File path = file.writeToTemporaryOrUseAlreadyExisting();

        assertThat(Files.asCharSource(path, Charsets.UTF_8).read()).isEqualTo("a   1\nab  2\nabc 3");
    }

    @Test
    public void fileIsReused_ifItWasGeneratedPreviouslyAndContainsSameArguments() throws IOException {
        final ArgumentsFile file1 = new ArgumentsFile();
        file1.addLine("a", "1");
        file1.addLine("ab", "2");
        file1.addLine("abc", "3");

        final File path1 = file1.writeToTemporaryOrUseAlreadyExisting();
        final long firstModification = path1.lastModified();

        final ArgumentsFile file2 = new ArgumentsFile();
        file2.addLine("a", "1");
        file2.addLine("ab", "2");
        file2.addLine("abc", "3");

        final File path2 = file1.writeToTemporaryOrUseAlreadyExisting();
        final long lastModification = path2.lastModified();

        assertThat(Files.asCharSource(path2, Charsets.UTF_8).read()).isEqualTo("a   1\nab  2\nabc 3");
        assertThat(firstModification).isEqualTo(lastModification);
    }
}
