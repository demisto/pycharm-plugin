package com.demisto.plugin.ide.integration.automations;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.automations.ui.AutomationOutputPanel;
import com.demisto.plugin.ide.unit.automations.DemistoAutomationYMLTest;
import com.intellij.openapi.project.Project;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.annotation.RunsInEDT;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunsInEDT
@Category(GUITest.class)
public class AutomationOutputPanelTest extends AssertJSwingJUnitTestCase {
    private FrameFixture panel;
    private DemistoAutomationYML.DemistoOutput automationOutputUnderTest;
    private DemistoAutomationYML automationYML;
    private AutomationOutputPanel outputPanel;
    @Mock
    protected static Project project;

    @Override
    protected void onSetUp() {
        String ymlString = DemistoUtils.readFile(DemistoAutomationYMLTest.TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        Map automationYMLMap = yaml.load(ymlString);
        automationYML = new DemistoAutomationYML(automationYMLMap);
        automationOutputUnderTest = automationYML.getOutputs().get(0);
        JFrame frame = GuiActionRunner.execute(() -> new JFrame("testFrame"));
        outputPanel = GuiActionRunner.execute(() -> new AutomationOutputPanel(automationOutputUnderTest, project, ""));
        frame.add(outputPanel);

        panel = new FrameFixture(robot(), frame);

        panel.show(); // shows the frame to test
    }

    @Test
    public void assertExistenceAndCorrectContent() {
        String initialContextPath = "firstContext";
        String initialDescriptionText = "test description";
        String initialType = "String";

        // check that the items exist
        assertNotNull(panel.textBox("contextPathTextField"));
        assertNotNull(panel.textBox("descriptionText"));
        assertNotNull(panel.comboBox("typeOptionsDropdown"));

        // validate correct initial content
        assertEquals(initialContextPath, panel.textBox("contextPathTextField").text());
        assertEquals(initialDescriptionText, panel.textBox("descriptionText").text());
        assertEquals(initialType, panel.comboBox("typeOptionsDropdown").selectedItem());
    }

    @Test
    public void checkTextAndSelectionChanges() {
        String contextPath = "nextContext";
        String descriptionText = "next description";
        String type = "Number";

        // set new content
        panel.textBox("contextPathTextField").setText(contextPath);
        panel.textBox("descriptionText").setText(descriptionText);
        panel.comboBox("typeOptionsDropdown").selectItem(type);

        // validate content
        assertEquals(contextPath, panel.textBox("contextPathTextField").text());
        assertEquals(descriptionText, panel.textBox("descriptionText").text());
        assertEquals(type, panel.comboBox("typeOptionsDropdown").selectedItem());
    }
}
