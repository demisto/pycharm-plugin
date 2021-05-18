package com.demisto.plugin.ide.unit.integrations;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.DemistoYML;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
import com.demisto.plugin.ide.integrations.model.DemistoIntegrationYML;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Category(DemistoYML.class)
public class DemistoIntegrationYMLTest {

    private Map integrationYML;

    private DemistoIntegrationYML demistoIntegrationYMLUnderTest;
    final public static String TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH = "src/test/resources/testIntegrationWithArgsAndOutputs.yml";
    final public static String TEST_YML_WITHOUT_ARGS_AND_OUTPUTS_PATH = "src/test/resources/testIntegrationWithoutArgsAndOutputs.yml";

    @Before
    public void setUp() throws IOException {
        String ymlString = DemistoUtils.readFile(TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        integrationYML = yaml.load(ymlString);
        demistoIntegrationYMLUnderTest = new DemistoIntegrationYML(integrationYML);
    }

    @Test
    public void testSetName() {
        // Setup
        final String name = "VirusTotal";

        // Run the test
        assertEquals(name, demistoIntegrationYMLUnderTest.getName());
    }
    @Test
    public void setNullName() {
        // Setup
        final String name = "";

        demistoIntegrationYMLUnderTest.setName(null);
        // Run the test
        assertEquals(name, demistoIntegrationYMLUnderTest.getName());
    }

    @Test
    public void testSetDescription() {
        // Setup
        final String description = "Analyze suspicious hashes, URLs, domains and IP addresses";

        // Run the test
        assertEquals(description, demistoIntegrationYMLUnderTest.getDescription());
    }

    @Test
    public void testSetNullDescription() {
        // Setup
        final String description = "";

        demistoIntegrationYMLUnderTest.setDescription(null);
        // Run the test
        assertEquals(description, demistoIntegrationYMLUnderTest.getDescription());
    }

    @Test
    public void testSetDisplay() {
        // Setup
        final String display = "VirusTotal";

        // Run the test
        assertEquals(display, demistoIntegrationYMLUnderTest.getDisplay());
    }

    @Test
    public void testSetNullDisplay() {
        // Setup
        final String display = "";

        demistoIntegrationYMLUnderTest.setDisplay(null);
        // Run the test
        assertEquals(display, demistoIntegrationYMLUnderTest.getDisplay());
    }

    @Test
    public void testSetDetailedDescription() {
        // Setup
        final String description = "Indicators thresholds";

        // Run the test
        assertEquals(description, demistoIntegrationYMLUnderTest.getDetaileddescription());
    }

    @Test
    public void testSetNullDetailedDescription() {
        // Setup
        final String description = "";

        demistoIntegrationYMLUnderTest.setDetaileddescription(null);
        // Run the test
        assertEquals(description, demistoIntegrationYMLUnderTest.getDetaileddescription());
    }

    @Test
    public void testSetCategory() {
        // Setup
        final String description = "Data Enrichment & Threat Intelligence";

        // Run the test
        assertEquals(description, demistoIntegrationYMLUnderTest.getCategory());
    }

    @Test
    public void testIntegrationScriptSetScript() {
        // Run the test
        String expected = "import requests";
        assertEquals(expected, demistoIntegrationYMLUnderTest.getScript().getScript());
    }

    @Test
    public void testIntegrationScriptSetType() {
        String expected = "python";
        // Run the test
        assertEquals(expected, demistoIntegrationYMLUnderTest.getScript().getType());
    }

    @Test
    public void testIntegrationScriptSetRunonce() {
        Boolean expected = false;
        // Run the test
        assertEquals(expected, demistoIntegrationYMLUnderTest.getScript().getRunonce());
    }

    @Test
    public void testIntegrationScriptSetIsfetch() {
        Boolean expected = false;
        // Run the test
        assertEquals(expected, demistoIntegrationYMLUnderTest.getScript().getIsfetch());
    }

    @Test
    public void testIntegrationScriptSetLongRunning() {
        Boolean expected = false;
        // Run the test
        assertEquals(expected, demistoIntegrationYMLUnderTest.getScript().getLongRunning());
    }

    @Test
    public void testIntegrationScriptSetLongRunningPort() {
        Boolean expected = false;
        // Run the test
        assertEquals(expected, demistoIntegrationYMLUnderTest.getScript().getLongRunningPort());
    }

    @Test
    public void testIntegrationScriptFeed() {
        Boolean expected = false;
        // Run the test
        assertEquals(expected, demistoIntegrationYMLUnderTest.getScript().getFeed());
    }

    @Test
    public void testIntegrationScriptSetCommandsSize() {
        int expected = 1;
        // Run the test
        assertEquals(expected, demistoIntegrationYMLUnderTest.getScript().getCommands().size());
    }

    @Test
    public void testIntegrationScriptSetCommandValues() {
        String name = "vt-private-check-file-behaviour";
        String description = "VirusTotal";
        Boolean execution = false;
        Boolean deprecated = false;

        // Run the test
        DemistoIntegrationYML.DemistoCommand command = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0);

        assertEquals(name, command.getName());
        assertEquals(description, command.getDescription());
        assertEquals(execution, command.getExecution());
        assertEquals(deprecated, command.getDeprecated());
    }

