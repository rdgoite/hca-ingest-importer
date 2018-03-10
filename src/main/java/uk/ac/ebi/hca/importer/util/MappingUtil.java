package uk.ac.ebi.hca.importer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.hca.importer.excel.SchemaDataType;
import uk.ac.ebi.hca.importer.excel.WorksheetMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MappingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingUtil.class);

    public void populateMappingsFromSchema(WorksheetMapping worksheetMapping, String schemaUrl, String prefix) {
        try {
            String text = getText(schemaUrl).trim();
            JsonNode root = new ObjectMapper().readTree(text);
            JsonNode properties = root.get("properties");
            for (String field : iteratorToIterable(properties.fieldNames())) {
                JsonNode property = properties.get(field);
                if (property.has("user_friendly")) {
                    String id = prefix.isEmpty() ? field : prefix + "." + field;
                    mapUserFriendlyField(worksheetMapping, id, property);
                }
                if (property.has("$ref")) {
                    String ref_schema_url = property.get("$ref").textValue();
                    populateMappingsFromSchema(worksheetMapping, ref_schema_url, field);
                }
            }
        } catch (IOException e) {
            LOGGER.info("Error processing json at " + schemaUrl + ": " + e);
            throw new RuntimeException("Invalid schema: " + prefix + ": " + schemaUrl);
        }
    }

    private void mapUserFriendlyField(WorksheetMapping worksheetMapping, String id, JsonNode property) {
        String header = property.get("user_friendly").textValue();
        SchemaDataType schemaDataType = determineDataType(id, property);
        String ref = determineRef(id, property, schemaDataType);
        worksheetMapping.map(header, id, schemaDataType, ref, false);
    }

    private String determineRef(String id, JsonNode property, SchemaDataType schemaDataType) {
        String ref = "";
        if (schemaDataType == SchemaDataType.OBJECT)
        {
            if (property.has("$ref"))
            {
                ref = property.get("$ref").textValue();
            }
            else {
                throw new RuntimeException("$ref not found for " + id);
            }
        }
        if (schemaDataType == SchemaDataType.OBJECT_ARRAY)
        {
            if (property.has("items"))
            {
                JsonNode items = property.get("items");
                if (items.has("$ref"))
                {
                    ref = items.get("$ref").textValue();
                }
                else {
                    throw new RuntimeException("$ref not found for " + id);
                }
            }
            else {
                throw new RuntimeException("$ref not found for " + id);
            }
        }
        return ref;
    }

    private SchemaDataType determineDataType(String id, JsonNode property) {
        if (property.has("enum")) {
            return SchemaDataType.ENUM;
        }
        String typeStr = "";
        String arrayTypeStr = "";
        if (property.has("type")) {
            typeStr = property.get("type").textValue();
            if (property.has("items")) {
                JsonNode items = property.get("items");
                if (items.has("type")) {
                    arrayTypeStr = items.get("type").textValue();
                }
                if (items.has("$ref")) {
                    arrayTypeStr = items.get("$ref").textValue();
                }
            }
            switch (typeStr) {
                case "string":
                    return SchemaDataType.STRING;
                case "integer":
                    return SchemaDataType.INTEGER;
                case "number":
                    return SchemaDataType.NUMBER;
                case "boolean":
                    return SchemaDataType.BOOLEAN;
                case "object":
                    return SchemaDataType.OBJECT;
                case "array":
                    switch (arrayTypeStr) {
                        case "string":
                            return SchemaDataType.STRING_ARRAY;
                        case "integer":
                            return SchemaDataType.INTEGER_ARRAY;
                        default:
                            if (arrayTypeStr.contains("http"))
                            {
                                return SchemaDataType.OBJECT_ARRAY;
                            }
                            throw new RuntimeException("Unknown array type in " + id + ": " + arrayTypeStr + " type: " + typeStr);
                    }
                default:
                    throw new RuntimeException("Unknown type in " + id + ": " + typeStr);
            }
        }
        throw new RuntimeException("Cannot determine type of " + id);
    }

    public String getText(String url) {
        StringBuilder response = new StringBuilder();
        try {
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF8"));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
        } catch (IOException e) {
            LOGGER.error("Error processing: " + url, e);
        }
        return response.toString();
    }

    private <T> Iterable<T> iteratorToIterable(Iterator<T> iterator) {
        return () -> iterator;
    }

    public void populatePredefinedValuesForSchema(ObjectNode objectNode, String schemaUrl) {
        List<String> parts = Arrays.asList(schemaUrl.split("/"));
        if (parts.size() > 3) {
            objectNode
                    .put("describedBy", schemaUrl)
                    .put("schema_version", parts.get(parts.size() - 2))
                    .put("schema_type", parts.get(4));
        }
    }
}