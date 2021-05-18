package com.demisto.plugin.ide.integration;

import com.demisto.plugin.ide.generalUIComponents.MessagePanel;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.annotation.RunsInEDT;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunsInEDT
@Category(GUITest.class)
public class MessagePanelTest extends AssertJSwingJUnitTestCase {

    private String message;
    private FrameFixture panel;

    @Override
    protected void onSetUp() {
        message = "test";
        JFrame frame = GuiActionRunner.execute((Callable<JFrame>) JFrame::new);
        MessagePanel messagePanel = GuiActionRunner.execute(() -> new MessagePanel(message));
        frame.add(messagePanel);

        panel = new FrameFixture(robot(), frame);
        panel.show(); // shows the frame to test
    }

    @Test
    public void shouldHaveTextBoxWithCorrectMessage() {
        assertNotNull(panel.textBox("messageLabel"));
        assertEquals(message, panel.textBox("messageLabel").text());
    }
}
