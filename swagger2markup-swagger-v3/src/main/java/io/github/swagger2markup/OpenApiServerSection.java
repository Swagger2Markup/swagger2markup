package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.asciidoctor.ast.*;
import org.asciidoctor.ast.List;

import java.util.*;

import static io.github.swagger2markup.OpenApiHelpers.appendDescription;

public class OpenApiServerSection {

    public static final String HEADING_DEFAULT = "Default";
    public static final String HEADING_DESCRIPTION = "Description";
    public static final String HEADING_POSSIBLE_VALUES = "Possible Values";
    public static final String HEADING_VARIABLE = "Variable";
    public static final String TITLE_SERVERS = "Servers";
    public static final String TITLE_SERVER_VARIABLES = "Server Variables";

    public static void addServersSection(Document document, OpenAPI openAPI) {
        if (!openAPI.getServers().isEmpty()) {
            Section serversSection = new SectionImpl(document);
            serversSection.setTitle(TITLE_SERVERS);

            openAPI.getServers().forEach(server -> {
                Section serverSection = new SectionImpl(serversSection);
                serverSection.setTitle("__Server__: " + server.getUrl());

                appendDescription(serverSection, server.getDescription());
                ServerVariables variables = server.getVariables();
                if (!variables.isEmpty()) {
                    TableImpl serverVariables = new TableImpl(serverSection, new HashMap<String, Object>() {{
                        put("header-option", "");
                        put("cols", ".^2a,.^9a,.^3a,.^4a");
                    }}, new ArrayList<>());
                    serverVariables.setTitle(TITLE_SERVER_VARIABLES);

                    serverVariables.setHeaderRow(HEADING_VARIABLE, HEADING_DESCRIPTION, HEADING_POSSIBLE_VALUES, HEADING_DEFAULT);

                    variables.forEach((name, variable) -> {
                        String possibleValues = String.join(", ", Optional.ofNullable(variable.getEnum()).orElse(Collections.singletonList("Any")));
                        serverVariables.addRow(name, Optional.ofNullable(variable.getDescription()).orElse(""), possibleValues, variable.getDefault());

                    });
                    serverSection.append(serverVariables);
                }
                serversSection.append(serverSection);
            });
            document.append(serversSection);
        }
    }
}
