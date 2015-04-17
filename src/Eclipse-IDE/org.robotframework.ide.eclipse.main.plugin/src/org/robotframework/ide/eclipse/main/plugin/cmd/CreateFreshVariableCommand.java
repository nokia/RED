package org.robotframework.ide.eclipse.main.plugin.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable.Type;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

public class CreateFreshVariableCommand extends EditorCommand {

    private final RobotSuiteFileSection variablesSection;
    private final int index;
    private final boolean notifySync;

    public CreateFreshVariableCommand(final RobotSuiteFileSection variablesSection, final boolean notifySynchronously) {
        this(variablesSection, -1, notifySynchronously);
    }

    public CreateFreshVariableCommand(final RobotSuiteFileSection variablesSection, final int index) {
        this(variablesSection, index, false);
    }

    private CreateFreshVariableCommand(final RobotSuiteFileSection variablesSection, final int index,
            final boolean notifySynchronously) {
        this.variablesSection = variablesSection;
        this.index = index;
        this.notifySync = notifySynchronously;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final String name = generateUniqueName();

        final RobotVariable variable = new RobotVariable(variablesSection, Type.SCALAR, name, "", "");

        if (index == -1) {
            variablesSection.getChildren().add(variable);
        } else {
            variablesSection.getChildren().add(index, variable);
        }

        if (notifySync) {
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
        } else {
            eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
        }
    }

    private String generateUniqueName() {
        final int maxNumber = getCurrentMaxNumberOfVars();
        return "var" + (maxNumber >= 0 ? maxNumber + 1 : "");
    }

    private int getCurrentMaxNumberOfVars() {
        final Collection<String> currentNames = Collections2.transform(
                newArrayList(Iterables.filter(variablesSection.getChildren(), RobotVariable.class)),
                new Function<RobotVariable, String>() {
                    @Override
                    public String apply(final RobotVariable var) {
                        return var.getName();
                    }
                });
        final Collection<Integer> numbers = Collections2.transform(currentNames, new Function<String, Integer>() {
            @Override
            public Integer apply(final String name) {
                if (name.startsWith("var")) {
                    final Integer num = Ints.tryParse(name.substring(3));
                    return num == null ? 0 : num;
                }
                return -1;
            }
        });
        return numbers.isEmpty() ? -1 : Collections.max(numbers);
    }
}
