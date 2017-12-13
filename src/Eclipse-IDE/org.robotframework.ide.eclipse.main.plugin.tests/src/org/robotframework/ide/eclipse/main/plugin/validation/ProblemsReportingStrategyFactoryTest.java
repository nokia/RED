package org.robotframework.ide.eclipse.main.plugin.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.validation.ProblemsReportingStrategyFactory.HeadlessValidationReportingStrategy;

public class ProblemsReportingStrategyFactoryTest {

    @Test
    public void properNormalStrategyIsCreated() {
        final HeadlessValidationReportingStrategy strategy = ProblemsReportingStrategyFactory.checkstyleReporter("filepath", mock(Logger.class));

        assertThat(strategy).isInstanceOf(CheckstyleReportingStrategy.class);
        assertThat(strategy).extracting("shouldPanic").contains(false);
    }

    @Test
    public void properPanickingStrategyIsCreated() {
        final HeadlessValidationReportingStrategy strategy = ProblemsReportingStrategyFactory
                .checkstylePanicReporter("filepath", mock(Logger.class));

        assertThat(strategy).isInstanceOf(CheckstyleReportingStrategy.class);
        assertThat(strategy).extracting("shouldPanic").contains(true);
    }

}
