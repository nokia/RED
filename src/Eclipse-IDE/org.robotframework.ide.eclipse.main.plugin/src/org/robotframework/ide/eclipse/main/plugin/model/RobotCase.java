/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseSetup;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTags;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTeardown;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTemplate;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTimeout;
import org.robotframework.ide.core.testData.model.table.testCases.TestDocumentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

import com.google.common.collect.Lists;

public class RobotCase extends RobotCodeHoldingElement {

    public static final String DOCUMENTATION = "Documentation";
    public static final String SETUP = "Setup";
    public static final String TEARDOWN = "Teardown";
    public static final String TIMEOUT = "Timeout";
    public static final String TEMPLATE = "Template";
    public static final String TAGS = "Tags";

    private TestCase testCase;

    RobotCase(final RobotCasesSection parent, final String name, final String comment) {
        super(parent, name, comment);
    }

    public void link(final TestCase testCase) {
        this.testCase = testCase;

        for (final RobotExecutableRow<TestCase> execRow : testCase.getTestExecutionRows()) {
            final String callName = execRow.getAction().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(execRow.getArguments(), TokenFunctions.tokenToString()));
            createKeywordCall(callName, args, "");
        }
        // settings
        for (final TestDocumentation documentation : testCase.getDocumentation()) {
            final String name = documentation.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(documentation.getDocumentationText(), TokenFunctions.tokenToString()));
            createDefinitionSetting(omitSquareBrackets(name), args, "");
        }
        for (final TestCaseTags tags : testCase.getTags()) {
            final String name = tags.getDeclaration().getText().toString();
            final List<String> args = newArrayList(Lists.transform(tags.getTags(), TokenFunctions.tokenToString()));
            createDefinitionSetting(omitSquareBrackets(name), args, "");
        }
        for (final TestCaseSetup setup : testCase.getSetups()) {
            final String name = setup.getDeclaration().getText().toString();
            final List<String> args = newArrayList(setup.getKeywordName().getText().toString());
            args.addAll(Lists.transform(setup.getArguments(), TokenFunctions.tokenToString()));
            createDefinitionSetting(omitSquareBrackets(name), args, "");
        }
        for (final TestCaseTemplate template : testCase.getTemplates()) {
            final String name = template.getDeclaration().getText().toString();
            final List<String> args = newArrayList(template.getKeywordName().getText().toString());
            createDefinitionSetting(omitSquareBrackets(name), args, "");
        }
        for (final TestCaseTimeout timeout : testCase.getTimeouts()) {
            final String name = timeout.getDeclaration().getText().toString();
            final List<String> args = newArrayList(timeout.getTimeout().getText().toString());
            args.addAll(Lists.transform(timeout.getMessage(), TokenFunctions.tokenToString()));
            createDefinitionSetting(omitSquareBrackets(name), args, "");
        }
        for (final TestCaseTeardown teardown : testCase.getTeardowns()) {
            final String name = teardown.getDeclaration().getText().toString();
            final List<String> args = newArrayList(teardown.getKeywordName().getText().toString());
            args.addAll(Lists.transform(teardown.getArguments(), TokenFunctions.tokenToString()));
            createDefinitionSetting(omitSquareBrackets(name), args, "");
        }
    }

    private static String omitSquareBrackets(final String nameInBrackets) {
        return nameInBrackets.substring(1, nameInBrackets.length() - 1);
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

    @Override
    public Position getPosition() {
        final FilePosition begin = testCase.getBeginPosition();
        final FilePosition end = testCase.getEndPosition();

        return new Position(begin.getOffset(), end.getOffset() - begin.getOffset() + 1);
    }

    public Position getDefinitionPosition() {
        final int begin = testCase.getTestName().getStartOffset();
        final int length = testCase.getTestName().getText().length();

        return new Position(begin, length);
    }
}
