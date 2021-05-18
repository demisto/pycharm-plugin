package com.demisto.plugin.ide.integration;

import com.demisto.plugin.ide.generalUIComponents.DemistoResultPanel;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.annotation.RunsInEDT;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
/**
 * @author  Shachar Hirshberg
 * @created December 26, 2018
 */
@RunsInEDT
@Category(GUITest.class)
public class DemistoResultPanelTest extends AssertJSwingJUnitTestCase {

    private FrameFixture panel;

    @Override
    protected void onSetUp() {
        JSONObject res = new JSONObject("{\"note\":false,\"pinned\":false,\"reputations\":null,\"instance\":\"Scripts\",\"errorSource\":\"\",\"scheduled\":false,\"roles\":null,\"type\":1,\"times\":0,\"file\":\"\",\"fileMetadata\":null,\"timezoneOffset\":0,\"modified\":\"2018-12-25T21:48:12.836914+02:00\",\"id\":\"402@3fdea85e-d21e-4cc7-899e-d07ef06951ec\",\"recurrent\":false,\"brand\":\"Scripts\",\"fileID\":\"\",\"previousRoles\":null,\"investigationId\":\"3fdea85e-d21e-4cc7-899e-d07ef06951ec\",\"reputationSize\":0,\"contentsSize\":88,\"created\":\"2018-12-25T21:48:12.836561+02:00\",\"format\":\"markdown\",\"cronView\":false,\"parentEntryTruncated\":false,\"version\":1,\"parentId\":\"401@3fdea85e-d21e-4cc7-899e-d07ef06951ec\",\"tags\":null,\"system\":\"\",\"endingDate\":\"0001-01-01T00:00:00Z\",\"contents\":\"### Result: IP to Host\",\"tagsRaw\":null,\"playbookId\":\"\",\"hasRole\":false,\"entryTask\":null,\"parentContent\":\"!IPToHost234 ip=\\\"8.8.8.8\\\"\",\"category\":\"artifact\",\"user\":\"\",\"startDate\":\"0001-01-01T00:00:00Z\",\"taskId\":\"\"}\n");
        JFrame frame = GuiActionRunner.execute((Callable<JFrame>) JFrame::new);
        DemistoResultPanel resultPanel = GuiActionRunner.execute(() -> new DemistoResultPanel(res));
        frame.add(resultPanel);

        panel = new FrameFixture(robot(), frame);
        panel.show(); // shows the frame to test
    }

    @Test
    public void shouldHaveTextBoxWithCorrectMessage() {
        String expectedTitle = "Result is:";
        String expectedResult = "<html>\n" +
                "  <head>\n" +
                "    \n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h3>\n" +
                "      Result: IP to Host\n" +
                "    </h3>\n" +
                "  </body>\n" +
                "</html>\n";
        assertNotNull(panel.label("vendorResultOrErrorLabel"));
        assertNotNull(panel.textBox("resultLabel"));
        assertEquals(expectedTitle, panel.label("vendorResultOrErrorLabel").text());
        assertEquals(expectedResult, panel.textBox("resultLabel").text());
    }
}
