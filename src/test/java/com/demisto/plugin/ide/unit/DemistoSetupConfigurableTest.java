package com.demisto.plugin.ide.unit;

import com.demisto.plugin.ide.DemistoYML;
import com.demisto.plugin.ide.plugin_settings_setup.DemistoSetupConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.swing.*;

// DemistoSetupGUI is also tested through this suite

@Category(DemistoYML.class)
public class DemistoSetupConfigurableTest extends LightCodeInsightFixtureTestCase {

    private DemistoSetupConfigurable demistoSetupConfigurableUnderTest;
    private Project p;

    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        p = myFixture.getProject();
        demistoSetupConfigurableUnderTest = new DemistoSetupConfigurable(p);
    }

    @Test
    public void testGetDisplayName() {
        // Setup
        final String expectedResult = "Demisto Setup Plugin";

        // Run the test
        final String result = demistoSetupConfigurableUnderTest.getDisplayName();

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetHelpTopic() {
        // Setup
        final String expectedResult = "preference.DemistoSetupConfigurable";

        // Run the test
        final String result = demistoSetupConfigurableUnderTest.getHelpTopic();

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetId() {
        // Setup
        final String expectedResult = "preference.DemistoSetupConfigurable";

        // Run the test
        final String result = demistoSetupConfigurableUnderTest.getId();

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testEnableSearch() {
        // Setup
        final String s = "s";
        final Runnable expectedResult = null;

        // Run the test
        final Runnable result = demistoSetupConfigurableUnderTest.enableSearch(s);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testCreateComponent() {
        // Setup

        // Run the test
        final JComponent result = demistoSetupConfigurableUnderTest.createComponent();

        // Verify the results
        assertNotNull(result);
    }

    @Test
    public void testIsModified() {
        // Setup
        demistoSetupConfigurableUnderTest.createComponent();

        // Run the test
        final boolean mGuiModified = demistoSetupConfigurableUnderTest.getmGUI().isModified();
        final boolean result = demistoSetupConfigurableUnderTest.isModified();

        // Verify the results
        assertEquals(mGuiModified, result);
    }

    @Test
    public void testReset() {
        // Setup
        demistoSetupConfigurableUnderTest.createComponent();

        String originalServerUrl = demistoSetupConfigurableUnderTest.getmConfig().getServerURL();
        String originalAPIKey = demistoSetupConfigurableUnderTest.getmConfig().getApiKey();
        String newServerUrl = "test";
        String newAPIKey = "test";

        // Run the test
        demistoSetupConfigurableUnderTest.getmGUI().setDemistoServerUrl(newServerUrl);
        demistoSetupConfigurableUnderTest.getmGUI().setDemistoApiKey(newAPIKey);
        demistoSetupConfigurableUnderTest.reset();

        // Verify the results
        assertEquals(originalServerUrl, demistoSetupConfigurableUnderTest.getmConfig().getServerURL());
        assertEquals(originalAPIKey, demistoSetupConfigurableUnderTest.getmConfig().getApiKey());
    }

    @Test
    public void testDisposeUIResources() {
        // Setup

        // Run the test
        demistoSetupConfigurableUnderTest.disposeUIResources();

        // Verify the results
        assertNull(demistoSetupConfigurableUnderTest.getmGUI());
    }
}
