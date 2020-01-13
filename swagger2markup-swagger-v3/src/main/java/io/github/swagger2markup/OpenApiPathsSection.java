package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.github.swagger2markup.adoc.ast.impl.TableImpl;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import java.util.*;

import static io.github.swagger2markup.OpenApiHelpers.*;

public class OpenApiPathsSection {

    public static void appendPathSection(Document document, Paths apiPaths) {
        if(null == apiPaths || apiPaths.isEmpty()) return;

        SectionImpl allPathsSection = new SectionImpl(document);
        allPathsSection.setTitle(SECTION_TITLE_PATHS);

        apiPaths.forEach((name, pathItem) -> {
            pathItem.readOperationsMap().forEach(((httpMethod, operation) -> {
                SectionImpl operationSection = new SectionImpl(allPathsSection);
                String summary = Optional.ofNullable(operation.getSummary()).orElse("");
                operationSection.setTitle((italicUnconstrained(httpMethod.name().toUpperCase()) + " " + monospaced(name) + " " + summary).trim());
                appendDescription(operationSection, operation.getDescription());
                appendExternalDoc(operationSection, operation.getExternalDocs());
                appendParameters(operationSection, operation.getParameters());
                appendPathResponses(operationSection, operation.getResponses());

                allPathsSection.append(operationSection);
            }));

        });

        document.append(allPathsSection);
    }

    private static void appendPathResponses(StructuralNode serverSection, ApiResponses apiResponses) {
        if (null == apiResponses || apiResponses.isEmpty()) return;
        TableImpl pathResponsesTable = new TableImpl(serverSection, new HashMap<>(), new ArrayList<>());
        pathResponsesTable.setOption("header");
        pathResponsesTable.setAttribute("caption", "", true);
        pathResponsesTable.setAttribute("cols", ".^2a,.^14a,.^4a", true);
        pathResponsesTable.setTitle(TABLE_TITLE_RESPONSES);
        pathResponsesTable.setHeaderRow(TABLE_HEADER_HTTP_CODE, TABLE_HEADER_DESCRIPTION, TABLE_HEADER_LINKS);

        apiResponses.forEach((httpCode, apiResponse) -> {
            pathResponsesTable.addRow(
                    generateInnerDoc(pathResponsesTable, httpCode),
                    getResponseDescriptionColumnDocument(pathResponsesTable, apiResponse),
                    generateLinksDocument(pathResponsesTable, apiResponse.getLinks())
            );
        });
        serverSection.append(pathResponsesTable);
    }

    private static Document getResponseDescriptionColumnDocument(Table table, ApiResponse apiResponse) {
        Document document = generateInnerDoc(table, Optional.ofNullable(apiResponse.getDescription()).orElse(""));
        appendHeadersTable(apiResponse.getHeaders(), document);
        return document;
    }
}
