/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.DumpContext;
import org.rf.ide.core.testdata.DumpedResultBuilder;
import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.IRobotFileDumper;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.SettingTableElementsComparator;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TableHeaderComparator;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;
import org.rf.ide.core.testdata.text.write.tables.KeywordsSectionTableDumper;
import org.rf.ide.core.testdata.text.write.tables.SettingsSectionTableDumper;
import org.rf.ide.core.testdata.text.write.tables.TasksSectionTableDumper;
import org.rf.ide.core.testdata.text.write.tables.TestCasesSectionTableDumper;
import org.rf.ide.core.testdata.text.write.tables.VariablesSectionTableDumper;

import com.google.common.annotations.VisibleForTesting;

public abstract class ARobotFileDumper implements IRobotFileDumper {

    private final DumperHelper dumpHelper;

    private DumpContext dumpContext;

    public ARobotFileDumper() {
        this.dumpHelper = new DumperHelper(this);
    }

    @Override
    public DumpedResult dump(final DumpContext ctx, final RobotFile model) {
        this.dumpContext = ctx;
        return newLines(model, new DumpedResultBuilder());
    }

    @SuppressWarnings("unchecked")
    private DumpedResult newLines(final RobotFile model, final DumpedResultBuilder builder) {
        final List<RobotLine> lines = new ArrayList<>(0);
        builder.producedLines(lines);
        this.dumpHelper.setTokenDumpListener(builder);

        final SectionBuilder sectionBuilder = new SectionBuilder();
        final List<Section> sections = sectionBuilder.build(model);

        dumpUntilRobotHeaderSection(model, sections, 0, lines);

        final SettingTable settingTable = model.getSettingTable();
        final List<AModelElement<SettingTable>> sortedSettings = sortSettings(settingTable);
        final VariableTable variableTable = model.getVariableTable();
        final List<AModelElement<VariableTable>> variables = new ArrayList<>(variableTable.getVariables());

        final TestCaseTable testCaseTable = model.getTestCaseTable();
        final List<AModelElement<TestCaseTable>> testCases = new ArrayList<>(testCaseTable.getTestCases());
        final TaskTable taskTable = model.getTasksTable();
        final List<AModelElement<TaskTable>> tasks = new ArrayList<>(taskTable.getTasks());
        final KeywordTable keywordTable = model.getKeywordTable();
        final List<AModelElement<KeywordTable>> keywords = new ArrayList<>(keywordTable.getKeywords());

        final List<TableHeader<? extends ARobotSectionTable>> headers = new ArrayList<>(0);
        headers.addAll(settingTable.getHeaders());
        headers.addAll(variableTable.getHeaders());
        headers.addAll(testCaseTable.getHeaders());
        headers.addAll(taskTable.getHeaders());
        headers.addAll(keywordTable.getHeaders());
        Collections.sort(headers, new TableHeaderComparator<>());

        for (final TableHeader<? extends ARobotSectionTable> th : headers) {
            final int sectionWithHeader = getSectionWithHeader(sections, th);

            if (th.getModelType() == ModelType.SETTINGS_TABLE_HEADER) {
                final List<AModelElement<SettingTable>> copy = new ArrayList<>(sortedSettings);
                final TableHeader<SettingTable> header = (TableHeader<SettingTable>) th;
                final SettingsSectionTableDumper dumper = new SettingsSectionTableDumper(dumpHelper);
                dumper.dump(model, sections, sectionWithHeader, header, copy, lines);
                sortedSettings.clear();
                sortedSettings.addAll(copy);

            } else if (th.getModelType() == ModelType.VARIABLES_TABLE_HEADER) {
                final List<AModelElement<VariableTable>> copy = copyVariables(variables);
                final TableHeader<VariableTable> header = (TableHeader<VariableTable>) th;
                final VariablesSectionTableDumper dumper = new VariablesSectionTableDumper(dumpHelper);
                dumper.dump(model, sections, sectionWithHeader, header, copy, lines);
                variables.clear();
                variables.addAll(copy);

            } else if (th.getModelType() == ModelType.KEYWORDS_TABLE_HEADER) {
                final List<AModelElement<KeywordTable>> copy = new ArrayList<>(keywords);
                final TableHeader<KeywordTable> header = (TableHeader<KeywordTable>) th;
                final KeywordsSectionTableDumper dumper = new KeywordsSectionTableDumper(dumpHelper);
                dumper.dump(model, sections, sectionWithHeader, header, copy, lines);
                keywords.clear();
                keywords.addAll(copy);

            } else if (th.getModelType() == ModelType.TEST_CASE_TABLE_HEADER) {
                final List<AModelElement<TestCaseTable>> copy = new ArrayList<>(testCases);
                final TableHeader<TestCaseTable> header = (TableHeader<TestCaseTable>) th;
                final TestCasesSectionTableDumper dumper = new TestCasesSectionTableDumper(dumpHelper);
                dumper.dump(model, sections, sectionWithHeader, header, copy, lines);
                testCases.clear();
                testCases.addAll(copy);

            } else if (th.getModelType() == ModelType.TASKS_TABLE_HEADER) {
                final List<AModelElement<TaskTable>> copy = new ArrayList<>(tasks);
                final TableHeader<TaskTable> header = (TableHeader<TaskTable>) th;
                final TasksSectionTableDumper dumper = new TasksSectionTableDumper(dumpHelper);
                dumper.dump(model, sections, sectionWithHeader, header, copy, lines);
                tasks.clear();
                tasks.addAll(copy);
            }

            if (sectionWithHeader > -1) {
                dumpUntilRobotHeaderSection(model, sections, sectionWithHeader + 1, lines);
            }
        }

        final List<Section> userSections = filterUserTableHeadersOnly(sections);
        dumpUntilRobotHeaderSection(model, userSections, 0, lines);

        dumpHelper.addEOFinCaseIsMissing(model, lines);

        return builder.build();
    }

