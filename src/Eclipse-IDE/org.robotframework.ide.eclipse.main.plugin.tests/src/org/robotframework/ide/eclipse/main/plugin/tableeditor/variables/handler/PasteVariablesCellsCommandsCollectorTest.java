package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetDictItemsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetListItemsCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetScalarValueCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

public class PasteVariablesCellsCommandsCollectorTest {

    @Test
    public void collectorHasNoElements_whenClipboardHasNoVariables() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final RedClipboard clipboard = mock(RedClipboard.class);
        when(clipboard.hasVariables()).thenReturn(false);
        when(clipboard.getVariables()).thenReturn(null);

        assertThat(collector.hasRobotElementsInClipboard(clipboard)).isFalse();
        assertThat(collector.getRobotElementsFromClipboard(clipboard)).isNull();
    }

    @Test
    public void collectorHasElements_whenClipboardHasVariables() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final RobotVariable[] vars = new RobotVariable[] { new RobotVariable(null, null) };

        final RedClipboard clipboard = mock(RedClipboard.class);
        when(clipboard.hasVariables()).thenReturn(true);
        when(clipboard.getVariables()).thenReturn(vars);

        assertThat(collector.hasRobotElementsInClipboard(clipboard)).isTrue();
        assertThat(collector.getRobotElementsFromClipboard(clipboard)).isSameAs(vars);
    }

    @Test
    public void nameOfVariableIsReturnedAsValueToPaste_whenFirstColumnIsGiven() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final ScalarVariable s = new ScalarVariable("${scalar}", RobotToken.create("${scalar}"),
                VariableScope.TEST_SUITE);
        s.addValue(RobotToken.create("42"));
        final RobotVariable scalar = new RobotVariable(null, s);

        final ListVariable l = new ListVariable("@{list}", RobotToken.create("@{list}"), VariableScope.TEST_SUITE);
        l.addItem(RobotToken.create("a"));
        l.addItem(RobotToken.create("b"));
        l.addItem(RobotToken.create("c"));
        final RobotVariable list = new RobotVariable(null, l);

        final DictionaryVariable d = new DictionaryVariable("&{dict}", RobotToken.create("&{dict}"),
                VariableScope.TEST_SUITE);
        d.addKeyValuePair(0,
                new DictionaryKeyValuePair(RobotToken.create("a=1"), RobotToken.create("a"), RobotToken.create("1")));
        d.addKeyValuePair(1,
                new DictionaryKeyValuePair(RobotToken.create("b=2"), RobotToken.create("b"), RobotToken.create("2")));
        final RobotVariable dictionary = new RobotVariable(null, d);

        assertThat(collector.findValuesToPaste(scalar, 0, -1)).containsExactly("${scalar}");
        assertThat(collector.findValuesToPaste(list, 0, -1)).containsExactly("@{list}");
        assertThat(collector.findValuesToPaste(dictionary, 0, -1)).containsExactly("&{dict}");
    }

    @Test
    public void valuesOfVariableIsReturnedAsValueToPaste_whenSecondColumnIsGiven() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final ScalarVariable s = new ScalarVariable("${scalar}", RobotToken.create("${scalar}"),
                VariableScope.TEST_SUITE);
        s.addValue(RobotToken.create("42"));
        final RobotVariable scalar = new RobotVariable(null, s);

        final ListVariable l = new ListVariable("@{list}", RobotToken.create("@{list}"), VariableScope.TEST_SUITE);
        l.addItem(RobotToken.create("a"));
        l.addItem(RobotToken.create("b"));
        l.addItem(RobotToken.create("c"));
        final RobotVariable list = new RobotVariable(null, l);

        final DictionaryVariable d = new DictionaryVariable("&{dict}", RobotToken.create("&{dict}"),
                VariableScope.TEST_SUITE);
        d.addKeyValuePair(0,
                new DictionaryKeyValuePair(RobotToken.create("a=1"), RobotToken.create("a"), RobotToken.create("1")));
        d.addKeyValuePair(1,
                new DictionaryKeyValuePair(RobotToken.create("b=2"), RobotToken.create("b"), RobotToken.create("2")));
        final RobotVariable dictionary = new RobotVariable(null, d);

        assertThat(collector.findValuesToPaste(scalar, 1, -1)).containsExactly("42");
        assertThat(collector.findValuesToPaste(list, 1, -1)).containsExactly("a", "b", "c");
        assertThat(collector.findValuesToPaste(dictionary, 1, -1)).containsExactly("a=1", "b=2");
    }

    @Test
    public void commentsOfVariableIsReturnedAsValueToPaste_whenThirdColumnIsGiven() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final ScalarVariable s = new ScalarVariable("${scalar}", RobotToken.create("${scalar}"),
                VariableScope.TEST_SUITE);
        s.addCommentPart(RobotToken.create("# some scalar"));
        final RobotVariable scalar = new RobotVariable(null, s);

        final ListVariable l = new ListVariable("@{list}", RobotToken.create("@{list}"), VariableScope.TEST_SUITE);
        l.addCommentPart(RobotToken.create("# some list"));
        final RobotVariable list = new RobotVariable(null, l);

        final DictionaryVariable d = new DictionaryVariable("&{dict}", RobotToken.create("&{dict}"),
                VariableScope.TEST_SUITE);
        d.addCommentPart(RobotToken.create("# some dictionary"));
        final RobotVariable dictionary = new RobotVariable(null, d);

        assertThat(collector.findValuesToPaste(scalar, 2, -1)).containsExactly("# some scalar");
        assertThat(collector.findValuesToPaste(list, 2, -1)).containsExactly("# some list");
        assertThat(collector.findValuesToPaste(dictionary, 2, -1)).containsExactly("# some dictionary");
    }

    @Test
    public void commandForSettingVariableNameIsReturned_whenPastingValuesIntoFirstColumn() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final ScalarVariable s = new ScalarVariable("${scalar}", RobotToken.create("${scalar}"),
                VariableScope.TEST_SUITE);
        s.addValue(RobotToken.create("42"));
        s.addCommentPart(RobotToken.create("# some scalar"));
        final RobotElement variable = new RobotVariable(null, s);

        final List<EditorCommand> commands = collector.collectPasteCommandsForSelectedElement(variable,
                newArrayList("${newName}"), 0, -1);

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0)).isInstanceOf(SetVariableNameCommand.class);
    }

    @Test
    public void commandForSettingScalarValueIsReturned_whenPastingValuesIntoSecondColumnOfScalar() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final ScalarVariable s = new ScalarVariable("${scalar}", RobotToken.create("${scalar}"),
                VariableScope.TEST_SUITE);
        s.addValue(RobotToken.create("42"));
        s.addCommentPart(RobotToken.create("# some scalar"));
        final RobotElement variable = new RobotVariable(null, s);

        final List<EditorCommand> commands = collector.collectPasteCommandsForSelectedElement(variable,
                newArrayList("${newName}"), 1, -1);

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0)).isInstanceOf(SetScalarValueCommand.class);
    }

    @Test
    public void commandForSettingListValueIsReturned_whenPastingValuesIntoSecondColumnOfList() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final ListVariable l = new ListVariable("@{list}", RobotToken.create("@{list}"), VariableScope.TEST_SUITE);
        l.addItem(RobotToken.create("a"));
        l.addItem(RobotToken.create("b"));
        l.addItem(RobotToken.create("c"));
        l.addCommentPart(RobotToken.create("# some list"));
        final RobotElement variable = new RobotVariable(null, l);

        final List<EditorCommand> commands = collector.collectPasteCommandsForSelectedElement(variable,
                newArrayList("1", "2"), 1, -1);

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0)).isInstanceOf(SetListItemsCommand.class);
    }

    @Test
    public void commandForSettingDictionaryValueIsReturned_whenPastingValuesIntoSecondColumnOfDictionary() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final DictionaryVariable d = new DictionaryVariable("&{dict}", RobotToken.create("&{dict}"),
                VariableScope.TEST_SUITE);
        d.addKeyValuePair(0,
                new DictionaryKeyValuePair(RobotToken.create("a=1"), RobotToken.create("a"), RobotToken.create("1")));
        d.addKeyValuePair(1,
                new DictionaryKeyValuePair(RobotToken.create("b=2"), RobotToken.create("b"), RobotToken.create("2")));
        d.addCommentPart(RobotToken.create("# some dictionary"));
        final RobotElement variable = new RobotVariable(null, d);

        final List<EditorCommand> commands = collector.collectPasteCommandsForSelectedElement(variable,
                newArrayList("1=2", "2=3"), 1, -1);

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0)).isInstanceOf(SetDictItemsCommand.class);
    }

    @Test
    public void commandForSettingVariableCommentIsReturned_whenPastingValuesIntoThridColumn() {
        final PasteVariablesCellsCommandsCollector collector = new PasteVariablesCellsCommandsCollector();

        final ScalarVariable s = new ScalarVariable("${scalar}", RobotToken.create("${scalar}"),
                VariableScope.TEST_SUITE);
        s.addValue(RobotToken.create("42"));
        s.addCommentPart(RobotToken.create("# some scalar"));
        final RobotElement variable = new RobotVariable(null, s);

        final List<EditorCommand> commands = collector.collectPasteCommandsForSelectedElement(variable,
                newArrayList("new comment"), 2, -1);

        assertThat(commands).hasSize(1);
        assertThat(commands.get(0)).isInstanceOf(SetVariableCommentCommand.class);
    }

}
