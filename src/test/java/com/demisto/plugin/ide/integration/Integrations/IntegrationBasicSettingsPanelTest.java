package com.demisto.plugin.ide.integration.Integrations;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.demisto.plugin.ide.integrations.ui.IntegrationBasicSettingsPanel;
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

import static com.demisto.plugin.ide.integrations.ui.IntegrationBasicSettingsPanel.YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD;
import static com.demisto.plugin.ide.unit.integrations.DemistoIntegrationYMLTest.TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunsInEDT
@Category(GUITest.class)
public class IntegrationBasicSettingsPanelTest extends AssertJSwingJUnitTestCase {
    private FrameFixture frame;
    private DemistoIntegrationYML integrationYML;
    private IntegrationBasicSettingsPanel integrationBasicSettingsPanelUnderTest;

    @Override
    protected void onSetUp() {
        String ymlString = DemistoUtils.readFile(TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        Map automationYMLMap = yaml.load(ymlString);
        integrationYML = new DemistoIntegrationYML(automationYMLMap);
        integrationBasicSettingsPanelUnderTest = GuiActionRunner.execute(() -> new IntegrationBasicSettingsPanel(integrationYML));
        frame = Containers.showInFrame(robot(), integrationBasicSettingsPanelUnderTest);
        frame.show(); // shows the frame to test
    }

    @Test
    public void assertExistenceAndCorrectContent() {
        String initialNameTextField = "VirusTotal";
        String initialDescriptionTextField = "Analyze suspicious hashes, URLs, domains and IP addresses";
        String initialCategoryField = "Data Enrichment & Threat Intelligence";

        // check that the items exist
        assertNotNull(frame.textBox("nameTextField"));
        assertNotNull(frame.textBox("descriptionTextField"));
        assertNotNull(frame.comboBox("categoryDropdown"));
        assertNotNull(frame.checkBox("isFetchCheckBox"));

        // validate correct initial content
        assertEquals(initialNameTextField, frame.textBox("nameTextField").text());
        assertEquals(initialDescriptionTextField, frame.textBox("descriptionTextField").text());
        assertEquals(initialCategoryField, frame.comboBox("categoryDropdown").selectedItem());
        frame.checkBox("isFetchCheckBox").requireNotSelected();

    }

    @Test
    public void checkTextAndSelectionChanges() {
        // setup
        String nameTextField = "testName";
        String descriptionTextField = "test description";
        String category = "Messaging";

        // set and validate new content
        frame.textBox("nameTextField").setText(nameTextField).requireText(nameTextField);
        frame.textBox("descriptionTextField").setText(descriptionTextField).requireText(descriptionTextField);
        assertEquals(category,frame.comboBox("categoryDropdown").selectItem(category).selectedItem());
        frame.checkBox("isFetchCheckBox").click().requireSelected();
    }

    @Test
    public void checkForPropertyChanges() {
        // setup
        String nameTextField = "testName";
        String descriptionTextField = "test description";
        String category = "Messaging";
        final int[] propertyChangeCounter = {0};

        JTextComponentFixture titleTextComponent = frame.textBox("nameTextField");
        JTextComponentFixture descriptionTextComponent = frame.textBox("descriptionTextField");

        integrationBasicSettingsPanelUnderTest.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(YML_CHANGE_INTEGRATION_BASIC_SETTINGS_FIELD)) {
                propertyChangeCounter[0] += 1;
            }
        });

        // set new content
        titleTextComponent.setText(nameTextField);
        descriptionTextComponent.setText(descriptionTextField);
        frame.comboBox("categoryDropdown").selectItem(category);
        // validate that property changes were fired
        assertEquals(5, propertyChangeCounter[0]);
    }
}
