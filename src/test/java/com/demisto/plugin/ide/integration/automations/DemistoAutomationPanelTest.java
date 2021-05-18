package com.demisto.plugin.ide.integration.automations;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.automations.ui.AutomationSettingsPanel;
import com.demisto.plugin.ide.unit.automations.DemistoAutomationYMLTest;
import com.intellij.openapi.project.Project;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.annotation.RunsInEDT;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.assertj.swing.core.MouseButton.LEFT_BUTTON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunsInEDT
@Category(GUITest.class)
public class DemistoAutomationPanelTest extends AssertJSwingJUnitTestCase {
    private FrameFixture frame;
    private DemistoAutomationYML automationYML;
    @Mock
    private Project mockProject;
    private String ymlFilePath = DemistoUtils.renameFileExtension(DemistoAutomationYMLTest.TEST_YML_WITHOUT_ARGS_AND_OUTPUTS_PATH, "test.yml");

    private AutomationSettingsPanel demistoAutomationPanelUnderTest;

    @Override
    protected void onSetUp() {
        String ymlString = DemistoUtils.readFile(DemistoAutomationYMLTest.TEST_YML_WITHOUT_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        Map automationYMLMap = yaml.load(ymlString);
        automationYML = new DemistoAutomationYML(automationYMLMap);
        demistoAutomationPanelUnderTest = GuiActionRunner.execute(() -> new AutomationSettingsPanel(automationYML, mockProject, ymlFilePath));
        frame = Containers.showInFrame(robot(), demistoAutomationPanelUnderTest);
        frame.show(); // shows the frame to test
    }

    @Test
    public void assertExistenceAndCorrectContent() {
        // basic settings panel

        String initialNameTextField = "testYML_with_args_and_outputs";
        String initialDescriptionTextField = "test yaml";
        String initialTagsTextField = "infra,server";

        // check that the items exist
        assertNotNull(frame.textBox("nameTextField"));
        assertNotNull(frame.textBox("descriptionTextField"));
        assertNotNull(frame.textBox("tagsTextField"));

        // validate correct initial content
        assertEquals(initialNameTextField, frame.textBox("nameTextField").text());
        assertEquals(initialDescriptionTextField, frame.textBox("descriptionTextField").text());
        assertEquals(initialTagsTextField, frame.textBox("tagsTextField").text());

        // validate that we have new argument button
        assertNotNull(frame.button("newArgumentButton"));

        // validate that we have new output button
        assertNotNull(frame.button("newOutputButton"));

        // validate that we have save button
        assertNotNull(frame.button("saveButton"));
    }

    @Test
    public void addArgumentAndValidateFields() {
        // arguments panel

        String initialTitleTextField = "testAb";
        String initialDescriptionText = "test argument description";
        String initialInitialValue = "";
        String initialListOptions = "";

        // add new argument

        // check that the items exist and have correct content
        JButtonFixture button = frame.button("newArgumentButton");
        button.click(LEFT_BUTTON);
        assertNotNull(frame.textBox("titleTextField"));
        frame.textBox("titleTextField").requireText("");
        assertNotNull(frame.textBox("descriptionText"));
        frame.textBox("descriptionText").requireText("");
        assertNotNull(frame.textBox("initialValueText"));
        frame.textBox("initialValueText").requireText("");
        assertNotNull(frame.textBox("listOptionsText"));
        frame.textBox("listOptionsText").requireText("");
        assertNotNull(frame.checkBox("isMandatoryCheckBox"));
        frame.checkBox("isMandatoryCheckBox").requireNotSelected();
        assertNotNull(frame.checkBox("isDefaultCheckBox"));
        frame.checkBox("isDefaultCheckBox").requireNotSelected();
        assertNotNull(frame.checkBox("isSensitiveCheckBox"));
        frame.checkBox("isSensitiveCheckBox").requireNotSelected();
        assertNotNull(frame.checkBox("isArrayCheckBox"));
        frame.checkBox("isArrayCheckBox").requireNotSelected();
    }
}
