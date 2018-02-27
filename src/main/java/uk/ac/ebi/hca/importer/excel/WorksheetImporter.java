package uk.ac.ebi.hca.importer.excel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class WorksheetImporter {

    private final ObjectMapper objectMapper;

    private final WorksheetMapping worksheetMapping;

    private final String fieldName;

    private final JsonNode predefinedValues;

    public WorksheetImporter(ObjectMapper objectMapper, WorksheetMapping worksheetMapping) {
        this(objectMapper, "", worksheetMapping);
    }

    public WorksheetImporter(ObjectMapper objectMapper, String fieldName,
            WorksheetMapping worksheetMapping) {
        this(objectMapper, fieldName, worksheetMapping, objectMapper.createObjectNode());
    }

    public WorksheetImporter(ObjectMapper objectMapper, String fieldName,
            WorksheetMapping worksheetMapping, ObjectNode predefinedValues) {
        this.objectMapper = objectMapper;
        this.fieldName = fieldName;
        this.worksheetMapping = worksheetMapping;
        this.predefinedValues = predefinedValues;
    }

    public String getFieldName() {
        return fieldName;
    }

    public JsonNode importFrom(Sheet worksheet) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        String arrayName = fieldName.isEmpty() ? worksheet.getSheetName() : fieldName;
        ArrayNode arrayNode = objectNode.putArray(arrayName);

        Row headerRow = worksheet.getRow(2);
        for (int row = 3; row <= worksheet.getLastRowNum(); row++) {
            ObjectNode rowJson = predefinedValues.deepCopy();
            worksheet.getRow(row).iterator().forEachRemaining(dataCell -> {
                Cell headerCell = headerRow.getCell(dataCell.getColumnIndex());
                String header = headerCell.getStringCellValue();
                CellMapping cellMapping = worksheetMapping.getMappingFor(header);
                cellMapping.importTo(rowJson, dataCell);
            });
            arrayNode.add(rowJson);
        }

        return objectNode;
    }

}
