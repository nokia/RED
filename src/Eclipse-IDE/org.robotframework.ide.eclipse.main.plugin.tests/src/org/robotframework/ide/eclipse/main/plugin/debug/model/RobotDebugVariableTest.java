package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.junit.Test;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrameVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugVariable.RobotDebugVariableVisitor;

import com.google.common.collect.ImmutableMap;

public class RobotDebugVariableTest {

    @Test
    public void variablePropertiesCheckOfTopLevelScalar() {
        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.TEST_SUITE, false, "var", "int",
                10);
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);

        assertThat(variable.getParent()).isNull();
        assertThat(variable.getName()).isEqualTo("var");
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfScalar.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.supportsValueModification()).isTrue();
        assertThat(variable.getScope()).contains(VariableScope.TEST_SUITE);
        assertThat(variable.getImage()).isEqualTo(RedImages.VARIABLES.getDebugScalarVariableImage());

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void variablePropertiesCheckOfTopLevelList() {
        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc")));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);

        assertThat(variable.getParent()).isNull();
        assertThat(variable.getName()).isEqualTo("var");
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfList.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.getScope()).contains(VariableScope.GLOBAL);
        assertThat(variable.getImage()).isEqualTo(RedImages.VARIABLES.getDebugListVariableImage());
        variable.visitAllVariables(childVar -> assertThat(childVar.supportsValueModification()).isTrue());

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verify(visitor, times(3)).visit(any(RobotDebugVariable.class));
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void variablePropertiesCheckOfTopLevelTuple() {
        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "tuple",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc")));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);

        assertThat(variable.getParent()).isNull();
        assertThat(variable.getName()).isEqualTo("var");
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfList.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.getScope()).contains(VariableScope.GLOBAL);
        assertThat(variable.getImage()).isEqualTo(RedImages.VARIABLES.getDebugListVariableImage());
        variable.visitAllVariables(childVar -> {
            if (childVar == variable) {
                assertThat(childVar.supportsValueModification()).isTrue();
            } else {
                assertThat(childVar.supportsValueModification()).isFalse();
            }
        });


        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verify(visitor, times(3)).visit(any(RobotDebugVariable.class));
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void variablePropertiesCheckOfTopLevelDictionary() {
        final StackFrameVariable stackVariable = new StackFrameVariable(VariableScope.TEST_CASE, false, "var", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc")));
        final RobotDebugVariable variable = new RobotDebugVariable(mock(RobotStackFrame.class), stackVariable);

        assertThat(variable.getParent()).isNull();
        assertThat(variable.getName()).isEqualTo("var");
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfDictionary.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.getScope()).contains(VariableScope.TEST_CASE);
        assertThat(variable.getImage()).isEqualTo(RedImages.VARIABLES.getDebugDictionaryVariableImage());
        variable.visitAllVariables(childVar -> assertThat(childVar.supportsValueModification()).isTrue());

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verify(visitor, times(3)).visit(any(RobotDebugVariable.class));
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void variablePropertiesCheckOfInnerScalar() {
        final RobotDebugVariable parent = mock(RobotDebugVariable.class);
        final RobotDebugVariable variable = new RobotDebugVariable(parent, "var", "int", 10);

        assertThat(variable.getParent()).isSameAs(parent);
        assertThat(variable.getName()).isEqualTo("var");
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfScalar.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.getScope()).isEmpty();
        assertThat(variable.getImage()).isEqualTo(RedImages.VARIABLES.getDebugScalarVariableImage());
        variable.visitAllVariables(childVar -> assertThat(childVar.supportsValueModification()).isTrue());

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void variablePropertiesCheckOfInnerList() {
        final RobotDebugVariable parent = mock(RobotDebugVariable.class);
        final RobotDebugVariable variable = new RobotDebugVariable(parent, "var", "list",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc")));

        assertThat(variable.getParent()).isSameAs(parent);
        assertThat(variable.getName()).isEqualTo("var");
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfList.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.getScope()).isEmpty();
        assertThat(variable.getImage()).isEqualTo(RedImages.VARIABLES.getDebugListVariableImage());
        variable.visitAllVariables(childVar -> assertThat(childVar.supportsValueModification()).isTrue());

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verify(visitor, times(3)).visit(any(RobotDebugVariable.class));
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void variablePropertiesCheckOfInnerTuple() {
        final RobotDebugVariable parent = mock(RobotDebugVariable.class);
        final RobotDebugVariable variable = new RobotDebugVariable(parent, "var", "tuple",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc")));

        assertThat(variable.getParent()).isSameAs(parent);
        assertThat(variable.getName()).isEqualTo("var");
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfList.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.getScope()).isEmpty();
        assertThat(variable.getImage()).isEqualTo(RedImages.VARIABLES.getDebugListVariableImage());
        variable.visitAllVariables(childVar -> {
            if (childVar == variable) {
                assertThat(childVar.supportsValueModification()).isTrue();
            } else {
                assertThat(childVar.supportsValueModification()).isFalse();
            }
        });

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verify(visitor, times(3)).visit(any(RobotDebugVariable.class));
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void variablePropertiesCheckOfInnerDictionary() {
        final RobotDebugVariable parent = mock(RobotDebugVariable.class);
        final RobotDebugVariable variable = new RobotDebugVariable(parent, "var", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc")));

        assertThat(variable.getParent()).isSameAs(parent);
        assertThat(variable.getName()).isEqualTo("var");
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfDictionary.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.getScope()).isEmpty();
        assertThat(variable.getImage()).isEqualTo(RedImages.VARIABLES.getDebugDictionaryVariableImage());
        variable.visitAllVariables(childVar -> assertThat(childVar.supportsValueModification()).isTrue());

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verify(visitor, times(3)).visit(any(RobotDebugVariable.class));
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void variablePropertiesOfAutomaticGroup() {
        final RobotDebugVariable var1 = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.TEST_SUITE, false, "var", "int", 10));
        final RobotDebugVariable var2 = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                        newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc"))));
        final RobotDebugVariable var3 = new RobotDebugVariable(mock(RobotStackFrame.class), new StackFrameVariable(
                VariableScope.TEST_CASE, false, "var", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc"))));

        final RobotDebugVariable variable = RobotDebugVariable.createAutomatic(mock(RobotStackFrame.class),
                newArrayList(var1, var2, var3));

        assertThat(variable.getParent()).isNull();
        assertThat(variable.getName()).isEqualTo(RobotDebugVariable.AUTOMATIC_NAME);
        assertThat(variable.getReferenceTypeName()).isEmpty(); // because the values have the type
        assertThat(variable.getValue()).isInstanceOf(RobotDebugValueOfDictionary.class);
        assertThat(variable.hasValueChanged()).isFalse();
        assertThat(variable.supportsValueModification()).isFalse();
        assertThat(variable.getScope()).isEmpty();
        assertThat(variable.getImage()).isEqualTo(RedImages.getElementImage());

        final RobotDebugVariableVisitor visitor = mock(RobotDebugVariableVisitor.class);
        variable.visitAllVariables(visitor);

        verify(visitor).visit(variable);
        verify(visitor).visit(var1);
        verify(visitor).visit(var2);
        verify(visitor).visit(var3);
        verify(visitor, times(8)).visit(any(RobotDebugVariable.class));
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void itIsNotPossibleToEditVariableUsingIValues() {
        final RobotDebugVariable topLevelScalar = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.TEST_SUITE, false, "var", "int", 10));
        final RobotDebugVariable topLevelList = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                        newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc"))));
        final RobotDebugVariable topLevelDict = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.TEST_CASE, false, "var", "dict", ImmutableMap.of("k1",
                        new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc"))));

        final RobotDebugVariable innerScalar = new RobotDebugVariable(mock(RobotDebugVariable.class), "var", "int", 10);
        final RobotDebugVariable innerList = new RobotDebugVariable(mock(RobotDebugVariable.class), "var", "list",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc")));
        final RobotDebugVariable innerDict = new RobotDebugVariable(mock(RobotDebugVariable.class), "var", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc")));

        final RobotDebugVariable automaticGroup = RobotDebugVariable.createAutomatic(mock(RobotStackFrame.class),
                newArrayList());

        final List<RobotDebugVariable> allVars = newArrayList(topLevelScalar, topLevelList, topLevelDict, innerScalar,
                innerList, innerDict, automaticGroup);

        for (final RobotDebugVariable var : allVars) {
            assertThat(var.verifyValue(mock(IValue.class)));
            assertThatExceptionOfType(DebugException.class).isThrownBy(() -> var.setValue(mock(IValue.class)));
        }
    }

    @Test
    public void topLevelScalarCanBeEditedAndRequestIsSendToStackFrame() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10);
        final RobotDebugVariable topLevelScalar = new RobotDebugVariable(frame, stackVar);

        final String expression = "a  b  c";
        assertThat(topLevelScalar.verifyValue(expression)).isTrue();
        topLevelScalar.setValue(expression);

        assertThat(topLevelScalar.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariable(same(stackVar), eq(newArrayList("a  b  c")));
    }

    @Test
    public void topLevelListCanBeEditedAndRequestIsSendToStackFrame_1() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc")));
        final RobotDebugVariable topLevelList = new RobotDebugVariable(frame, stackVar);

        final String expression = "a  b  c";
        assertThat(topLevelList.verifyValue(expression)).isTrue();
        topLevelList.setValue(expression);

        assertThat(topLevelList.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariable(same(stackVar), eq(newArrayList("a", "b", "c")));
    }

    @Test
    public void topLevelListCanBeEditedAndRequestIsSendToStackFrame_2() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc")));
        final RobotDebugVariable topLevelList = new RobotDebugVariable(frame, stackVar);

        final String expression = "[a,  b,  c]";
        assertThat(topLevelList.verifyValue(expression)).isTrue();
        topLevelList.setValue(expression);

        assertThat(topLevelList.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariable(same(stackVar), eq(newArrayList("a", "b", "c")));
    }

    @Test
    public void topLevelDictionaryCanBeEditedAndRequestIsSendToStackFrame_1() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.TEST_CASE, false, "var", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc")));
        final RobotDebugVariable topLevelDict = new RobotDebugVariable(frame, stackVar);

        final String expression = "a=1  b=2  c=3";
        assertThat(topLevelDict.verifyValue(expression)).isTrue();
        topLevelDict.setValue(expression);

        assertThat(topLevelDict.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariable(same(stackVar), eq(newArrayList("a=1", "b=2", "c=3")));
    }

    @Test
    public void topLevelDictionaryCanBeEditedAndRequestIsSendToStackFrame_2() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.TEST_CASE, false, "var", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc")));
        final RobotDebugVariable topLevelDict = new RobotDebugVariable(frame, stackVar);

        final String expression = "{a=1,  b=2,  c=3}";
        assertThat(topLevelDict.verifyValue(expression)).isTrue();
        topLevelDict.setValue(expression);

        assertThat(topLevelDict.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariable(same(stackVar), eq(newArrayList("a=1", "b=2", "c=3")));
    }

    @Test
    public void innerLevelScalarCanBeEditedAndRequestIsSendToStackFrame() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc")));
        final RobotDebugVariable topLevelList = new RobotDebugVariable(frame, stackVar);
        final RobotDebugVariable innerScalar = topLevelList.getValue().getVariable("[1]");

        final String expression = "a  b  c";
        assertThat(innerScalar.verifyValue(expression)).isTrue();
        innerScalar.setValue(expression);

        assertThat(innerScalar.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariableInnerValue(same(stackVar),
                eq(newArrayList(newArrayList("list", 1), newArrayList("scalar", null))), eq(newArrayList("a  b  c")));
    }

    @Test
    public void innerLevelListCanBeEditedAndRequestIsSendToStackFrame_1() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("list",
                        newArrayList(new VariableTypedValue("str", "x"), new VariableTypedValue("str", "y")))));
        final RobotDebugVariable topLevelList = new RobotDebugVariable(frame, stackVar);
        final RobotDebugVariable innerListList = topLevelList.getValue().getVariable("[1]");

        final String expression = "a  b  c";
        assertThat(innerListList.verifyValue(expression)).isTrue();
        innerListList.setValue(expression);

        assertThat(innerListList.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariableInnerValue(same(stackVar),
                eq(newArrayList(newArrayList("list", 1), newArrayList("list", null))), eq(newArrayList("a", "b", "c")));
    }

    @Test
    public void innerLevelListCanBeEditedAndRequestIsSendToStackFrame_2() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("list",
                        newArrayList(new VariableTypedValue("str", "x"), new VariableTypedValue("str", "y")))));
        final RobotDebugVariable topLevelList = new RobotDebugVariable(frame, stackVar);
        final RobotDebugVariable innerListList = topLevelList.getValue().getVariable("[1]");

        final String expression = "[a,  b,  c]";
        assertThat(innerListList.verifyValue(expression)).isTrue();
        innerListList.setValue(expression);

        assertThat(innerListList.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariableInnerValue(same(stackVar),
                eq(newArrayList(newArrayList("list", 1), newArrayList("list", null))), eq(newArrayList("a", "b", "c")));
    }

    @Test
    public void innerLevelDictionaryCanBeEditedAndRequestIsSendToStackFrame_1() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                ImmutableMap.of("f", new VariableTypedValue("int", 1), "g", new VariableTypedValue("dict", ImmutableMap
                        .of("a", new VariableTypedValue("str", "x"), "b", new VariableTypedValue("str", "y")))));
        final RobotDebugVariable topLevelDict = new RobotDebugVariable(frame, stackVar);
        final RobotDebugVariable innerListDict = topLevelDict.getValue().getVariable("g");

        final String expression = "a  b  c";
        assertThat(innerListDict.verifyValue(expression)).isTrue();
        innerListDict.setValue(expression);

        assertThat(innerListDict.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariableInnerValue(same(stackVar),
                eq(newArrayList(newArrayList("dict", "g"), newArrayList("dict", null))),
                eq(newArrayList("a", "b", "c")));
    }

    @Test
    public void innerLevelDictionaryCanBeEditedAndRequestIsSendToStackFrame_2() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final StackFrameVariable stackVar = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                ImmutableMap.of("f", new VariableTypedValue("int", 1), "g", new VariableTypedValue("dict", ImmutableMap
                        .of("a", new VariableTypedValue("str", "x"), "b", new VariableTypedValue("str", "y")))));
        final RobotDebugVariable topLevelDict = new RobotDebugVariable(frame, stackVar);
        final RobotDebugVariable innerListDict = topLevelDict.getValue().getVariable("g");

        final String expression = "{a,  b,  c}";
        assertThat(innerListDict.verifyValue(expression)).isTrue();
        innerListDict.setValue(expression);

        assertThat(innerListDict.hasValueChanged()).isTrue();
        verify(frame).getDebugTarget();
        verify(frame).changeVariableInnerValue(same(stackVar),
                eq(newArrayList(newArrayList("dict", "g"), newArrayList("dict", null))),
                eq(newArrayList("a", "b", "c")));
    }

    @Test
    public void topLevelVariablesEqualityTest() {
        final RobotStackFrame frame = mock(RobotStackFrame.class);

        final RobotDebugVariable topLevelScalar1 = new RobotDebugVariable(frame,
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10));
        final RobotDebugVariable topLevelScalar2 = new RobotDebugVariable(frame,
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10));

        final RobotDebugVariable topLevelList1 = new RobotDebugVariable(frame,
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                        newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc"))));
        final RobotDebugVariable topLevelList2 = new RobotDebugVariable(frame,
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "list",
                        newArrayList(new VariableTypedValue("int", 1), new VariableTypedValue("str", "abc"))));

        final RobotDebugVariable topLevelDict1 = new RobotDebugVariable(frame, new StackFrameVariable(
                VariableScope.TEST_CASE, false, "var", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc"))));
        final RobotDebugVariable topLevelDict2 = new RobotDebugVariable(frame, new StackFrameVariable(
                VariableScope.TEST_CASE, false, "var", "dict",
                ImmutableMap.of("k1", new VariableTypedValue("int", 1), "k2", new VariableTypedValue("str", "abc"))));

        assertThat(topLevelScalar1.equals(topLevelScalar2)).isTrue();
        assertThat(topLevelList1.equals(topLevelList2)).isTrue();
        assertThat(topLevelDict1.equals(topLevelDict2)).isTrue();

        assertThat(topLevelScalar1.hashCode()).isEqualTo(topLevelScalar2.hashCode());
        assertThat(topLevelList1.hashCode()).isEqualTo(topLevelList2.hashCode());
        assertThat(topLevelDict1.hashCode()).isEqualTo(topLevelDict2.hashCode());
    }

    @Test
    public void innerVariablesEqualityTest() {
        final RobotDebugVariable parent = mock(RobotDebugVariable.class);
        final RobotDebugVariable var1 = new RobotDebugVariable(parent, "var", "int", 100);
        final RobotDebugVariable var2 = new RobotDebugVariable(parent, "var", "int", 100);

        assertThat(var1.equals(var2)).isTrue();
        assertThat(var1.hashCode()).isEqualTo(var2.hashCode());
    }

    @Test
    public void topLevelVariablesNonEqualityTest() {
        final RobotDebugVariable var1 = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10));
        final RobotDebugVariable var2 = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.LOCAL, false, "var", "int", 10));
        final RobotDebugVariable var3 = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.GLOBAL, true, "var", "int", 10));
        final RobotDebugVariable var4 = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.GLOBAL, false, "var1", "int", 10));
        final RobotDebugVariable var5 = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "str", 10));
        final RobotDebugVariable var6 = new RobotDebugVariable(mock(RobotStackFrame.class),
                new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 20));

        assertThat(var1.equals(var2)).isFalse();
        assertThat(var1.equals(var3)).isFalse();
        assertThat(var1.equals(var4)).isFalse();
        assertThat(var1.equals(var5)).isFalse();
        assertThat(var1.equals(var6)).isFalse();
    }

    @Test
    public void innerVariablesNonEqualityTest() {
        final RobotDebugVariable parent = mock(RobotDebugVariable.class);
        final RobotDebugVariable var1 = new RobotDebugVariable(parent, "var", "int", 100);
        final RobotDebugVariable var2 = new RobotDebugVariable(mock(RobotDebugVariable.class), "var", "int", 100);
        final RobotDebugVariable var3 = new RobotDebugVariable(parent, "var1", "int", 100);

        assertThat(var1.equals(var2)).isFalse();
        assertThat(var1.equals(var3)).isFalse();
    }
}
