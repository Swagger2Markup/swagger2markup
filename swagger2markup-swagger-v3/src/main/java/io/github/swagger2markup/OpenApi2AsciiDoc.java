package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class OpenApi2AsciiDoc {

    public static final String TITLE_LICENSE = "License";
    public static final String TITLE_SERVERS = "Servers";
    public static final String TITLE_OVERVIEW = "Overview";

    public String translate(OpenAPI openAPI) {
        Document rootDocument = new DocumentImpl();
        addInfoSection(rootDocument, openAPI);
        addServersSection(rootDocument, openAPI);
        return rootDocument.convert();
    }

    private void addServersSection(Document document, OpenAPI openAPI) {
        if (!openAPI.getServers().isEmpty()) {
            Section serversSection = new SectionImpl(document);
            serversSection.setTitle(TITLE_SERVERS);
            List uList = new ListImpl(serversSection, "ulist", null, new ArrayList<>());

            openAPI.getServers().forEach(server -> {
                ListItem listItem = new ListItemImpl(uList, "__URL__: " + server.getUrl());
                appendDescription(listItem, server.getDescription());
                ServerVariables variables = server.getVariables();
                if (!variables.isEmpty()) {
                    java.util.List<DescriptionListEntry> items = new ArrayList<>();
                    DescriptionListImpl variablesList = new DescriptionListImpl(listItem, "dlist", null, items);
                    variablesList.setTitle("Variables");

                    variables.forEach((name, variable) -> {
                        DescriptionListEntryImpl variableName = new DescriptionListEntryImpl(variablesList);
                        variableName.setDescription(new ListItemImpl(variableName, name));

                        java.util.List<String> possibleValues = variable.getEnum();
//                        if(null != possibleValues && !possibleValues.isEmpty()){
//                            Document possibleValuesDocument = processor.createDocument(document);
//                            List possibleValuesList = createList(possibleValuesDocument, "ulist", new HashMap<>(), new HashMap<>());
//                            possibleValues.forEach(possibleValue -> {
//                                ListItem possibleValueItem = processor.createListItem(possibleValuesList, possibleValue);
//                                possibleValuesList.append(possibleValueItem);
//                            });
//                            processor.createTableCell(possibleValuesColumn, possibleValuesDocument);
//                        } else {
//                            processor.createTableCell(possibleValuesColumn, "");
//                        }
//
//                        processor.createTableCell(defaultValueColumn, variable.getDefault());
//
//                        String description = variable.getDescription();
//                        if (StringUtils.isNotBlank(description)) {
//                            Document cellDocument = processor.createDocument(document);
//                            appendDescription(cellDocument, variable.getDescription());
//                            processor.createTableCell(descriptionColumn, cellDocument);
//                        } else {
//                            processor.createTableCell(descriptionColumn, "");
//                        }
                        items.add(variableName);
                    });
                    listItem.append(variablesList);
                }
                uList.append(listItem);
            });
            serversSection.append(uList);
            document.append(serversSection);
        }
    }

    private void addInfoSection(Document rootDocument, OpenAPI openAPI) {
        Info apiInfo = openAPI.getInfo();
        rootDocument.setAttribute("openapi", openAPI.getOpenapi(), true);
        addDocumentTitle(rootDocument, apiInfo);
        addAuthorInfo(rootDocument, apiInfo);
        addVersionInfo(rootDocument, apiInfo);
        addOverview(rootDocument, apiInfo);
    }

    private void addDocumentTitle(Document rootDocument, Info apiInfo) {
        String title = apiInfo.getTitle();
        if (StringUtils.isNotBlank(title)) {
            rootDocument.setTitle(title);
        }
    }

    private void addVersionInfo(Document rootDocument, Info info) {
        String version = info.getVersion();
        if (StringUtils.isNotBlank(version)) {
            rootDocument.setAttribute("revnumber", version, true);
        }
    }

    private void addAuthorInfo(Document rootDocument, Info info) {
        Contact contact = info.getContact();
        if (null != contact) {
            String author = Optional.ofNullable(contact.getName()).orElse("");
            String email = contact.getEmail();
            if (StringUtils.isNotBlank(email)) {
                rootDocument.setAttribute("email", email, true);
            }
            rootDocument.setAttribute("author", author, true);
            rootDocument.setAttribute("authorcount", 1L, true);
        }
    }

    private void addOverview(Document document, Info info) {
        Section overviewDoc = new SectionImpl(document);
        overviewDoc.setTitle(TITLE_OVERVIEW);

        appendDescription(overviewDoc, info.getDescription());
        addLicenseInfo(overviewDoc, info);
        addTermsOfServiceInfo(overviewDoc, info);
        document.append(overviewDoc);
    }

    private void addLicenseInfo(Section overviewDoc, Info info) {
        License license = info.getLicense();
        if (null != license) {
            Section licenseInfo = new SectionImpl(overviewDoc);
            licenseInfo.setTitle(TITLE_LICENSE);
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append(license.getUrl()).append("[");
            }
            sb.append(license.getName());
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append("]");
            }
            BlockImpl paragraph = new BlockImpl(licenseInfo, "paragraph",
                    new HashMap<String, Object>() {{
                        put("hardbreaks-option", "");
                    }});
            paragraph.setSource(sb.toString());
            licenseInfo.append(paragraph);
            overviewDoc.append(licenseInfo);
        }
    }

    private void addTermsOfServiceInfo(Section overviewDoc, Info info) {
        String termsOfService = info.getTermsOfService();
        if (StringUtils.isNotBlank(termsOfService)) {
            Block paragraph = new BlockImpl(overviewDoc, "paragraph");
            paragraph.setSource(termsOfService);
            overviewDoc.append(paragraph);
        }
    }

    private void appendDescription(StructuralNode node, String description) {
        if (StringUtils.isNotBlank(description)) {
            Block paragraph = new BlockImpl(node, "paragraph");
            paragraph.setSource(description);
            node.append(paragraph);
        }
    }
}