    private List<AModelElement<VariableTable>> copyVariables(final List<AModelElement<VariableTable>> elems) {
        final List<AModelElement<VariableTable>> copied = new ArrayList<>(elems);
        copied.forEach(elem -> fixVariableDeclarationToName((AVariable) elem));
        return copied;
    }

    private void fixVariableDeclarationToName(final AVariable var) {
        final String varName = var.getName();
        if (varName != null && !varName.isEmpty()) {
            String varDeclaration = varName;
            final String correctBeginOfVariable = var.getType().getIdentificator() + "{";
            if (!varName.startsWith(correctBeginOfVariable)) {
                varDeclaration = correctBeginOfVariable + varName;
            }
            if (!varDeclaration.endsWith("}")) {
                varDeclaration += "}";
            }
            var.getDeclaration().setText(varDeclaration);
        }
    }

    private List<AModelElement<SettingTable>> sortSettings(final SettingTable settingTable) {
        final List<AModelElement<SettingTable>> list = new ArrayList<>();

        list.addAll(settingTable.getDefaultTags());
        list.addAll(settingTable.getDocumentation());
        list.addAll(settingTable.getForceTags());
        list.addAll(settingTable.getSuiteSetups());
        list.addAll(settingTable.getSuiteTeardowns());
        list.addAll(settingTable.getTestSetups());
        list.addAll(settingTable.getTestTeardowns());
        list.addAll(settingTable.getTestTemplates());
        list.addAll(settingTable.getTestTimeouts());
        list.addAll(settingTable.getUnknownSettings());

        list.addAll(settingTable.getMetadatas());
        list.addAll(settingTable.getImports());

        Collections.sort(list, new SettingTableElementsComparator());
        repositionElementsBaseOnList(list, settingTable.getImports());
        repositionElementsBaseOnList(list, settingTable.getMetadatas());

        return list;
    }

