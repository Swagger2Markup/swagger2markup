package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.github.swagger2markup.adoc.ast.impl.TableImpl;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import java.util.*;

import static io.github.swagger2markup.OpenApiHelpers.*;

public class OpenApiServerSection {

    public static void addServersSection(StructuralNode node, List<Server> servers) {
        if (null == servers || servers.isEmpty()) return;

        Section serversSection = new SectionImpl(node);
        serversSection.setTitle(SECTION_TITLE_SERVERS);

        servers.forEach(server -> {
            Section serverSection = new SectionImpl(serversSection);
            serverSection.setTitle(italicUnconstrained(LABEL_SERVER) + ": " + server.getUrl());

            appendDescription(serverSection, server.getDescription());
            ServerVariables variables = server.getVariables();
            appendVariables(serverSection, variables);
            serversSection.append(serverSection);
        });
        node.append(serversSection);
    }

    private static void appendVariables(Section serverSection, ServerVariables variables) {
        if (null == variables || variables.isEmpty()) return;

        TableImpl serverVariables = new TableImpl(serverSection, new HashMap<String, Object>() {{
                put("header-option", "");
                put("cols", ".^2a,.^9a,.^3a,.^4a");
            }}, new ArrayList<>());
        serverVariables.setTitle(TABLE_TITLE_SERVER_VARIABLES);

        serverVariables.setHeaderRow(TABLE_HEADER_VARIABLE, TABLE_HEADER_DESCRIPTION, TABLE_HEADER_POSSIBLE_VALUES, TABLE_HEADER_DEFAULT);

        variables.forEach((name, variable) -> {
                String possibleValues = String.join(", ", Optional.ofNullable(variable.getEnum()).orElse(Collections.singletonList("Any")));
                serverVariables.addRow(name, Optional.ofNullable(variable.getDescription()).orElse(""), possibleValues, variable.getDefault());

            });
        serverSection.append(serverVariables);
    }
}
