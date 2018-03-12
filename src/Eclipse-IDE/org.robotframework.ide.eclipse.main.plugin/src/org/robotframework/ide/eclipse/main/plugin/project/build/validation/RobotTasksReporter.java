/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask.Priority;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;

public class RobotTasksReporter {

    private final RobotSuiteFile fileModel;
    private final ValidationReportingStrategy reporter;

    private final boolean tasksDetectingEnabled;
    private final Map<String, Priority> tasksTags;

    private final Pattern tasksPattern;

    public RobotTasksReporter(final RobotSuiteFile fileModel, final ValidationReportingStrategy reporter) {
        this(fileModel, reporter, RedPlugin.getDefault().getPreferences());
    }

    @VisibleForTesting
    RobotTasksReporter(final RobotSuiteFile fileModel, final ValidationReportingStrategy reporter,
            final RedPreferences preferences) {
        this.fileModel = fileModel;
        this.reporter = reporter;
        this.tasksDetectingEnabled = preferences.isTasksDetectionEnabled();
        this.tasksTags = preferences.getTaskTagsWithPriorities();
        this.tasksPattern = Pattern.compile(String.join("|", tasksTags.keySet()));
    }

    public void reportTasks() {
        if (!tasksDetectingEnabled || tasksTags.isEmpty()) {
            return;
        }

        final RobotFile model = fileModel.getLinkedElement();
        final List<RobotLine> content = model.getFileContent();

        int currentLine = 1;
        for (final RobotLine line : content) {
            final String comment = getComment(line);
            final List<Range<Integer>> tagsRanges = calculateTagsRanges(comment);

            for (int i = 0; i < tagsRanges.size(); i++) {
                final int startOffset = tagsRanges.get(i).lowerEndpoint();
                final int endOffset = tagsRanges.get(i).upperEndpoint();
                final int nextStartOffset = i + 1 < tagsRanges.size() ? tagsRanges.get(i + 1).lowerEndpoint()
                        : comment.length();

                final String matchedTag = comment.substring(startOffset, endOffset);
                final String description = comment.substring(startOffset, nextStartOffset).trim();
                final RobotTask task = new RobotTask(tasksTags.get(matchedTag), description, currentLine);
                reporter.handleTask(task, fileModel.getFile());
            }

            currentLine++;
        }
    }

    private String getComment(final RobotLine line) {
        final List<IRobotLineElement> lineElements = line.getLineElements();

        return lineElements.stream()
                .filter(element -> element.getTypes().contains(RobotTokenType.START_HASH_COMMENT))
                .map(lineElements::indexOf)
                .findFirst()
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .map(index -> lineElements.subList(index, lineElements.size()))
                .flatMap(Collection::stream)
                .map(IRobotLineElement::getText)
                .collect(joining(" "));
    }

    private List<Range<Integer>> calculateTagsRanges(final String comment) {
        final List<Range<Integer>> foundTagsRanges = new ArrayList<>();
        final Matcher tasksMatcher = tasksPattern.matcher(comment);
        while (tasksMatcher.find()) {
            final int startOffset = tasksMatcher.start();
            final int endOffset = tasksMatcher.end();

            foundTagsRanges.add(Range.openClosed(startOffset, endOffset));
        }
        return foundTagsRanges;
    }
}