    @VisibleForTesting
    void repositionElementsBaseOnList(final List<AModelElement<SettingTable>> src,
            final List<? extends AModelElement<SettingTable>> correctors) {
        if (correctors.size() >= 2) {
            int hitCorrectors = 0;

            AModelElement<SettingTable> currentCorrector = correctors.get(hitCorrectors);
            for (int i = 0; i < src.size(); i++) {
                final AModelElement<SettingTable> m = src.get(i);
                if (correctors.contains(m)) {
                    if (currentCorrector == m) {
                        hitCorrectors++;
                        if (hitCorrectors < correctors.size()) {
                            currentCorrector = correctors.get(hitCorrectors);
                        } else {
                            if (isNextTheSameAsCurrent(src, m, i)) {
                                src.remove(i);
                                i--;
                            }
                        }
                    } else {
                        if (hitCorrectors < correctors.size()) {
                            if (currentCorrector.getBeginPosition().isNotSet()) {
                                src.add(i, currentCorrector);
                            } else {
                                src.set(i, currentCorrector);
                            }
                        } else {
                            src.remove(i);
                        }

                        i--;
                    }
                }
            }

            if (hitCorrectors != correctors.size()) {
                throw new IllegalStateException("Not all elements included in output before.");
            }
        }
    }

    @VisibleForTesting
    boolean isNextTheSameAsCurrent(final List<AModelElement<SettingTable>> src, final AModelElement<SettingTable> m,
            final int currentIndex) {
        return currentIndex + 1 < src.size() && m == src.get(currentIndex + 1);
    }

    private List<Section> filterUserTableHeadersOnly(final List<Section> sections) {
        final List<Section> userSections = new ArrayList<>(0);
        for (final Section section : sections) {
            final SectionType type = section.getType();
            if (type == SectionType.TRASH || type == SectionType.USER_TABLE) {
                userSections.add(section);
            }
        }

        return userSections;
    }

    private void dumpUntilRobotHeaderSection(final RobotFile model, final List<Section> sections,
            final int currentSection, final List<RobotLine> outLines) {
        int removedIndex = -1;

        final int sectionSize = sections.size();
        for (int sectionId = currentSection; sectionId < sectionSize; sectionId++) {
            final Section section = sections.get(sectionId);
            final SectionType type = section.getType();
            if (type == SectionType.TRASH || type == SectionType.USER_TABLE) {
                dumpFromTo(model, section.getStart(), section.getEnd(), outLines);
                removedIndex++;
            } else {
                break;
            }
        }

        for (int i = removedIndex; i > -1; i--) {
            sections.remove(currentSection);
        }
    }

    private void dumpFromTo(final RobotFile model, final FilePosition start, final FilePosition end,
            final List<RobotLine> outLines) {
        boolean meetEnd = false;

        final List<RobotLine> fileContent = model.getFileContent();
        for (final RobotLine line : fileContent) {
            for (final IRobotLineElement elem : line.getLineElements()) {
                final FilePosition elemPos = elem.getFilePosition();
                if (elemPos.isBefore(start)) {
                    continue;
                } else if (elemPos.isSamePlace(start) || elemPos.isSamePlace(end)
                        || (elemPos.isAfter(start) && elemPos.isBefore(end))) {
                    dumpHelper.getDumpLineUpdater().updateLine(model, outLines, elem);
                } else {
                    meetEnd = true;
                    break;
                }
            }

            if (meetEnd) {
                break;
            } else {
                final IRobotLineElement endOfLine = line.getEndOfLine();
                final FilePosition endOfLineFP = endOfLine.getFilePosition();
                if (endOfLineFP.isSamePlace(start) || endOfLineFP.isSamePlace(end)
                        || (endOfLineFP.isAfter(start) && endOfLineFP.isBefore(end))) {
                    dumpHelper.getDumpLineUpdater().updateLine(model, outLines, endOfLine);
                }
            }
        }
    }

    private int getSectionWithHeader(final List<Section> sections, final TableHeader<? extends ARobotSectionTable> th) {
        final int sectionsSize = sections.size();
        for (int sectionId = 0; sectionId < sectionsSize; sectionId++) {
            final Section s = sections.get(sectionId);
            final FilePosition thPos = th.getDeclaration().getFilePosition();
            if (thPos.isSamePlace(s.getStart()) || (thPos.isAfter(s.getStart()) && thPos.isBefore(s.getEnd()))) {
                return sectionId;
            }
        }

        return -1;
    }

