package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput.KeywordDefinitionOnSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.LibraryImportSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput.SuiteFileOnSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput.TestCaseOnSettingInput;
import org.robotframework.red.junit.ProjectProvider;


public class DocumentationsTest {

    @ClassRule
    public static final ProjectProvider projectProvider = new ProjectProvider(DocumentationsTest.class);

    @Test
    public void suiteFileInputIsFoundForRobotSuiteFile() throws Exception {
        final RobotSuiteFile suiteFile = new RobotSuiteFileCreator().build();

        final Optional<DocumentationViewInput> input = Documentations.findInput(suiteFile);
        assertThat(input).isPresent().containsInstanceOf(SuiteFileInput.class);
    }

    @Test
    public void suiteFileOnDocSettingInputIsFoundForSuiteDocumentationSetting() {
        final RobotSetting setting = new RobotSetting(null, new SuiteDocumentation(RobotToken.create("Documentation")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(SuiteFileOnSettingInput.class);
    }

    @Test
    public void libraryImportOnSettingInputIsFoundForLibraryImportSetting() {
        final LibraryImport importedLib = new LibraryImport(RobotToken.create("Library"));
        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES, importedLib);

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(LibraryImportSettingInput.class);
    }

    @Test
    public void suiteFileInputIsFoundForResourceSettingImport() throws Exception {
        projectProvider.createFile("res.robot");
        final IFile suiteFile = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource  res.robot");
        final RobotSuiteFile suite = RedPlugin.getModelManager().createSuiteFile(suiteFile);

        final RobotSetting setting = (RobotSetting) suite.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(SuiteFileInput.class);

        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void noInputIsFoundForResourceSettingImport_whenImportedFileDoesNotExist() throws Exception {
        final IFile suiteFile = projectProvider.createFile("suite.robot", "*** Settings ***",
                "Resource  non_existing.robot");
        final RobotSuiteFile suite = RedPlugin.getModelManager().createSuiteFile(suiteFile);

        final RobotSetting setting = (RobotSetting) suite.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);

        assertThat(Documentations.findInput(setting)).isEmpty();

        RedPlugin.getModelManager().dispose();

    }

    @Test
    public void keywordDefinitionInputIsFoundForRobotKeywordDefinition() {
        final RobotKeywordDefinition keyword = new RobotKeywordDefinition(null,
                new UserKeyword(RobotToken.create("keyword")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(keyword);
        assertThat(input).isPresent().containsInstanceOf(KeywordDefinitionInput.class);
    }

    @Test
    public void keywordOnDocSettingInputIsFoundForDocumentationSettingInRobotCase() {
        final RobotKeywordDefinition keyword = new RobotKeywordDefinition(null,
                new UserKeyword(RobotToken.create("keyword")));
        final RobotDefinitionSetting setting = new RobotDefinitionSetting(keyword,
                new KeywordDocumentation(RobotToken.create("[Documentation]")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(KeywordDefinitionOnSettingInput.class);
    }

    @Test
    public void noInputIsFoundOnSomeKeywordSettingOtherThanDocumentation() {
        final RobotKeywordDefinition keyword = new RobotKeywordDefinition(null,
                new UserKeyword(RobotToken.create("keyword")));
        final RobotDefinitionSetting setting = new RobotDefinitionSetting(keyword,
                new KeywordArguments(RobotToken.create("[Arguments]")));

        assertThat(Documentations.findInput(setting)).isEmpty();
    }

    @Test
    public void testCaseInputIsFoundForRobotCase() {
        final RobotCase testCase = new RobotCase(null, new TestCase(RobotToken.create("test")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(testCase);
        assertThat(input).isPresent().containsInstanceOf(TestCaseInput.class);
    }

    @Test
    public void testCaseOnDocSettingInputIsFoundForDocumentationSettingInRobotCase() {
        final RobotCase testCase = new RobotCase(null, new TestCase(RobotToken.create("test")));
        final RobotDefinitionSetting setting = new RobotDefinitionSetting(testCase,
                new TestDocumentation(RobotToken.create("[Documentation]")));
        
        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(TestCaseOnSettingInput.class);
    }

    @Test
    public void noInputIsFoundOnSomeTestSettingOtherThanDocumentation() {
        final RobotCase testCase = new RobotCase(null, new TestCase(RobotToken.create("test")));
        final RobotDefinitionSetting setting = new RobotDefinitionSetting(testCase,
                new TestTemplate(RobotToken.create("[Template]")));

        assertThat(Documentations.findInput(setting)).isEmpty();
    }

}
