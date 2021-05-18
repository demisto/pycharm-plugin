package com.demisto.plugin.ide.integration.automations;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.automations.ui.AutomationBasicSettingsPanel;
import com.demisto.plugin.ide.unit.automations.DemistoAutomationYMLTest;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.annotation.RunsInEDT;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static com.demisto.plugin.ide.automations.ui.AutomationBasicSettingsPanel.YML_CHANGE_AUTOMATION_BASIC_SETTINGS_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunsInEDT
@Category(GUITest.class)
public class AutomationBasicSettingsPanelTest extends AssertJSwingJUnitTestCase {
    private FrameFixture frame;
    private DemistoAutomationYML automationYML;
    private AutomationBasicSettingsPanel automationBasicSettingsPanelUnderTest;

    @Override
    protected void onSetUp() {
        String ymlString = DemistoUtils.readFile(DemistoAutomationYMLTest.TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        Map automationYMLMap = yaml.load(ymlString);
        automationYML = new DemistoAutomationYML(automationYMLMap);
        automationBasicSettingsPanelUnderTest = GuiActionRunner.execute(() -> new AutomationBasicSettingsPanel(automationYML));
        frame = Containers.showInFrame(robot(), automationBasicSettingsPanelUnderTest);
        frame.show(); // shows the frame to test
    }

    @Test
    public void assertExistenceAndCorrectContent() {
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
    }

    @Test
    public void checkTextAndSelectionChanges() {
        // setup
        String nameTextField = "testName";
        String descriptionTextField = "test description";
        String tagsTextField = "test,basic";

        // set and validate new content
        frame.textBox("nameTextField").setText(nameTextField).requireText(nameTextField);
        frame.textBox("descriptionTextField").setText(descriptionTextField).requireText(descriptionTextField);
        frame.textBox("tagsTextField").setText(tagsTextField).requireText(tagsTextField);
    }

    @Test
    public void checkForPropertyChanges() {
        // setup
        String nameTextField = "testName";
        String descriptionTextField = "test description";
        String tagsTextField = "test,basic";
        final int[] propertyChangeCounter = {0};

        JTextComponentFixture titleTextComponent = frame.textBox("nameTextField");
        JTextComponentFixture descriptionTextComponent = frame.textBox("descriptionTextField");
        JTextComponentFixture initialValueComponent = frame.textBox("tagsTextField");

        automationBasicSettingsPanelUnderTest.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(YML_CHANGE_AUTOMATION_BASIC_SETTINGS_FIELD)) {
                propertyChangeCounter[0] += 1;
            }
        });

        // set new content
        titleTextComponent.setText(nameTextField);
        descriptionTextComponent.setText(descriptionTextField);
        initialValueComponent.setText(tagsTextField);
        // validate that property changes were fired
        assertEquals(6, propertyChangeCounter[0]);
    }
}