    public Separator getSeparator(final RobotFile model, final List<RobotLine> lines, final IRobotLineElement lastToken,
            final IRobotLineElement currentToken) {
        Separator sep = null;
        final FilePosition fp = lastToken.getFilePosition();
        final FilePosition fpTok = currentToken.getFilePosition();

        boolean wasLastToken = false;
        IRobotLineElement tokenToSearch = null;
        final int offset;
        if (fpTok.isNotSet()) {
            if (fp.isNotSet()) {
                tokenToSearch = lastToken;
                offset = -1;
            } else {
                wasLastToken = true;
                tokenToSearch = lastToken;
                offset = fp.getOffset();
            }
        } else {
            tokenToSearch = currentToken;
            offset = fpTok.getOffset();
        }

        final RobotLine line;
        if (offset > -1) {
            line = model.getFileContent().get(model.getRobotLineIndexBy(offset).get());
        } else {
            if (!lines.isEmpty()) {
                line = lines.get(lines.size() - 1);
            } else {
                line = new RobotLine(0, model);
            }
        }

        final List<IRobotLineElement> elems = line.getLineElements();
        final Optional<Integer> tokenPos = line.getElementPositionInLine(tokenToSearch);
        if (tokenPos.isPresent()) {
            final Integer tokPos = tokenPos.get();
            final int start = (wasLastToken) ? tokPos + 1 : tokPos - 1;
            for (int index = start; index < elems.size() && index >= 0; index--) {
                final IRobotLineElement elem = elems.get(index);
                if (elem instanceof RobotToken) {
                    break;
                } else if (elem instanceof Separator) {
                    sep = (Separator) elem;
                    break;
                } else {
                    continue;
                }
            }

            if (sep != null) {
                final Optional<SeparatorType> separatorForLine = line.getSeparatorForLine();
                if (separatorForLine.isPresent()) {
                    if (sep.getTypes().get(0) != separatorForLine.get()) {
                        if (separatorForLine.get() == SeparatorType.PIPE) {
                            final List<Separator> seps = new ArrayList<>(0);
                            for (final IRobotLineElement e : elems) {
                                if (e instanceof Separator) {
                                    seps.add((Separator) e);
                                }
                            }

                            if (seps.size() > 1) {
                                sep = seps.get(seps.size() - 1);
                            } else {
                                sep = getPipeSeparator();
                            }
                        }
                    }
                }
            }
        }

        if (sep == null) {
            sep = Separator.matchSeparator(dumpContext.getPreferedSeparator());
        }

        if (sep == null || !isAcceptableForDefault(sep)) {
            sep = getTabSeparator();
        }

        if (sep != null) {
            final Optional<SeparatorType> sepInLine = line.getSeparatorForLine();
            if (sepInLine.isPresent()) {
                if (sepInLine.get() != sep.getTypes().get(0)) {
                    if (sepInLine.get() == SeparatorType.PIPE) {
                        sep = getPipeSeparator();
                    } else {
                        sep = getTabSeparator();
                    }
                }
            }
        }

        return sep;
    }

    public boolean isFileDirty() {
        return this.dumpContext.isDirty();
    }

    private Separator getPipeSeparator() {
        final Separator sep = new Separator();
        sep.setRaw(" | ");
        sep.setText(" | ");
        sep.setType(SeparatorType.PIPE);
        return sep;
    }

    private Separator getTabSeparator() {
        final Separator sep = new Separator();
        sep.setRaw("\t");
        sep.setText("\t");
        sep.setType(SeparatorType.TABULATOR_OR_DOUBLE_SPACE);
        return sep;
    }

    protected abstract boolean isAcceptableForDefault(final Separator separator);

    protected abstract boolean canBeSeparatorAddBeforeExecutableUnitName(final Separator currentSeparator);
}
