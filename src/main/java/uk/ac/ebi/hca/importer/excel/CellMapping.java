package uk.ac.ebi.hca.importer.excel;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;

class CellMapping {

    final String jsonProperty;
    final CellDataType dataType;

    CellMapping(String jsonProperty, CellDataType dataType) {
        this.jsonProperty = jsonProperty;
        this.dataType = dataType;
    }

    static CellMapping map(String jsonProperty, CellDataType dataType) {
        return new CellMapping(jsonProperty, dataType);
    }

    void importTo(final ObjectNode node, final String data) {
        if (CellDataType.STRING_ARRAY.equals(dataType)) {
            ArrayNode array = node.putArray(jsonProperty);
            Arrays.stream(data.split("\\|\\|")).forEach(array::add);
        } else {
            node.put(jsonProperty, data);
        }

    }

}