    @Test
    public void testAddEmptyArg() {
        // Run the test

        // should have 3 args before adding more
        assertEquals(3, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments().size());

        // now let's add another one and check that the size increased
        demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).addEmptyArg();
        assertEquals(demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments().size(),4);

        // verify that we added an empty arg
        DemistoAutomationYML.DemistoArgument arg = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments()
                .get(demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments().size()-1);

        assertEquals(arg.getName(),"");
        assertEquals(arg.getDefault(),false);
        assertEquals(arg.getRequired(),false);
        assertEquals(arg.getDefaultValue(),"");
        assertEquals(arg.getDescription(),"");
        assertEquals(arg.getIsArray(),false);
        assertEquals(arg.getSecret(),false);
        assertEquals(arg.getPredefined(),new ArrayList());
    }

    @Test
    public void testAddEmptyOutput() {
        // Run the test

        // should have 2 outputs before adding more
        assertEquals(2, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs().size());

        // now let's add another one and check that the size increased
        demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).addEmptyOutput();
        assertEquals(3, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs().size());

        // verify that we added an empty output
        DemistoAutomationYML.DemistoOutput output = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs().get(demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs().size()-1);

        assertEquals("", output.getContextPath());
        assertEquals("Unknown", output.getType());
        assertEquals("", output.getDescription());
    }


    @Test
    public void testValidateArguments() {
        /* validateArguments should remove empty arguments before saving the yml.
           we do so to avoid error when sending the yml to Demisto */

        // Setup
        ArrayList args;

        // Run the test

        // In the beginning we shouldn't have any empty args
        args = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments();

        assertEquals(3, args.size());
        demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).validateArguments();
        // we expect the amount to stay the same
        args = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments();
        assertEquals(3, args.size());

        // we add empty arg
        demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).addEmptyArg();
        // expect size to increase
        args = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments();
        assertEquals(4,args.size());

        // now we run the validate arguments and expect size to decrease
        demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).validateArguments();
        args = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments();
        assertEquals(3, args.size());
    }

    @Test
    public void testValidateOutputs() {
        /* validateOutputs should remove empty outputs before saving the yml.
           we do so to avoid error when sending the yml to Demisto */

        // Setup
        ArrayList<DemistoYML.DemistoOutput> outputs;

        // Run the test

        // In the beginning we shouldn't have any empty args
        assertEquals(2, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs().size());

        demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).validateOutputs();
        // we expect the amount to stay the same
        assertEquals(2, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs().size());

        // we add empty arg
        demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).addEmptyOutput();
        // expect size to increase
        assertEquals(3, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs().size());

        // now we run the validate arguments and expect size to decrease
        demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).validateOutputs();
        assertEquals(2, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs().size());
    }

    @Test
    public void setNullSecret() {
        // Setup
        final Boolean expectedResult = false;

        // Run the test
        DemistoAutomationYML.DemistoArgument arg = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments().get(0);
        arg.setSecret(null);

        // verify results
        assertEquals(expectedResult,arg.getSecret());
    }

    @Test
    public void setNullDefault() {
        // Setup
        final Boolean expectedResult = false;

        // Run the test
        DemistoAutomationYML.DemistoArgument arg = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments().get(0);
        arg.setDefault(null);
        // verify results
        assertEquals(expectedResult, arg.getDefault());
    }

    @Test
    public void setNullIsArray() {
        // Setup
        final Boolean expectedResult = false;

        // Run the test
        DemistoAutomationYML.DemistoArgument arg = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments().get(0);
        arg.setIsArray(null);
        // verify results
        assertEquals(expectedResult, arg.getIsArray());
    }

    @Test
    public void setDefaultValue() {
        // Setup
        final String expectedResult = "test";

        // Run the test
        DemistoAutomationYML.DemistoArgument arg = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments().get(0);
        arg.setDefaultValue("test");

        // verify results
        assertEquals(expectedResult, arg.getDefaultValue());
    }

    @Test
    public void setNullRequired() {
        // Setup
        final Boolean expectedResult = false;

        // Run the test
        DemistoAutomationYML.DemistoArgument arg = demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments().get(0);
        arg.setRequired(null);

        // verify results
        assertEquals(expectedResult, arg.getRequired());
    }

    @Test
    public void testEmptyArgument() {
        // Setup
        String ymlString = DemistoUtils.readFile(TEST_YML_WITHOUT_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        integrationYML = yaml.load(ymlString);
        demistoIntegrationYMLUnderTest = new DemistoIntegrationYML(integrationYML);

        ArrayList expectedResult = new ArrayList();

        // Run the test
        assertEquals(expectedResult, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getArguments());
    }

    @Test
    public void testEmptyOutput() {
        // Setup
        String ymlString = DemistoUtils.readFile(TEST_YML_WITHOUT_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        integrationYML = yaml.load(ymlString);
        demistoIntegrationYMLUnderTest = new DemistoIntegrationYML(integrationYML);

        ArrayList expectedResult = new ArrayList();

        // Run the test
        assertEquals(expectedResult, demistoIntegrationYMLUnderTest.getScript().getCommands().get(0).getOutputs());
    }

    @Test
    public void checkThatUnsupportedFieldsAreWritten() {
        // Setup
        String expectedResult = "data:image/png;base64,iVBORw0K";
        String ymlString = DemistoUtils.readFile(TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        integrationYML = yaml.load(ymlString);
        demistoIntegrationYMLUnderTest = new DemistoIntegrationYML(integrationYML);

        // get string representation of the updated automation yml
        Map demistoYML = demistoIntegrationYMLUnderTest.getDemistoYMLMapWithUnsupportedFields(demistoIntegrationYMLUnderTest);

        assert (demistoYML.containsKey("image"));
        assertEquals(expectedResult, demistoYML.get("image"));
    }

    @Test
    public void setUnsupportedFields() {
        // Setup
        Map expectedResult = demistoIntegrationYMLUnderTest.getUnsupportedFields();
        // Run the test
        demistoIntegrationYMLUnderTest.setUnsupportedFields(expectedResult);

        // verify results
        assertEquals(expectedResult, demistoIntegrationYMLUnderTest.getUnsupportedFields());
    }

    @Test
    public void setSupportedFieldsArray() {
        // Setup
        String [] expectedResult = demistoIntegrationYMLUnderTest.getSupportedFieldsArray();
        // Run the test
        demistoIntegrationYMLUnderTest.setSupportedFieldsArray(expectedResult);

        // verify results
        assertArrayEquals(expectedResult, demistoIntegrationYMLUnderTest.getSupportedFieldsArray());
    }

    @Test
    public void testScriptSetSubtype() {
        // Setup
        final String subtype = "python3";

        demistoIntegrationYMLUnderTest.getScript().setSubtype("python3");
        // Run the test
        assertEquals(subtype, demistoIntegrationYMLUnderTest.getScript().getSubtype());
    }

    @Test
    public void testScriptSetSubtype2() {
        // Setup
        final String subtype = "python2";

        demistoIntegrationYMLUnderTest.getScript().setSubtype("python2");
        // Run the test
        assertEquals(subtype, demistoIntegrationYMLUnderTest.getScript().getSubtype());
    }

    @Test
    public void testScriptSetSubtypeNull() {
        // Setup
        final String subtype = "python3";

        demistoIntegrationYMLUnderTest.getScript().setSubtype(null);
        // Run the test
        assertEquals(subtype, demistoIntegrationYMLUnderTest.getScript().getSubtype());
    }
}
