package org.robotframework.ide.eclipse.main.plugin.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

class NamesGenerator {

    static String generateUniqueName(final RobotElement parent, final String prefix) {
        final int maxNumber = getCurrentMaxNumber(parent, prefix);
        return prefix + (maxNumber >= 0 ? maxNumber + 1 : "");
    }

    private static int getCurrentMaxNumber(final RobotElement parent, final String prefix) {
        final Collection<String> currentNames = Collections2.transform(
                newArrayList(Iterables.filter(parent.getChildren(), RobotElement.class)),
                new Function<RobotElement, String>() {
                    @Override
                    public String apply(final RobotElement element) {
                        return element.getName();
                    }
                });
        final Collection<Integer> numbers = Collections2.transform(currentNames, new Function<String, Integer>() {
            @Override
            public Integer apply(final String name) {
                if (name.startsWith(prefix)) {
                    final Integer num = Ints.tryParse(name.substring(prefix.length()));
                    return num == null ? 0 : num;
                }
                return -1;
            }
        });
        return numbers.isEmpty() ? -1 : Collections.max(numbers);
    }
}
