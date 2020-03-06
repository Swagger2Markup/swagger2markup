package io.github.swagger2markup.internal.document;

import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.github.swagger2markup.adoc.ast.impl.TableImpl;
import io.github.swagger2markup.extension.MarkupComponent;
import io.github.swagger2markup.internal.component.ExternalDocumentationComponent;
import io.github.swagger2markup.internal.component.ParametersComponent;
import io.github.swagger2markup.internal.component.ResponseComponent;
import io.github.swagger2markup.internal.component.SecurityRequirementTableComponent;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import java.util.*;

import static io.github.swagger2markup.config.OpenAPILabels.*;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.*;

public class PathsDocument extends MarkupComponent<Document, PathsDocument.Parameters, Document> {
    private final ParametersComponent parametersComponent;
    private final ExternalDocumentationComponent externalDocumentationComponent;
    private final ResponseComponent responseComponent;
    private final SecurityRequirementTableComponent securityRequirementTableComponent;

    public PathsDocument(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.parametersComponent = new ParametersComponent(context);
        this.externalDocumentationComponent = new ExternalDocumentationComponent(context);
        this.responseComponent = new ResponseComponent(context);
        this.securityRequirementTableComponent = new SecurityRequirementTableComponent(context);
    }

    public static Parameters parameters(OpenAPI schema) {
        return new Parameters(schema);
    }

    @Override
    public Document apply(Document document, Parameters parameters) {
        Paths apiPaths = parameters.schema.getPaths();

        if (null == apiPaths || apiPaths.isEmpty()) return document;

        SectionImpl allPathsSection = new SectionImpl(document);
        allPathsSection.setTitle(labels.getLabel(SECTION_TITLE_PATHS));

        apiPaths.forEach((name, pathItem) ->
                pathItem.readOperationsMap().forEach(((httpMethod, operation) -> {
                    SectionImpl operationSection = new SectionImpl(allPathsSection);
                    String summary = Optional.ofNullable(operation.getSummary()).orElse("");
                    operationSection.setTitle((italicUnconstrained(httpMethod.name().toUpperCase()) + " " + monospaced(name) + " " + summary).trim());
                    appendDescription(operationSection, operation.getDescription());
                    externalDocumentationComponent.apply(operationSection, operation.getExternalDocs());
                    parametersComponent.apply(operationSection, operation.getParameters());
                    responseComponent.apply(operationSection, operation.getResponses());
                    appendServersSection(operationSection, operation.getServers());
                    securityRequirementTableComponent.apply(operationSection, operation.getSecurity(), false);
                    allPathsSection.append(operationSection);
                })));

        document.append(allPathsSection);
        return document;
    }

    private void appendServersSection(StructuralNode node, List<Server> servers) {
        if (null == servers || servers.isEmpty()) return;

        Section serversSection = new SectionImpl(node);
        serversSection.setTitle(labels.getLabel(SECTION_TITLE_SERVERS));

        servers.forEach(server -> {
            Section serverSection = new SectionImpl(serversSection);
            serverSection.setTitle(italicUnconstrained(labels.getLabel(LABEL_SERVER)) + ": " + server.getUrl());

            appendDescription(serverSection, server.getDescription());
            ServerVariables variables = server.getVariables();
            appendVariables(serverSection, variables);
            serversSection.append(serverSection);
        });
        node.append(serversSection);
    }

    private void appendVariables(Section serverSection, ServerVariables variables) {
        if (null == variables || variables.isEmpty()) return;

        TableImpl serverVariables = new TableImpl(serverSection, new HashMap<String, Object>() {{
            put("header-option", "");
            put("cols", ".^2a,.^9a,.^3a,.^4a");
        }}, new ArrayList<>());
        serverVariables.setTitle(labels.getLabel(TABLE_TITLE_SERVER_VARIABLES));

        serverVariables.setHeaderRow(labels.getLabel(TABLE_HEADER_VARIABLE), labels.getLabel(TABLE_HEADER_DESCRIPTION),
                labels.getLabel(TABLE_HEADER_POSSIBLE_VALUES), labels.getLabel(TABLE_HEADER_DEFAULT)
        );

        variables.forEach((name, variable) -> {
            String possibleValues = String.join(", ", Optional.ofNullable(variable.getEnum()).orElse(Collections.singletonList("Any")));
            serverVariables.addRow(name, Optional.ofNullable(variable.getDescription()).orElse(""), possibleValues, variable.getDefault());

        });
        serverSection.append(serverVariables);
    }

    public static class Parameters {
        private final OpenAPI schema;

        public Parameters(OpenAPI schema) {
            this.schema = Validate.notNull(schema, "Schema must not be null");
        }
    }
}
