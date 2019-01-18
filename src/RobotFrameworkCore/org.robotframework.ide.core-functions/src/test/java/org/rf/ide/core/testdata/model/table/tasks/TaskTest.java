/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.stream.Stream;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TaskTest {

    @Test
    public void checkPropertiesOfTaskWithoutName() {
        final Task task = new Task(null);

        assertThat(task.isPresent()).isFalse();
        assertThat(task.getName()).isNull();
        assertThat(task.getHolder()).isSameAs(task);
        assertThat(task.getModelType()).isEqualTo(ModelType.TASK);
        assertThat(task.getDeclaration()).isSameAs(task.getName());
    }

    @Test
    public void checkPropertiesOfTaskWithName() {
        final Task task = new Task(RobotToken.create("task"));

        assertThat(task.isPresent()).isTrue();
        assertThat(task.getName()).isNotNull();
        assertThat(task.getName().getText()).isEqualTo("task");
        assertThat(task.getName().getTypes()).contains(RobotTokenType.TASK_NAME);
        assertThat(task.getHolder()).isSameAs(task);
        assertThat(task.getModelType()).isEqualTo(ModelType.TASK);
        assertThat(task.getDeclaration()).isSameAs(task.getName());
    }

    @Test
    public void theNameOfTaskCanBeChanged() {
        final Task task = new Task(null);
        assertThat(task.isPresent()).isFalse();
        assertThat(task.getName()).isNull();

        task.setName(RobotToken.create("task"));
        assertThat(task.isPresent()).isTrue();
        assertThat(task.getName()).isNotNull();
        assertThat(task.getName().getText()).isEqualTo("task");
        assertThat(task.getName().getTypes()).contains(RobotTokenType.TASK_NAME);

        task.setName(null);
        assertThat(task.isPresent()).isFalse();
        assertThat(task.getName()).isNull();
    }

    @Test
    public void itIsPossibleToAddAndRemoveChildElements() {
        final Task task = new Task(RobotToken.create("task"));

        final RobotExecutableRow<Task> child1 = row("kw1", "arg1", "arg2");
        final RobotExecutableRow<Task> child2 = row("kw2", "arg1", "arg2");
        final RobotExecutableRow<Task> child3 = row("kw3", "arg1", "arg2");

        task.addElement(child1);
        assertThat(task.getElements()).containsExactly(child1);

        task.removeElement(child1);
        assertThat(task.getElements()).isEmpty();

        task.addElement(child2);
        task.addElement(child3);
        assertThat(task.getElements()).containsExactly(child2, child3);

        task.removeAllElements();
        assertThat(task.getElements()).isEmpty();

        task.addElement(child1);
        task.addElement(child2);
        task.addElement(child3);
        assertThat(task.getElements()).containsExactly(child1, child2, child3);

        task.removeElementAt(1);
        assertThat(task.getElements()).containsExactly(child1, child3);
    }

    @Test
    public void itIsPossibleToMoveChildUp() {
        final Task task = new Task(RobotToken.create("task"));

        final RobotExecutableRow<Task> child1 = row("kw1", "arg1", "arg2");
        final RobotExecutableRow<Task> child2 = row("kw2", "arg1", "arg2");
        final RobotExecutableRow<Task> child3 = row("kw3", "arg1", "arg2");

        task.addElement(child1);
        task.addElement(child2);
        task.addElement(child3);
        assertThat(task.getElements()).containsExactly(child1, child2, child3);

        task.moveElementUp(child3);
        assertThat(task.getElements()).containsExactly(child1, child3, child2);

        task.moveElementUp(child3);
        assertThat(task.getElements()).containsExactly(child3, child1, child2);

        task.moveElementUp(child3);
        assertThat(task.getElements()).containsExactly(child3, child1, child2);
    }

    @Test
    public void itIsPossibleToMoveChildDown() {
        final Task task = new Task(RobotToken.create("task"));

        final RobotExecutableRow<Task> child1 = row("kw1", "arg1", "arg2");
        final RobotExecutableRow<Task> child2 = row("kw2", "arg1", "arg2");
        final RobotExecutableRow<Task> child3 = row("kw3", "arg1", "arg2");

        task.addElement(child1);
        task.addElement(child2);
        task.addElement(child3);
        assertThat(task.getElements()).containsExactly(child1, child2, child3);

        task.moveElementDown(child1);
        assertThat(task.getElements()).containsExactly(child2, child1, child3);

        task.moveElementDown(child1);
        assertThat(task.getElements()).containsExactly(child2, child3, child1);

        task.moveElementDown(child1);
        assertThat(task.getElements()).containsExactly(child2, child3, child1);
    }

    @Test
    public void itIsPossibleToReplaceOneChildByAnother() {
        final Task task = new Task(RobotToken.create("task"));

        final RobotExecutableRow<Task> child1 = row("kw1", "arg1", "arg2");
        final RobotExecutableRow<Task> child2 = row("kw2", "arg1", "arg2");

        task.addElement(child1);
        assertThat(task.getElements()).containsExactly(child1);
        assertThat(child1.getParent()).isSameAs(task);
        assertThat(child2.getParent()).isNull();

        task.replaceElement(child1, child2);
        assertThat(task.getElements()).containsExactly(child2);
        assertThat(child1.getParent()).isSameAs(task);
        assertThat(child2.getParent()).isSameAs(task);
    }

    @Test
    public void itIsNotPossibleToChangeChildrenListOutsideOfTask() {
        final Task task = new Task(RobotToken.create("task"));
        final RobotExecutableRow<Task> child = row("kw1", "arg1", "arg2");

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> task.getElements().add(child));

    }

    @Test
    public void unknownSettingTest() {
        final Task task = new Task(RobotToken.create("task"));

        task.newUnknownSetting(0);
        task.newUnknownSetting(0, "[set]");

        assertThat(task.getElements()).hasSize(2)
                .allMatch(setting -> setting.getModelType() == ModelType.TASK_SETTING_UNKNOWN)
                .allMatch(setting -> setting.getDeclaration().getTypes().contains(RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION))
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[set]", "[]");
        
        assertThat(task.getUnknownSettings()).hasSize(2)
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[set]", "[]");
    }

    @Test
    public void documentationSettingTest() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        task.newDocumentation(0);
        task.newDocumentation(1, "[documentation]");

        assertThat(task.getElements()).hasSize(2)
                .allMatch(setting -> setting.getModelType() == ModelType.TASK_DOCUMENTATION)
                .allMatch(setting -> setting.getDeclaration()
                        .getTypes()
                        .contains(RobotTokenType.TASK_SETTING_DOCUMENTATION))
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "[documentation]");

        assertThat(task.getDocumentation()).hasSize(2)
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Documentation]", "[documentation]");

        assertThat(task.getLastDocumentation().getDeclaration().getText()).isEqualTo("[documentation]");
    }

    @Test
    public void tagsSettingTest() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        task.newTags(0);
        task.newTags(1, "[tags]");

        assertThat(task.getElements()).hasSize(2)
                .allMatch(setting -> setting.getModelType() == ModelType.TASK_TAGS)
                .allMatch(setting -> setting.getDeclaration()
                        .getTypes()
                        .contains(RobotTokenType.TASK_SETTING_TAGS_DECLARATION))
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Tags]", "[tags]");

        assertThat(task.getTags()).hasSize(2)
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Tags]", "[tags]");

        assertThat(task.getLastTags().getDeclaration().getText()).isEqualTo("[tags]");
    }

    @Test
    public void setupSettingTest() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        task.newSetup(0);
        task.newSetup(1, "[setup]");

        assertThat(task.getElements()).hasSize(2)
                .allMatch(setting -> setting.getModelType() == ModelType.TASK_SETUP)
                .allMatch(setting -> setting.getDeclaration()
                        .getTypes()
                        .contains(RobotTokenType.TASK_SETTING_SETUP))
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Setup]", "[setup]");

        assertThat(task.getSetups()).hasSize(2)
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Setup]", "[setup]");

        assertThat(task.getLastSetup().getDeclaration().getText()).isEqualTo("[setup]");
    }

    @Test
    public void teardownSettingTest() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        task.newTeardown(0);
        task.newTeardown(1, "[teardown]");

        assertThat(task.getElements()).hasSize(2)
                .allMatch(setting -> setting.getModelType() == ModelType.TASK_TEARDOWN)
                .allMatch(setting -> setting.getDeclaration()
                        .getTypes()
                        .contains(RobotTokenType.TASK_SETTING_TEARDOWN))
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "[teardown]");

        assertThat(task.getTeardowns()).hasSize(2)
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Teardown]", "[teardown]");

        assertThat(task.getLastTeardown().getDeclaration().getText()).isEqualTo("[teardown]");
    }

    @Test
    public void templateSettingTest() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        task.newTemplate(0);
        task.newTemplate(1, "[template]");

        assertThat(task.getElements()).hasSize(2)
                .allMatch(setting -> setting.getModelType() == ModelType.TASK_TEMPLATE)
                .allMatch(setting -> setting.getDeclaration().getTypes().contains(RobotTokenType.TASK_SETTING_TEMPLATE))
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Template]", "[template]");

        assertThat(task.getTemplates()).hasSize(2)
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Template]", "[template]");

        assertThat(task.getLastTemplate().getDeclaration().getText()).isEqualTo("[template]");
    }

    @Test
    public void timeoutSettingTest() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        task.newTimeout(0);
        task.newTimeout(1, "[timeout]");

        assertThat(task.getElements()).hasSize(2)
                .allMatch(setting -> setting.getModelType() == ModelType.TASK_TIMEOUT)
                .allMatch(setting -> setting.getDeclaration().getTypes().contains(RobotTokenType.TASK_SETTING_TIMEOUT))
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "[timeout]");

        assertThat(task.getTimeouts()).hasSize(2)
                .extracting(AModelElement::getDeclaration)
                .extracting(RobotToken::getText)
                .containsExactly("[Timeout]", "[timeout]");

        assertThat(task.getLastTimeout().getDeclaration().getText()).isEqualTo("[timeout]");
    }

    @Test
    public void checkDuplicatedSetting() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        final LocalSetting<Task> unknown1 = task.newUnknownSetting(0);
        final LocalSetting<Task> unknown2 = task.newUnknownSetting(1);
        final LocalSetting<Task> doc1 = task.newDocumentation(2);
        final LocalSetting<Task> doc2 = task.newDocumentation(3);
        final LocalSetting<Task> tags1 = task.newTags(4);
        final LocalSetting<Task> tags2 = task.newTags(5);
        final LocalSetting<Task> setup1 = task.newSetup(6);
        final LocalSetting<Task> setup2 = task.newSetup(7);
        final LocalSetting<Task> teardown1 = task.newTeardown(8);
        final LocalSetting<Task> teardown2 = task.newTeardown(9);
        final LocalSetting<Task> template1 = task.newTemplate(10);
        final LocalSetting<Task> template2 = task.newTemplate(11);
        final LocalSetting<Task> timeout1 = task.newTimeout(12);
        final LocalSetting<Task> timeout2 = task.newTimeout(13);

        assertThat(task.isDuplicatedSetting(unknown1)).isFalse();
        assertThat(task.isDuplicatedSetting(unknown2)).isFalse();
        assertThat(Stream.of(doc1, tags1, setup1, teardown1, template1, timeout1))
                .noneMatch(setting -> task.isDuplicatedSetting(setting));
        assertThat(Stream.of(doc2, tags2, setup2, teardown2, template2, timeout2))
                .allMatch(setting -> task.isDuplicatedSetting(setting));
    }

    @Test
    public void templateKeywordIsReturnedFromLocalSetting() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        final LocalSetting<Task> template = task.newTemplate(0);
        template.addToken("keyword");
        template.addToken("to");
        template.addToken("use");

        assertThat(task.getTemplateKeywordName()).isEqualTo("keyword to use");
    }

    @Test
    public void templateKeywordIsReturnedFromLocalSetting_evenWhenGlobalIsDefined() {
        final TaskTemplate globalTemplate = new TaskTemplate(RobotToken.create("Task Template"));
        globalTemplate.setKeywordName("global kw");

        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        file.includeSettingTableSection();
        file.getSettingTable().addTaskTemplate(globalTemplate);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        final LocalSetting<Task> template = task.newTemplate(0);
        template.addToken("keyword");

        assertThat(task.getTemplateKeywordName()).isEqualTo("keyword");
    }

    @Test
    public void templateKeywordIsReturnedFromSettingsTable_whenThereIsNoLocalTemplate() {
        final TaskTemplate globalTemplate = new TaskTemplate(RobotToken.create("Task Template"));
        globalTemplate.setKeywordName("global kw");

        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        file.includeSettingTableSection();
        file.getSettingTable().addTaskTemplate(globalTemplate);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        assertThat(task.getTemplateKeywordName()).isEqualTo("global kw");
    }

    @Test
    public void templateKeywordIsNotReturned_whenTemplatesAreDuplicated() {
        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        final LocalSetting<Task> template1 = task.newTemplate(0);
        template1.addToken("keyword1");
        final LocalSetting<Task> template2 = task.newTemplate(1);
        template2.addToken("keyword2");

        assertThat(task.getTemplateKeywordName()).isNull();
    }

    @Test
    public void templateKeywordIsNotReturned_whenGlobalIsDefinedButLocalCancelsIt() {
        final TaskTemplate globalTemplate = new TaskTemplate(RobotToken.create("Task Template"));
        globalTemplate.setKeywordName("global kw");

        final RobotFileOutput parentFileOutput = new RobotFileOutput(new RobotVersion(3, 1));
        final RobotFile file = new RobotFile(parentFileOutput);
        file.includeSettingTableSection();
        file.getSettingTable().addTaskTemplate(globalTemplate);
        final TaskTable table = new TaskTable(file);
        final Task task = new Task(RobotToken.create("task"));
        task.setParent(table);

        final LocalSetting<Task> template = task.newTemplate(0);
        template.addToken("NONE");

        assertThat(task.getTemplateKeywordName()).isNull();
    }

    private static RobotExecutableRow<Task> row(final String... cells) {
        final RobotExecutableRow<Task> child = new RobotExecutableRow<>();
        if (cells == null || cells.length == 0) {
            return child;
        }
        child.setAction(RobotToken.create(cells[0]));
        for (int i = 1; i < cells.length; i++) {
            child.addArgument(RobotToken.create(cells[i]));
        }
        return child;
    }
}
