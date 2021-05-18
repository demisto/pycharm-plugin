package com.demisto.plugin.ide.unit;

import com.demisto.plugin.ide.DemistoYML;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(DemistoYML.class)
public class DemistoYMLTest {

    private DemistoYML demistoYMLUnderTest;

    @Before
    public void setUp() {
        demistoYMLUnderTest = new DemistoYML();
    }

    @Test
    public void testSetId() {
        // Setup
        final String firstId = "test";
        final String secondId = "test2";
        final Integer version = -1;
        // Run the test
        DemistoYML.DemistoCommonFields common = new DemistoYML.DemistoCommonFields(firstId, version);
        common.setId(secondId);
        // Verify the results
        assertEquals(secondId, common.getId());
    }

    @Test
    public void testSetVersion() {
        // Setup
        final String id = "test";
        final Integer firstVersion = 1;
        final Integer secondVersion = -1;
        // Run the test
        DemistoYML.DemistoCommonFields common = new DemistoYML.DemistoCommonFields(id, firstVersion);
        common.setVersion(secondVersion);
        // Verify the results
        assertEquals(secondVersion, common.getVersion());
    }

    @Test
    public void testCommonFields(){
        // Setup
        final String id = "testID";
        final int version = -1;
        final DemistoYML.DemistoCommonFields cf = new DemistoYML.DemistoCommonFields(id, version);

        // Run the test
        demistoYMLUnderTest.setCommonfields(cf);

        // Verify the results
        assertEquals(demistoYMLUnderTest.getCommonfields().getId(),id);
        assertEquals(java.util.Optional.ofNullable(demistoYMLUnderTest.getCommonfields().getVersion()),java.util.Optional.of(version));
    }
}
