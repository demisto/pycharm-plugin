package com.demisto.plugin.ide.unit;

import com.demisto.plugin.ide.DemistoUtils;
import com.demisto.plugin.ide.DemistoYML;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static com.demisto.plugin.ide.DemistoUtils.getValueOfStringOrNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(DemistoYML.class)
public class UtilsTest {
    @Test
    public void testWriteImageToFile() {
        // Setup
        String imageDataPath = "src/test/resources/base64.txt";
        String destPath = "src/test/resources/test.png";

        // Run the test
        String imageData = DemistoUtils.readFile(imageDataPath);
        DemistoUtils.writeImageToFile(destPath, imageData);
        String imageDataFromFile = DemistoUtils.readFile(destPath, true);

        // Verify the results
        try {
            assertEquals(imageData, imageDataFromFile);
        } finally {
            DemistoUtils.deleteFile(destPath);
        }
    }

    @Test
    public void testCreateImageFilePNG() {
        // Setup
        String imageDataPath = "src/test/resources/base64.txt";
        String destPath = "src/test/resources/test.png";
        String imagePath = DemistoUtils.addFilePostfix(destPath, DemistoUtils.IMAGE_POSTFIX);

        // Run the test
        String imageData = DemistoUtils.readFile(imageDataPath);
        DemistoUtils.createImageFile(DemistoUtils.renameFileExtension(destPath, "yml"), DemistoUtils.BASE64_PNG_PREFIX + imageData);
        String imageDataFromFile = DemistoUtils.readFile(imagePath, true);

        // Verify the results
        try {
            assertEquals(imageData, imageDataFromFile);
        } finally {
            DemistoUtils.deleteFile(imagePath);
        }
    }

    @Test
    public void testCreateImageFileJPG() {
        // Setup
        String imageDataPath = "src/test/resources/base64.txt";
        String destPath = "src/test/resources/test.jpg";
        String imagePath = DemistoUtils.addFilePostfix(destPath, DemistoUtils.IMAGE_POSTFIX);

        // Run the test
        String imageData = DemistoUtils.readFile(imageDataPath);
        DemistoUtils.createImageFile(DemistoUtils.renameFileExtension(destPath, "yml"), DemistoUtils.BASE64_JPEG_PREFIX + imageData);
        String imageDataFromFile = DemistoUtils.readFile(imagePath, true);

        // Verify the results
        try {
            assertEquals(imageData, imageDataFromFile);
        } finally {
            DemistoUtils.deleteFile(imagePath);
        }
    }

    @Test
    public void testCreateImageFileEmptyFile() {
        // Setup
        String destPath = "src/test/resources/test.png";
        String imagePath = DemistoUtils.addFilePostfix(destPath, DemistoUtils.IMAGE_POSTFIX);
        String imageData = DemistoUtils.readFile("src/test/resources/placeholder.png", true);

        // Run the test
        DemistoUtils.createImageFile(DemistoUtils.renameFileExtension(destPath, "yml"), "");
        String imageDataFromFile = DemistoUtils.readFile(imagePath, true);

        // Verify the results
        try {
            assertEquals(imageData, imageDataFromFile);
        } finally {
            DemistoUtils.deleteFile(imagePath);
        }
    }

    @Test
    public void testCreateImageFileInvalidFile() {
        // Setup
        String imageDataPath = "src/test/resources/base64.txt";
        String destPath = "src/test/resources/test.png";
        String imagePath = DemistoUtils.addFilePostfix(destPath, DemistoUtils.IMAGE_POSTFIX);
        String imageData = DemistoUtils.readFile("src/test/resources/placeholder.png", true);

        // Run the test
        DemistoUtils.createImageFile(DemistoUtils.renameFileExtension(destPath, "yml"), "image/sade" + imageData);
        String imageDataFromFile = DemistoUtils.readFile(imagePath, true);

        // Verify the results
        try {
            assertEquals(imageData, imageDataFromFile);
        } finally {
            DemistoUtils.deleteFile(imagePath);
        }
    }

    @Test
    public void testWriteImageToFileEmptyImage() {
        // Setup
        String imageData = "";
        String destPath = "src/test/resources/test.png";

        // Run the test
        DemistoUtils.writeImageToFile(destPath, imageData);
        String imageDataFromFile = DemistoUtils.readFile(destPath, true);

        // Verify the results
        try {
            assertEquals(imageData, imageDataFromFile);
        } finally {
            DemistoUtils.deleteFile(destPath);
        }
    }

    @Test
    public void testReadFileWithEncode() {
        // Setup
        String imagePath = "src/test/resources/icons/export-to-demisto.png";
        String expectedData = DemistoUtils.readFile("src/test/resources/base64.txt");

        // Run the test
        String imageData = DemistoUtils.readFile(imagePath, true);
        assertEquals(expectedData, imageData);
    }

