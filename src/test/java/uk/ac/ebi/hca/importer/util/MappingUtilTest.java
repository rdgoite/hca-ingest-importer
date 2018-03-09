package uk.ac.ebi.hca.importer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.ac.ebi.hca.importer.excel.WorksheetMappingSpy;
import uk.ac.ebi.hca.test.IngestTestRunner;
import uk.ac.ebi.hca.test.IntegrationTest;

import static org.junit.Assert.assertEquals;

@RunWith(IngestTestRunner.class)
public class MappingUtilTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private MappingUtil mappingUtil = new MappingUtil();

    @IntegrationTest
    public void test_addMappingsFromSchema_with_valid_donor_organism_core_schema() {
        WorksheetMappingSpy worksheetMapping = new WorksheetMappingSpy();
        mappingUtil.populateMappingsFromSchema(worksheetMapping, "https://schema.humancellatlas.org/type/biomaterial/5.0.0/donor_organism", "");
        System.out.println(worksheetMapping.toString());
        assertEquals(38, worksheetMapping.getNumberOfMappings());
    }

    @IntegrationTest
    public void test_addMappingsFromSchema_with_valid_smartseq2_schema() {
        WorksheetMappingSpy worksheetMapping = new WorksheetMappingSpy();
        mappingUtil.populateMappingsFromSchema(worksheetMapping, "https://schema.humancellatlas.org/module/process/sequencing/5.0.0/smartseq2", "");
        assertEquals(5, worksheetMapping.getNumberOfMappings());
    }

    @Test(expected = RuntimeException.class)
    public void test_addMappingsFromSchema_with_invalid_schema() {
        WorksheetMappingSpy worksheetMapping = new WorksheetMappingSpy();
        mappingUtil.populateMappingsFromSchema(worksheetMapping, "https://schema.humancellatlas.org/core/biomaterial/5.0.0/invalid", "");
        assertEquals(0, worksheetMapping.getNumberOfMappings());
    }

    @IntegrationTest
    public void test_addMappingsFromSchema_with_valid_project_schema() {
        WorksheetMappingSpy worksheetMapping = new WorksheetMappingSpy();
        mappingUtil.populateMappingsFromSchema(worksheetMapping, "https://schema.humancellatlas.org/type/project/5.0.0/project", "");
        System.out.println(worksheetMapping.toString());
        assertEquals(8, worksheetMapping.getNumberOfMappings());
    }

    @IntegrationTest
    public void test_generatePredefinedValuesForSchema_with_valid_project_schema() {
        ObjectNode predefinedValues = objectMapper.createObjectNode();
        mappingUtil.populatePredefinedValuesForSchema(predefinedValues, "https://schema.humancellatlas.org/type/project/5.0.0/project");
        assertEquals("https://schema.humancellatlas.org/type/project/5.0.0/project", predefinedValues.get("describedBy").textValue());
        assertEquals("5.0.0", predefinedValues.get("schema_version").textValue());
        assertEquals("project", predefinedValues.get("schema_type").textValue());
    }

    @IntegrationTest
    public void test_generatePredefinedValuesForSchema_with_valid_donor_organism_schema() {
        ObjectNode predefinedValues = objectMapper.createObjectNode();
        mappingUtil.populatePredefinedValuesForSchema(predefinedValues, "https://schema.humancellatlas.org/type/biomaterial/5.0.0/donor_organism");
        assertEquals("https://schema.humancellatlas.org/type/biomaterial/5.0.0/donor_organism", predefinedValues.get("describedBy").textValue());
        assertEquals("5.0.0", predefinedValues.get("schema_version").textValue());
        assertEquals("biomaterial", predefinedValues.get("schema_type").textValue());
    }

}
