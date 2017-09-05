package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

import com.google.common.collect.ImmutableMap;

public class VariablesEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        VariablesEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("variables", new Object());
        VariablesEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("variables", newArrayList());
        VariablesEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("variables", newArrayList("foo"));
        VariablesEvent.from(eventMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("variables", newArrayList(ImmutableMap.of()));
        VariablesEvent.from(eventMap);
    }

    @Test
    public void eventIsProperlyConstructed() {
        final Map<String, Object> scope1 = new LinkedHashMap<>();
        scope1.put("${a}", newArrayList("int", 1, "global"));
        scope1.put("@{b}", newArrayList("list", newArrayList(newArrayList("int", 4), newArrayList("int", 2)), "suite"));
        scope1.put("&{c}", newArrayList("dict",
                ImmutableMap.of("a", newArrayList("int", 4), "b", newArrayList("int", 2)), "local"));
        final List<Map<String, Object>> scopes = newArrayList(scope1);

        final Map<String, Object> eventMap = ImmutableMap.of("variables", newArrayList(
                ImmutableMap.of("var_scopes", scopes, "error", "err")));
        final VariablesEvent event = VariablesEvent.from(eventMap);

        final List<Map<Variable, VariableTypedValue>> expectedVars = newArrayList(
                ImmutableMap.of(
                    new Variable("${a}", VariableScope.GLOBAL),
                        new VariableTypedValue("int", 1),
                    new Variable("@{b}", VariableScope.TEST_SUITE),
                        new VariableTypedValue("list",
                                newArrayList(new VariableTypedValue("int", 4), new VariableTypedValue("int", 2))),
                    new Variable("&{c}", VariableScope.LOCAL),
                        new VariableTypedValue("dict", ImmutableMap.of("a", new VariableTypedValue("int", 4), "b",
                                new VariableTypedValue("int", 2)))));

        assertThat(event.hasError()).isTrue();
        assertThat(event.getError()).contains("err");
        assertThat(event.getVariables()).isEqualTo(expectedVars);
    }

    @Test
    public void eventIsProperlyConstructed_whenNoErrorIsProvided() {
        final Map<String, Object> scope1 = new LinkedHashMap<>();
        scope1.put("${a}", newArrayList("int", 1, "global"));
        scope1.put("@{b}", newArrayList("list", newArrayList(newArrayList("int", 4), newArrayList("int", 2)), "suite"));
        scope1.put("&{c}", newArrayList("dict",
                ImmutableMap.of("a", newArrayList("int", 4), "b", newArrayList("int", 2)), "local"));
        final List<Map<String, Object>> scopes = newArrayList(scope1);

        final Map<String, Object> eventMap = ImmutableMap.of("variables", newArrayList(
                ImmutableMap.of("var_scopes", scopes)));
        final VariablesEvent event = VariablesEvent.from(eventMap);

        final List<Map<Variable, VariableTypedValue>> expectedVars = newArrayList(
                ImmutableMap.of(
                    new Variable("${a}", VariableScope.GLOBAL),
                        new VariableTypedValue("int", 1),
                    new Variable("@{b}", VariableScope.TEST_SUITE),
                        new VariableTypedValue("list",
                                newArrayList(new VariableTypedValue("int", 4), new VariableTypedValue("int", 2))),
                    new Variable("&{c}", VariableScope.LOCAL),
                        new VariableTypedValue("dict", ImmutableMap.of("a", new VariableTypedValue("int", 4), "b",
                                new VariableTypedValue("int", 2)))));

        assertThat(event.hasError()).isFalse();
        assertThat(event.getError()).isEmpty();
        assertThat(event.getVariables()).isEqualTo(expectedVars);
    }

    @Test
    public void equalsTests() {
        final List<Map<Variable, VariableTypedValue>> vars1 = newArrayList(
                ImmutableMap.of(
                    new Variable("${a}", VariableScope.GLOBAL),
                        new VariableTypedValue("int", 1),
                    new Variable("@{b}", VariableScope.TEST_SUITE),
                        new VariableTypedValue("list", newArrayList(4, 2)),
                    new Variable("&{c}", VariableScope.LOCAL),
                        new VariableTypedValue("dict", ImmutableMap.of("a", 4, "b", 2))));
        final List<Map<Variable, VariableTypedValue>> vars2 = newArrayList(
                ImmutableMap.of(
                    new Variable("${a}", VariableScope.GLOBAL),
                        new VariableTypedValue("int", 1),
                    new Variable("@{b}", VariableScope.TEST_SUITE),
                        new VariableTypedValue("list", newArrayList(4, 2)),
                    new Variable("&{c}", VariableScope.LOCAL),
                        new VariableTypedValue("dict", ImmutableMap.of("a", 4, "b", 2))));
        final List<Map<Variable, VariableTypedValue>> vars3 = newArrayList(
                ImmutableMap.of(
                    new Variable("${a}", VariableScope.GLOBAL),
                        new VariableTypedValue("int", 1),
                    new Variable("@{b}", VariableScope.TEST_SUITE),
                        new VariableTypedValue("list", newArrayList(4, 2)),
                    new Variable("&{c}", VariableScope.LOCAL),
                        new VariableTypedValue("dict", ImmutableMap.of("a", 4, "b", 5))));
        
        assertThat(new VariablesEvent(vars1, "error")).isEqualTo(new VariablesEvent(vars2, "error2"));
        assertThat(new VariablesEvent(vars1, null)).isEqualTo(new VariablesEvent(vars2, null));

        assertThat(new VariablesEvent(vars1, "error")).isNotEqualTo(new VariablesEvent(vars3, "error"));
        assertThat(new VariablesEvent(vars3, "error")).isNotEqualTo(new VariablesEvent(vars1, "error"));
        assertThat(new VariablesEvent(vars1, "error")).isNotEqualTo(new Object());
        assertThat(new VariablesEvent(vars1, "error")).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        final List<Map<Variable, VariableTypedValue>> vars1 = newArrayList(
                ImmutableMap.of(
                    new Variable("${a}", VariableScope.GLOBAL),
                        new VariableTypedValue("int", 1),
                    new Variable("@{b}", VariableScope.TEST_SUITE),
                        new VariableTypedValue("list", newArrayList(4, 2)),
                    new Variable("&{c}", VariableScope.LOCAL),
                        new VariableTypedValue("dict", ImmutableMap.of("a", 4, "b", 2))));
        final List<Map<Variable, VariableTypedValue>> vars2 = newArrayList(
                ImmutableMap.of(
                    new Variable("${a}", VariableScope.GLOBAL),
                        new VariableTypedValue("int", 1),
                    new Variable("@{b}", VariableScope.TEST_SUITE),
                        new VariableTypedValue("list", newArrayList(4, 2)),
                    new Variable("&{c}", VariableScope.LOCAL),
                        new VariableTypedValue("dict", ImmutableMap.of("a", 4, "b", 2))));

        assertThat(new VariablesEvent(vars1, "error").hashCode())
                .isEqualTo(new VariablesEvent(vars2, "error").hashCode());
    }
}
