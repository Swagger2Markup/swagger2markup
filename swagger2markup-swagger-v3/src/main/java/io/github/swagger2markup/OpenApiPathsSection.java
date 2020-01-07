package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import java.util.*;

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
        pathParametersTable.setAttribute("cols", ".^2a,.^3a,.^9a,.^4a", true);
        pathParametersTable.setTitle(TABLE_TITLE_PARAMETERS);
        pathParametersTable.setHeaderRow(TABLE_HEADER_TYPE, TABLE_HEADER_NAME, TABLE_HEADER_DESCRIPTION, TABLE_HEADER_SCHEMA);

        parameters.forEach(parameter -> {
            pathParametersTable.addRow(
                    generateInnerDoc(boldUnconstrained(parameter.getIn())),
                    getParameterNameDocument(parameter),
                    generateInnerDoc(Optional.ofNullable(parameter.getDescription()).orElse("")),
                    generateSchemaDocument(parameter)
            );
        });
        serverSection.append(pathParametersTable);
    }

    private static Document getParameterNameDocument(Parameter parameter) {
        String documentContent = boldUnconstrained(parameter.getName()) + " +" + LINE_SEPARATOR +
                italicUnconstrained(parameter.getRequired() ? LABEL_REQUIRED : LABEL_OPTIONAL).toLowerCase();
        return generateInnerDoc(documentContent);
    }

    private static Document generateInnerDoc(String documentContent){
        Document innerDoc = new DocumentImpl();
        Block paragraph = new ParagraphBlockImpl(innerDoc);
        paragraph.setSource(documentContent);
        innerDoc.append(paragraph);
        return innerDoc;
    }
}