    @Test
    public void testAddFilePostfixWithExt() {
        // Setup
        final String source = "source.py";
        final String postfix = "_postfix";
        final String expectedResult = "source_postfix.py";

        // Run the test
        final String result = DemistoUtils.addFilePostfix(source, postfix);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testAddFilePostfixWithoutExt() {
        // Setup
        final String source = "source";
        final String postfix = "_postfix";
        final String expectedResult = "source_postfix";

        // Run the test
        final String result = DemistoUtils.addFilePostfix(source, postfix);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRemoveFilePrefixWithEmptyPrefix() {
        // Setup
        final String prefix = "";
        final String source = prefix + "source.py";
        final String expectedResult = "source.py";

        // Run the test
        final String result = DemistoUtils.removePrefix(source, prefix);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRemoveFilePrefixWithFileName() {
        // Setup
        final String source = "source.py";
        final String expectedResult = "source.py";

        // Run the test
        final String result = DemistoUtils.removePrefix(source, FilenameUtils.removeExtension(source));

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRemoveFilePrefixWithWholeFileName() {
        // Setup
        final String source = "source.py";
        final String expectedResult = "source.py";

        // Run the test
        final String result = DemistoUtils.removePrefix(source, source);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRemoveFilePrefix() {
        // Setup
        final String prefix = "integration-";
        final String source = prefix + "source.py";
        final String expectedResult = "source.py";

        // Run the test
        final String result = DemistoUtils.removePrefix(source, prefix);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRenameFileExtensionStringWithExt() {
        // Setup
        final String source = "source.py";
        final String newExtension = "yml";
        final String expectedResult = "source.yml";

        // Run the test
        final String result = DemistoUtils.renameFileExtension(source, newExtension);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRenameFileExtensionStringWithoutExt() {
        // Setup
        final String source = "source";
        final String newExtension = "yml";
        final String expectedResult = "source.yml";

        // Run the test
        final String result = DemistoUtils.renameFileExtension(source, newExtension);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRenameFileExtensionStringWithTwoExt() {
        // Setup
        final String source = "source.py.zip";
        final String newExtension = "yml";
        final String expectedResult = "source.py.yml";

        // Run the test
        final String result = DemistoUtils.renameFileExtension(source, newExtension);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testRenameFileExtensionStringWithPath() {
        // Setup
        final String source = "/users/source.py.zip";
        final String newExtension = "yml";
        final String expectedResult = "/users/source.py.yml";

        // Run the test
        final String result = DemistoUtils.renameFileExtension(source, newExtension);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testStringIsNotEmptyOrNullWithString() {
        // Setup
        final String str = "str";
        final Boolean expectedResult = true;

        // Run the test
        final Boolean result = DemistoUtils.stringIsNotEmptyOrNull(str);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testStringIsNotEmptyOrNullWithEmptyString() {
        // Setup
        final String str = "";
        final Boolean expectedResult = false;

        // Run the test
        final Boolean result = DemistoUtils.stringIsNotEmptyOrNull(str);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testStringIsNotEmptyOrNullWithNull() {
        // Setup
        final String str = null;
        final Boolean expectedResult = false;

        // Run the test
        final Boolean result = DemistoUtils.stringIsNotEmptyOrNull(str);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void testStringIsNotEmptyOrNullWithNullString() {
        // Run the test
        final Boolean result = DemistoUtils.stringIsNotEmptyOrNull("null");

        // Verify the results
        assertEquals(false, result);
    }

    @Test
    public void testBooleanIsNotEmptyOrNullWithFalse() {
        // Run the test
        final Boolean result = DemistoUtils.booleanIsNotEmptyOrNull(false);

        // Verify the results
        assertEquals(true, result);
    }

    @Test
    public void testBooleanIsNotEmptyOrNullWithTrue() {
        // Run the test
        final Boolean result = DemistoUtils.booleanIsNotEmptyOrNull(true);

        // Verify the results
        assertEquals(true, result);
    }

    @Test
    public void testBooleanIsNotEmptyOrNullWithNull() {
        // Run the test
        final Boolean result = DemistoUtils.booleanIsNotEmptyOrNull(null);

        // Verify the results
        assertEquals(false, result);
    }

    @Test
    public void testReadFileWithNull() {
        // Run the test
        final String result = DemistoUtils.readFile("");

        // Verify the results
        assertEquals("", result);
    }

    @Test
    public void getYMLStringFromMap() {
        // Setup
        String ymlString = "hello: 25";
        String expectedResult = "hello: 25\n";
        Yaml yaml = new Yaml();
        Map map = yaml.load(ymlString);

        // Run the test
        final String result = DemistoUtils.getYMLStringFromMap(map);

        // Verify the results
        assertEquals(25, map.get("hello"));
        assertEquals(expectedResult, result);
    }

    @Test
    public void removeImportsFromPythonFile() {
        // Setup
        String scriptString = "import demistomock as demisto\n" +
                "from CommonServerPython import *\n" +
                "from CommonServerUserPython import *\n" +
                "from __future__ import print_function";
        String expectedResult = "\n\n\n";

        // Run the Test
        final String result = DemistoUtils.removeImportsFromPythonFile(scriptString);
        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    public void nullableString() {
        assertNull(getValueOfStringOrNull("null"));
    }

    @Test
    public void notNullString() {
        assertEquals(getValueOfStringOrNull("nonull"), "nonull");
    }
}
