package com.demisto.plugin.ide.integration.automations;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.automations.ui.AutomationArgumentPanel;
import com.demisto.plugin.ide.unit.automations.DemistoAutomationYMLTest;
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

import static com.demisto.plugin.ide.automations.ui.AutomationArgumentPanel.YML_CHANGE_AUTOMATION_ARG_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunsInEDT
@Category(GUITest.class)
public class AutomationArgumentPanelTest extends AssertJSwingJUnitTestCase {
    private FrameFixture frame;
    private DemistoAutomationYML.DemistoArgument automationArgumentUnderTest;
    private DemistoAutomationYML automationYML;
    private AutomationArgumentPanel argsPanel;
    @Mock
    protected static Project project;

    @Override
    protected void onSetUp() {
        String ymlString = DemistoUtils.readFile(DemistoAutomationYMLTest.TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        Map automationYMLMap = yaml.load(ymlString);
        automationYML = new DemistoAutomationYML(automationYMLMap);
        automationArgumentUnderTest = automationYML.getArgs().get(0);
        argsPanel = GuiActionRunner.execute(() -> new AutomationArgumentPanel(automationArgumentUnderTest, project, ""));
        frame = Containers.showInFrame(robot(), argsPanel);
        frame.show(); // shows the frame to test
    }

    @Test
    public void assertExistenceAndCorrectContent() {
        // setup
        String initialTitleTextField = "testAb";
        String initialDescriptionText = "test argument description";
        String initialInitialValue = "";
        String initialListOptions = "";

        // check that the items exist
        assertNotNull(frame.textBox("titleTextField"));
        assertNotNull(frame.textBox("descriptionText"));
        assertNotNull(frame.textBox("initialValueText"));
        assertNotNull(frame.textBox("listOptionsText"));
        assertNotNull(frame.checkBox("isMandatoryCheckBox"));
        assertNotNull(frame.checkBox("isDefaultCheckBox"));
        assertNotNull(frame.checkBox("isSensitiveCheckBox"));
        assertNotNull(frame.checkBox("isArrayCheckBox"));

        // validate correct initial content
        assertEquals(initialTitleTextField, frame.textBox("titleTextField").text());
        assertEquals(initialDescriptionText, frame.textBox("descriptionText").text());
        assertEquals(initialInitialValue, frame.textBox("initialValueText").text());
        assertEquals(initialListOptions, frame.textBox("listOptionsText").text());
        frame.checkBox("isMandatoryCheckBox").requireSelected();
        frame.checkBox("isDefaultCheckBox").requireNotSelected();
        frame.checkBox("isSensitiveCheckBox").requireNotSelected();
        frame.checkBox("isArrayCheckBox").requireNotSelected();
    }

    @Test
    public void checkTextAndSelectionChanges() {
        // setup
        String titleTextField = "new title";
        String descriptionText = "new description";
        String initialValue = "test value";
        String initialListOptions = "a,b";

        // set and validate new content
//        frame.textBox("titleTextField").setText(titleTextField).requireText(titleTextField);
        frame.textBox("descriptionText").setText(descriptionText).requireText(descriptionText);
        frame.textBox("initialValueText").setText(initialValue).requireText(initialValue);
        frame.textBox("listOptionsText").setText(initialListOptions).requireText(initialListOptions);
        frame.checkBox("isMandatoryCheckBox").click().requireNotSelected();
        frame.checkBox("isDefaultCheckBox").check().requireSelected();
        frame.checkBox("isSensitiveCheckBox").check().requireSelected();
        frame.checkBox("isArrayCheckBox").check().requireSelected();
    }

    @Test
    public void checkForPropertyChanges() {
        // setup
        String titleTextField = "new title";
        String descriptionText = "new description";
        String initialValue = "test value";
        String initialListOptions = "a,b,c";
        final int[] propertyChangeCounter = {0};

        JTextComponentFixture titleTextComponent = frame.textBox("titleTextField");
        JTextComponentFixture descriptionTextComponent = frame.textBox("descriptionText");
        JTextComponentFixture initialValueComponent = frame.textBox("initialValueText");
        JTextComponentFixture initialListComponent = frame.textBox("listOptionsText");

        argsPanel.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(YML_CHANGE_AUTOMATION_ARG_FIELD)) {
                propertyChangeCounter[0] += 1;
            }
        });

        // set new content
//        titleTextComponent.setText(titleTextField);
        descriptionTextComponent.setText(descriptionText);
        initialValueComponent.setText(initialValue);
        initialListComponent.setText(initialListOptions);
        frame.checkBox("isMandatoryCheckBox").click();
        frame.checkBox("isDefaultCheckBox").check();
        frame.checkBox("isSensitiveCheckBox").check();
        frame.checkBox("isArrayCheckBox").check();
        // validate that property changes were fired
        assertEquals(8, propertyChangeCounter[0]);
    }
}
