package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.asciidoctor.ast.*;

import java.util.*;
import java.util.List;

import static io.github.swagger2markup.OpenApiHelpers.*;
import static io.github.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;

public class OpenApiPathsSection {

    public static void appendPathSection(Document document, OpenAPI openAPI) {
        Paths apiPaths = openAPI.getPaths();
        SectionImpl allPathsSection = new SectionImpl(document);
        allPathsSection.setTitle(SECTION_TITLE_PATHS);

        apiPaths.forEach((name, pathItem) -> {
            pathItem.readOperationsMap().forEach(((httpMethod, operation) -> {
                SectionImpl operationSection = new SectionImpl(allPathsSection);
                operationSection.setTitle(italicUnconstrained(httpMethod.name().toUpperCase()) + " " + monospaced(name) + " " + operation.getSummary());
                appendDescription(operationSection, operation.getDescription());

                appendPathParameters(operationSection, operation.getParameters());
                appendPathResponses(operationSection, operation.getResponses());

                allPathsSection.append(operationSection);
            }));

        });

        document.append(allPathsSection);
    }

    private static void appendPathParameters(StructuralNode serverSection, List<Parameter> parameters) {
        if (null == parameters || parameters.isEmpty()) return;
        TableImpl pathParametersTable = new TableImpl(serverSection, new HashMap<>(), new ArrayList<>());
        pathParametersTable.setOption("header");
        pathParametersTable.setAttribute("caption", "", true);
        pathParametersTable.setAttribute("cols", ".^2a,.^3a,.^10a,.^5a", true);
        pathParametersTable.setTitle(TABLE_TITLE_PARAMETERS);
        pathParametersTable.setHeaderRow(TABLE_HEADER_TYPE, TABLE_HEADER_NAME, TABLE_HEADER_DESCRIPTION, TABLE_HEADER_SCHEMA);

        parameters.forEach(parameter ->
                pathParametersTable.addRow(
                        generateInnerDoc(pathParametersTable, boldUnconstrained(parameter.getIn())),
                        getParameterNameDocument(pathParametersTable, parameter),
                        generateInnerDoc(pathParametersTable, Optional.ofNullable(parameter.getDescription()).orElse("")),
                        generateSchemaDocument(pathParametersTable, parameter.getSchema())
                ));
        serverSection.append(pathParametersTable);
    }

    private static void appendPathResponses(StructuralNode serverSection, ApiResponses apiResponses) {
        if (null == apiResponses || apiResponses.isEmpty()) return;
        TableImpl pathResponsesTable = new TableImpl(serverSection, new HashMap<>(), new ArrayList<>());
        pathResponsesTable.setOption("header");
        pathResponsesTable.setAttribute("caption", "", true);
        pathResponsesTable.setAttribute("cols", ".^2a,.^10a,.^4a,.^4a", true);
        pathResponsesTable.setTitle(TABLE_TITLE_RESPONSES);
        pathResponsesTable.setHeaderRow(TABLE_HEADER_HTTP_CODE, TABLE_HEADER_DESCRIPTION, TABLE_HEADER_SCHEMA, TABLE_HEADER_LINKS);

        apiResponses.forEach((httpCode, apiResponse) -> {
            apiResponse.getLinks();
            pathResponsesTable.addRow(
                    generateInnerDoc(pathResponsesTable, httpCode),
                    getResponseDescriptionColumnDocument(pathResponsesTable, apiResponse),
                    generateInnerDoc(pathResponsesTable, TABLE_HEADER_SCHEMA),
                    generateInnerDoc(pathResponsesTable, "No Links")
            );
        });
        serverSection.append(pathResponsesTable);
    }

    private static Document getResponseDescriptionColumnDocument(Table table, ApiResponse apiResponse) {
        Document document = generateInnerDoc(table, Optional.ofNullable(apiResponse.getDescription()).orElse(""));
        Map<String, Header> headers = apiResponse.getHeaders();
        if (null != headers && !headers.isEmpty()) {
            TableImpl responseHeadersTable = new TableImpl(document, new HashMap<>(), new ArrayList<>());
            responseHeadersTable.setOption("header");
            responseHeadersTable.setAttribute("caption", "", true);
            responseHeadersTable.setAttribute("cols", ".^2a,.^14a,.^4a", true);
            responseHeadersTable.setTitle(TABLE_TITLE_HEADERS);
            responseHeadersTable.setHeaderRow(TABLE_HEADER_NAME, TABLE_HEADER_DESCRIPTION, TABLE_HEADER_SCHEMA);
            headers.forEach((name, header) ->
                    responseHeadersTable.generateRow(
                            generateInnerDoc(responseHeadersTable, name),
                            generateInnerDoc(responseHeadersTable, Optional.ofNullable(header.getDescription()).orElse("")),
                            generateSchemaDocument(responseHeadersTable, header.getSchema())
                    ));
            ParagraphBlockImpl emptyParagraph = new ParagraphBlockImpl(document);
            emptyParagraph.setSource(" ");
            document.append(emptyParagraph);
            document.append(responseHeadersTable);
        }
        return document;
    }

    private static Document getParameterNameDocument(Table table, Parameter parameter) {
        String documentContent = boldUnconstrained(parameter.getName()) + " +" + LINE_SEPARATOR +
                italicUnconstrained(parameter.getRequired() ? LABEL_REQUIRED : LABEL_OPTIONAL).toLowerCase();
        return generateInnerDoc(table, documentContent);
    }

    private static Document generateInnerDoc(Table table, String documentContent) {
        Document innerDoc = new DocumentImpl(table);
        Block paragraph = new ParagraphBlockImpl(innerDoc);
        paragraph.setSource(documentContent);
        innerDoc.append(paragraph);
        return innerDoc;
    }
}
