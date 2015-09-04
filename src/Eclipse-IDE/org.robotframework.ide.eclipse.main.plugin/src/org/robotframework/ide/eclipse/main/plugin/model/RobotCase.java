package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.collect.Lists;

public class RobotCase extends RobotCodeHoldingElement {

    public static final String DOCUMENTATION = "Documentation";
    public static final String SETUP = "Setup";
    public static final String TEARDOWN = "Teardown";
    public static final String TIMEOUT = "Timeout";
    public static final String TEMPLATE = "Template";
    public static final String TAGS = "Tags";

    RobotCase(final RobotCasesSection parent, final String name, final String comment) {
        super(parent, name, comment);
    }

    public void link(final TestCase testCase) {
        for (final RobotExecutableRow execRow : testCase.getTestExecutionRows()) {
            final String callName = execRow.getAction().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(execRow.getArguments(), TokenFunctions.tokenToString()));
            createKeywordCall(callName, args, "");
        }
    }

    @Override
    public RobotCasesSection getParent() {
        return (RobotCasesSection) super.getParent();
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getTestCaseImage();
    }

    public boolean hasDocumentation() {
        return getDocumentationSetting() != null;
    }

    public RobotDefinitionSetting getDocumentationSetting() {
        return findSetting(DOCUMENTATION);
    }

    public boolean hasSetup() {
        return getSetupSetting() != null;
    }

    public RobotDefinitionSetting getSetupSetting() {
        return findSetting(SETUP);
    }

    public boolean hasTeardownValue() {
        return getTeardownSetting() != null;
    }

    public RobotDefinitionSetting getTeardownSetting() {
        return findSetting(TEARDOWN);
    }

    public boolean hasTimeoutValue() {
        return getTimeoutSetting() != null;
    }

    public RobotDefinitionSetting getTimeoutSetting() {
        return findSetting(TIMEOUT);
    }

    public boolean hasTemplate() {
        return getTemplateSetting() != null;
    }

    public RobotDefinitionSetting getTemplateSetting() {
        return findSetting(TEMPLATE);
    }

    public boolean hasTags() {
        return getTagsSetting() != null;
    }

    public RobotDefinitionSetting getTagsSetting() {
        return findSetting(TAGS);
    }

    private RobotDefinitionSetting findSetting(final String name) {
        for (final RobotKeywordCall call : getChildren()) {
            if (call instanceof RobotDefinitionSetting && call.getName().equals(name)) {
                return (RobotDefinitionSetting) call;
            }
        }
        return null;
    }
}
