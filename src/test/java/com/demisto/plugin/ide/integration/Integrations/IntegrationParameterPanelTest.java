package com.demisto.plugin.ide.integration.Integrations;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import com.demisto.plugin.ide.integrations.ui.IntegrationParameterPanel;
import com.intellij.openapi.project.Project;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.annotation.RunsInEDT;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.Containers;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static com.demisto.plugin.ide.integrations.ui.IntegrationParameterPanel.YML_CHANGE_INTEGRATION_PARAMETER_FIELD;
import static com.demisto.plugin.ide.unit.integrations.DemistoIntegrationYMLTest.TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
/**
 * @author  Shachar Hirshberg
 * @created January 2, 2019
 */
@RunsInEDT
@Category(GUITest.class)
public class IntegrationParameterPanelTest extends AssertJSwingJUnitTestCase {
    private FrameFixture frame;
    private DemistoIntegrationYML integrationYML;
    private IntegrationParameterPanel integrationParameterPanelUnderTest;
    @Mock
    protected static Project project;

    @Override
    protected void onSetUp() {
        String ymlString = DemistoUtils.readFile(TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        Map automationYMLMap = yaml.load(ymlString);
        integrationYML = new DemistoIntegrationYML(automationYMLMap);
        integrationParameterPanelUnderTest = GuiActionRunner.execute(() -> new IntegrationParameterPanel(integrationYML.getConfiguration().get(0), project));
        frame = Containers.showInFrame(robot(), integrationParameterPanelUnderTest);
        frame.show(); // shows the frame to test
    }

    @Test
    public void assertExistenceAndCorrectContent() {
        // setup
        String initialNameTextField = "APIKey";
        String initialDisplayTextField = "Virus Total private API key";
        String emptyString = "";
        String initialType = "Encrypted";

        // check that the items exist
        assertNotNull(frame.textBox("nameTextArea"));
        assertNotNull(frame.textBox("displayTextArea"));
        assertNotNull(frame.textBox("initialValueTextArea"));
        assertNotNull(frame.textBox("additionalinfoTextArea"));
        assertNotNull(frame.comboBox("typeDropdown"));
        assertNotNull(frame.checkBox("isMandatoryCheckbox"));
        assertNotNull(frame.button("deleteParameterButton"));

        // validate correct initial content
        assertEquals(initialNameTextField, frame.textBox("nameTextArea").text());
        assertEquals(initialDisplayTextField, frame.textBox("displayTextArea").text());
        assertEquals(emptyString, frame.textBox("initialValueTextArea").text());
        assertEquals(emptyString, frame.textBox("additionalinfoTextArea").text());
        assertEquals(initialType, frame.comboBox("typeDropdown").selectedItem());
        frame.checkBox("isMandatoryCheckbox").requireSelected();

    }

    @Test
    public void checkTextAndSelectionChanges() {
        // setup
        String nameTextField = "testName";
        String descriptionTextField = "test description";
        String type = "Multi Select";
        String emptyString = "";

        // set and validate new content
        frame.textBox("nameTextArea").setText(nameTextField).requireText(nameTextField);
        frame.textBox("displayTextArea").setText(descriptionTextField).requireText(descriptionTextField);
        assertEquals(type,frame.comboBox("typeDropdown").selectItem(type).selectedItem());
        frame.checkBox("isMandatoryCheckbox").click().requireNotSelected();
        assertNotNull(frame.textBox("optionsTextfield"));
        assertEquals(emptyString, frame.textBox("optionsTextfield").text());
    }

    @Test
    public void checkForPropertyChanges() {
        // setup
        String nameTextField = "testName";
        String descriptionTextField = "test description";
        String type = "Multi Select";
        final int[] propertyChangeCounter = {0};

        JTextComponentFixture titleTextComponent = frame.textBox("nameTextArea");
        JTextComponentFixture descriptionTextComponent = frame.textBox("displayTextArea");

        integrationParameterPanelUnderTest.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(YML_CHANGE_INTEGRATION_PARAMETER_FIELD)) {
                propertyChangeCounter[0] += 1;
            }
        });

        // set new content
        titleTextComponent.setText(nameTextField);
        descriptionTextComponent.setText(descriptionTextField);
        frame.comboBox("typeDropdown").selectItem(type);
        // validate that property changes were fired
        assertEquals(5, propertyChangeCounter[0]);
    }
}
