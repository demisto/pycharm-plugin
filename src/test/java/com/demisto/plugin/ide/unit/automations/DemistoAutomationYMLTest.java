package com.demisto.plugin.ide.unit.automations;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.DemistoYML;
import com.demisto.plugin.ide.automations.model.DemistoAutomationYML;
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
public class DemistoAutomationYMLTest {

    private Map automationYML;

    private DemistoAutomationYML demistoAutomationYMLUnderTest;
    final public static String TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH = "src/test/resources/testAutomationYML_with_args_and_outputs.yml";
    final public static String TEST_YML_WITHOUT_ARGS_AND_OUTPUTS_PATH = "src/test/resources/testAutomationYML_without_args_and_outputs.yml";

    @Before
    public void setUp() throws IOException {
        String ymlString = DemistoUtils.readFile(TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        automationYML = yaml.load(ymlString);
        demistoAutomationYMLUnderTest = new DemistoAutomationYML(automationYML);
    }

    @Test
    public void testScript() {
        // Setup
        final String script = "script";

        // Run the test
        demistoAutomationYMLUnderTest.setScript(script);

        // Verify the results
        assertEquals(demistoAutomationYMLUnderTest.getScript(),script);
    }

    @Test
    public void testEmptyScript() {
        // Setup
        final String script = "";

        // Run the test
        demistoAutomationYMLUnderTest.setScript(script);

        // Verify the results
        assertEquals(demistoAutomationYMLUnderTest.getScript(),script);
    }

    @Test
    public void testSetTags() {
        // Setup
        ArrayList initialRes = new ArrayList();
        initialRes.add("infra");
        initialRes.add("server");

        // Run the test
        assertEquals(demistoAutomationYMLUnderTest.getTags(),initialRes);
    }

    @Test
    public void testSetNullTags() {
        // Setup
        ArrayList initialRes = new ArrayList();

        demistoAutomationYMLUnderTest.setTags(null);
        // Run the test
        assertEquals(demistoAutomationYMLUnderTest.getTags(),initialRes);
    }

    @Test
    public void testSetComment() {
        // Setup
        final String comment = "test yaml";

        // Run the test
        assertEquals(demistoAutomationYMLUnderTest.getComment(),comment);

    }

    @Test
    public void testSetEnabled() {
        // Setup
        final Boolean enabled = true;

        // Run the test
        demistoAutomationYMLUnderTest.setEnabled(enabled);
        assertEquals(demistoAutomationYMLUnderTest.getEnabled(),enabled);
    }

    @Test
    public void testSetSystem() {
        // Setup
        final Boolean system = false;

        // Run the test
        assertEquals(demistoAutomationYMLUnderTest.getSystem(),system);
    }

    @Test
    public void testAddEmptyArg() {
        // Run the test

        // should have 2 args before adding more
        assertEquals(demistoAutomationYMLUnderTest.getArgs().size(),2);

        // now let's add another one and check that the size increased
        demistoAutomationYMLUnderTest.addEmptyArg();
        assertEquals(demistoAutomationYMLUnderTest.getArgs().size(),3);

        // verify that we added an empty arg
        DemistoAutomationYML.DemistoArgument arg = demistoAutomationYMLUnderTest.getArgs().get(demistoAutomationYMLUnderTest.getArgs().size()-1);

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
        assertEquals(demistoAutomationYMLUnderTest.getOutputs().size(),2);

        // now let's add another one and check that the size increased
        demistoAutomationYMLUnderTest.addEmptyOutput();
        assertEquals(demistoAutomationYMLUnderTest.getOutputs().size(),3);

        // verify that we added an empty output
        DemistoAutomationYML.DemistoOutput output = demistoAutomationYMLUnderTest.getOutputs().get(demistoAutomationYMLUnderTest.getOutputs().size()-1);

        assertEquals(output.getContextPath(),"");
        assertEquals(output.getType(),"Unknown");
        assertEquals(output.getDescription(),"");
    }

    @Test
    public void testSetScripttarget() {
        // Setup
        final Integer scripttarget = 0;

        // Run the test
        assertEquals(demistoAutomationYMLUnderTest.getScripttarget(),scripttarget);
    }

    @Test
    public void testSetTimeout() {
        // Setup
        final String timeout = "0";

        // Run the test
        assertEquals(demistoAutomationYMLUnderTest.getTimeout(),timeout);
    }

    @Test
    public void testSetName() {
        // Setup
        final String name = "testYML_with_args_and_outputs";

        // Run the test
        assertEquals(demistoAutomationYMLUnderTest.getName(),name);
    }

    @Test
    public void testValidateArguments() {
        /* validateArguments should remove empty arguments before saving the yml.
           we do so to avoid error when sending the yml to Demisto */

        // Setup
        ArrayList args;

        // Run the test

        // In the beginning we shouldn't have any empty args
        args = demistoAutomationYMLUnderTest.getArgs();

        assertEquals(args.size(),2);
        demistoAutomationYMLUnderTest.validateArguments();
        // we expect the amount to stay the same
        args = demistoAutomationYMLUnderTest.getArgs();
        assertEquals(args.size(),2);

        // we add empty arg
        demistoAutomationYMLUnderTest.addEmptyArg();
        // expect size to increase
        args = demistoAutomationYMLUnderTest.getArgs();
        assertEquals(args.size(),3);

        // now we run the validate arguments and expect size to decrease
        demistoAutomationYMLUnderTest.validateArguments();
        args = demistoAutomationYMLUnderTest.getArgs();
        assertEquals(args.size(),2);
    }

    @Test
    public void testValidateOutputs() {
        /* validateOutputs should remove empty outputs before saving the yml.
           we do so to avoid error when sending the yml to Demisto */

        // Setup
        ArrayList outputs;

        // Run the test

        // In the beginning we shouldn't have any empty args
        outputs = demistoAutomationYMLUnderTest.getOutputs();

        assertEquals(outputs.size(),2);
        demistoAutomationYMLUnderTest.validateOutputs();
        // we expect the amount to stay the same
        outputs = demistoAutomationYMLUnderTest.getOutputs();
        assertEquals(outputs.size(),2);

        // we add empty arg
        demistoAutomationYMLUnderTest.addEmptyOutput();
        // expect size to increase
        outputs = demistoAutomationYMLUnderTest.getOutputs();
        assertEquals(outputs.size(),3);

        // now we run the validate arguments and expect size to decrease
        demistoAutomationYMLUnderTest.validateOutputs();
        outputs = demistoAutomationYMLUnderTest.getOutputs();
        assertEquals(2, outputs.size());
    }

    @Test
    public void getType() {
        // Setup
        final String type = "python";

        // Run the test
        assertEquals(type, demistoAutomationYMLUnderTest.getType());
    }

    @Test
    public void getComment() {
        // Setup
        final String comment = "";

        demistoAutomationYMLUnderTest.setComment(null);
        // Run the test
        assertEquals(comment, demistoAutomationYMLUnderTest.getComment());
    }

    @Test
    public void setNullEnabled() {
        // Setup
        final Boolean enabled = true;

        demistoAutomationYMLUnderTest.setEnabled(null);
        // Run the test
        assertEquals(enabled, demistoAutomationYMLUnderTest.getEnabled());
    }

    @Test
    public void setNullSystem() {
        // Setup
        final Boolean system = false;

        demistoAutomationYMLUnderTest.setSystem(null);
        // Run the test
        assertEquals(system, demistoAutomationYMLUnderTest.getSystem());
    }

    @Test
    public void setNullScripttarget() {
        // Setup
        final Integer scripttarget = 0;

        demistoAutomationYMLUnderTest.setScripttarget(null);
        // Run the test
        assertEquals(scripttarget, demistoAutomationYMLUnderTest.getScripttarget());
    }


    @Test
    public void setNullTimeout() {
        // Setup
        final String timeout = "0";

        demistoAutomationYMLUnderTest.setTimeout(null);
        // Run the test
        assertEquals(timeout, demistoAutomationYMLUnderTest.getTimeout());
    }

    @Test
    public void setTimeout() {
        // Setup
        final String timeout = "0.1";

        demistoAutomationYMLUnderTest.setTimeout("0.1");
        // Run the test
        assertEquals(timeout, demistoAutomationYMLUnderTest.getTimeout());
    }

    @Test
    public void setNullName() {
        // Setup
        final String name = "";

        demistoAutomationYMLUnderTest.setName(null);
        // Run the test
        assertEquals(name, demistoAutomationYMLUnderTest.getName());
    }

    @Test
    public void setNullSecret() {
        // Setup
        final Boolean expectedResult = false;

        // Run the test
        demistoAutomationYMLUnderTest.getArgs().get(0).setSecret(null);

        // verify results
        assertEquals(expectedResult, demistoAutomationYMLUnderTest.getArgs().get(0).getSecret());
    }

    @Test
    public void setNullDefault() {
        // Setup
        final Boolean expectedResult = false;

        // Run the test
        demistoAutomationYMLUnderTest.getArgs().get(0).setDefault(null);

        // verify results
        assertEquals(expectedResult, demistoAutomationYMLUnderTest.getArgs().get(0).getDefault());
    }

    @Test
    public void setNullIsArray() {
        // Setup
        final Boolean expectedResult = false;

        // Run the test
        demistoAutomationYMLUnderTest.getArgs().get(0).setIsArray(null);

        // verify results
        assertEquals(expectedResult, demistoAutomationYMLUnderTest.getArgs().get(0).getIsArray());
    }

    @Test
    public void setDefaultValue() {
        // Setup
        final String expectedResult = "test";

        // Run the test
        demistoAutomationYMLUnderTest.getArgs().get(0).setDefaultValue("test");

        // verify results
        assertEquals(expectedResult, demistoAutomationYMLUnderTest.getArgs().get(0).getDefaultValue());
    }

    @Test
    public void setNullRequired() {
        // Setup
        final Boolean expectedResult = false;

        // Run the test
        demistoAutomationYMLUnderTest.getArgs().get(0).setRequired(null);

        // verify results
        assertEquals(expectedResult, demistoAutomationYMLUnderTest.getArgs().get(0).getRequired());
    }

    @Test
    public void testEmptyArgument() {
        // Setup
        String ymlString = DemistoUtils.readFile(TEST_YML_WITHOUT_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        automationYML = yaml.load(ymlString);
        demistoAutomationYMLUnderTest = new DemistoAutomationYML(automationYML);

        ArrayList expectedResult = new ArrayList();

        // Run the test
        assertEquals(expectedResult, demistoAutomationYMLUnderTest.getArgs());
    }

    @Test
    public void testEmptyOutput() {
        // Setup
        String ymlString = DemistoUtils.readFile(TEST_YML_WITHOUT_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        automationYML = yaml.load(ymlString);
        demistoAutomationYMLUnderTest = new DemistoAutomationYML(automationYML);

        ArrayList expectedResult = new ArrayList();

        // Run the test
        assertEquals(expectedResult, demistoAutomationYMLUnderTest.getOutputs());
    }

    @Test
    public void checkThatUnsupportedFieldsAreWritten() {
        // Setup
        Boolean expectedResult = false;
        String ymlString = DemistoUtils.readFile(TEST_YML_WITH_ARGS_AND_OUTPUTS_PATH);
        Yaml yaml = new Yaml();
        automationYML = yaml.load(ymlString);
        demistoAutomationYMLUnderTest = new DemistoAutomationYML(automationYML);

        // get string representation of the updated automation yml
        Map demistoYML = demistoAutomationYMLUnderTest.getDemistoYMLMapWithUnsupportedFields(demistoAutomationYMLUnderTest);

        assert (demistoYML.containsKey("runonce"));
        assertEquals(expectedResult, demistoYML.get("runonce"));
    }

    @Test
    public void setUnsupportedFields() {
        // Setup
        Map expectedResult = demistoAutomationYMLUnderTest.getUnsupportedFields();
        // Run the test
        demistoAutomationYMLUnderTest.setUnsupportedFields(expectedResult);

        // verify results
        assertEquals(expectedResult, demistoAutomationYMLUnderTest.getUnsupportedFields());
    }

    @Test
    public void setSupportedFieldsArray() {
        // Setup
        String [] expectedResult = demistoAutomationYMLUnderTest.getSupportedFieldsArray();
        // Run the test
        demistoAutomationYMLUnderTest.setSupportedFieldsArray(expectedResult);

        // verify results
        assertArrayEquals(expectedResult, demistoAutomationYMLUnderTest.getSupportedFieldsArray());
    }

    @Test
    public void testSetSubtype() {
        // Setup
        final String subtype = "python3";

        demistoAutomationYMLUnderTest.setSubtype("python3");
        // Run the test
        assertEquals(subtype, demistoAutomationYMLUnderTest.getSubtype());
    }

    @Test
    public void testSetSubtype2() {
        // Setup
        final String subtype = "python2";

        demistoAutomationYMLUnderTest.setSubtype("python2");
        // Run the test
        assertEquals(subtype, demistoAutomationYMLUnderTest.getSubtype());
    }

    @Test
    public void testSetSubtypeNull() {
        // Setup
        final String subtype = "python3";

        demistoAutomationYMLUnderTest.setSubtype(null);
        // Run the test
        assertEquals(subtype, demistoAutomationYMLUnderTest.getSubtype());
    }
}
