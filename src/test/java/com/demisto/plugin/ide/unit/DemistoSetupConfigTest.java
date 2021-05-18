package com.demisto.plugin.ide.unit;

import com.demisto.plugin.ide.plugin_settings_setup.DemistoSetupConfig;
import com.demisto.plugin.ide.DemistoYML;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(DemistoYML.class)
public class DemistoSetupConfigTest extends LightCodeInsightFixtureTestCase {

    private DemistoSetupConfig demistoSetupConfigUnderTest;
    private Project p;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        p = myFixture.getProject();
        demistoSetupConfigUnderTest = DemistoSetupConfig.getInstance(p);
        demistoSetupConfigUnderTest.setServerURL("");
        demistoSetupConfigUnderTest.setApiKey("");

    }

    @Test
    public void testSetApiKey() {
        // Setup
        final String apiKey = "apiKey";

        // Run the test
        demistoSetupConfigUnderTest.setApiKey(apiKey);

        // Verify the results
        assertEquals(apiKey, demistoSetupConfigUnderTest.getApiKey());
    }

    @Test
    public void testGetState() {
        // Run the test
        final DemistoSetupConfig result = demistoSetupConfigUnderTest.getState();

        // Verify the results
        assertEquals(result, demistoSetupConfigUnderTest);
    }

    @Test
    public void testLoadState() {
        // Setup
        DemistoSetupConfig demistoSetupConfig = new DemistoSetupConfig();

        String apiKey = "test";
        String serverURL = "www.test.com";

        demistoSetupConfig.setApiKey(apiKey);
        demistoSetupConfig.setServerURL(serverURL);
        // Run the test
        demistoSetupConfigUnderTest.loadState(demistoSetupConfig);

        // Verify the results
        assertEquals(demistoSetupConfig.getApiKey(),apiKey);
        assertEquals(demistoSetupConfig.getServerURL(),serverURL);
    }

    @Test
    public void testGetServerURL() {
        // Setup
        final String expectedResult = "";

        // Run the test
        final String result = demistoSetupConfigUnderTest.getServerURL();

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testSetServerURL() {
        // Setup
        final String serverURL = "test";
        // Run the test
        demistoSetupConfigUnderTest.setServerURL(serverURL);
        // Verify the results
        assertEquals(serverURL, demistoSetupConfigUnderTest.getServerURL());
    }

    @Test
    public void testGetInstance() {
        // Run the test
        final DemistoSetupConfig result = DemistoSetupConfig.getInstance(p);

        // Verify the results
        assertEquals(result.getServerURL(), "");
    }
}
