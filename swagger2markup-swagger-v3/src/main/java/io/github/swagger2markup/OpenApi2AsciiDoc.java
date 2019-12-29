package io.github.swagger2markup;

import io.github.swagger2markup.adoc.converter.AsciidocConverter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.*;
import org.asciidoctor.jruby.ast.impl.ContentNodeImpl;
import org.asciidoctor.jruby.ast.impl.NodeConverter;
import org.asciidoctor.jruby.extension.internal.JRubyProcessor;
import org.asciidoctor.jruby.internal.JRubyRuntimeContext;
import org.asciidoctor.jruby.internal.RubyHashUtil;
import org.asciidoctor.jruby.internal.RubyUtils;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OpenApi2AsciiDoc {

    public static final String TITLE_LICENSE = "License";
    public static final String TITLE_SERVERS = "Servers";
    public static final String TITLE_OVERVIEW = "Overview";
    private final Asciidoctor asciidoctor;
    private final AsciidocConverter converter;
    private final HashMap<String, Object> asciiDocOpts;
    private final JRubyProcessor processor;

    public OpenApi2AsciiDoc(Asciidoctor asciidoctor) {
        asciiDocOpts = new HashMap<String, Object>() {{
            put("backend", AsciidocConverter.NAME);
        }};
        this.asciidoctor = asciidoctor;
        this.asciidoctor.javaConverterRegistry().register(AsciidocConverter.class, AsciidocConverter.NAME);
        converter = new AsciidocConverter(AsciidocConverter.NAME, new HashMap<>());
        processor = new JRubyProcessor();
    }

    public String translate(OpenAPI openAPI) {
        Document rootDocument = asciidoctor.load("", asciiDocOpts);
        addInfoSection(rootDocument, openAPI);
        addServersSection(rootDocument, openAPI);
        return converter.convert(rootDocument, "document", new HashMap<>());
    }

    private void addServersSection(Document document, OpenAPI openAPI) {
        if (!openAPI.getServers().isEmpty()) {
            Section serversSection = processor.createSection(document);
            serversSection.setTitle(TITLE_SERVERS);
            processor.parseContent(serversSection, new java.util.ArrayList<>());
            List uList = createList(serversSection, "ulist", new HashMap<>(), new HashMap<>());

            openAPI.getServers().forEach(server -> {
                ListItem listItem = processor.createListItem(uList, "__URL__: " + server.getUrl());
                appendDescription(listItem, server.getDescription());
                ServerVariables variables = server.getVariables();
//                Table table = processor.createTable(listItem, new HashMap<String, Object>(){{
//                    put("cols", "<,a,<,a");
//                }});
                if (!variables.isEmpty()) {
                    Table tmpTable = processor.createTable(listItem);
//                    Row tableRow = processor.createTableRow(tmpTable);
                    Column nameColumn = processor.createTableColumn(tmpTable, 0);
                    Column possibleValuesColumn = processor.createTableColumn(tmpTable, 1);
                    Column defaultValueColumn = processor.createTableColumn(tmpTable, 2);
                    Column descriptionColumn = processor.createTableColumn(tmpTable, 3);


                    Table variablesTable = processor.createTable(listItem);
                    variablesTable.setCaption("Variables");

                    Cell nameHeader = processor.createTableCell(nameColumn, "Name");
                    Cell possibleValuesHeader = processor.createTableCell(possibleValuesColumn, "Possible Values");
                    Cell defaultValueHeader = processor.createTableCell(defaultValueColumn, "Default");
                    Cell descriptionHeader = processor.createTableCell(descriptionColumn, "Description");

                    java.util.List<Cell> headerRow = Arrays.asList(nameHeader, possibleValuesHeader, defaultValueHeader, descriptionHeader);


                    variables.forEach((name, variable) -> {
                        processor.createTableCell(nameColumn, name);

                        java.util.List<String> possibleValues = variable.getEnum();
                        if(null != possibleValues && !possibleValues.isEmpty()){
                            Document possibleValuesDocument = processor.createDocument(document);
                            List possibleValuesList = createList(possibleValuesDocument, "ulist", new HashMap<>(), new HashMap<>());
                            possibleValues.forEach(possibleValue -> {
                                ListItem possibleValueItem = processor.createListItem(possibleValuesList, possibleValue);
                                possibleValuesList.append(possibleValueItem);
                            });
                            processor.createTableCell(possibleValuesColumn, possibleValuesDocument);
                        } else {
                            processor.createTableCell(possibleValuesColumn, "");
                        }

                        processor.createTableCell(defaultValueColumn, variable.getDefault());

                        String description = variable.getDescription();
                        if (StringUtils.isNotBlank(description)) {
                            Document cellDocument = processor.createDocument(document);
                            appendDescription(cellDocument, variable.getDescription());
                            processor.createTableCell(descriptionColumn, cellDocument);
                        } else {
                            processor.createTableCell(descriptionColumn, "");
                        }
                    });

                    listItem.append(variablesTable);
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
                author += " <" + email + ">";
            }
            rootDocument.setAttribute("authors", author, true);
        }
    }

    private void addOverview(Document document, Info info) {
        Section overviewDoc = processor.createSection(document);
        overviewDoc.setTitle(TITLE_OVERVIEW);

        appendDescription(overviewDoc, info.getDescription());
        addLicenseInfo(overviewDoc, info);
        addTermsOfServiceInfo(document, overviewDoc, info);
        document.append(overviewDoc);
    }

    private void addLicenseInfo(Section overviewDoc, Info info) {
        License license = info.getLicense();
        if (null != license) {
            Map<Object, Object> options = new HashMap<>();
            Section licenseInfo = processor.createSection(overviewDoc, options);
            licenseInfo.setTitle(TITLE_LICENSE);
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append(license.getUrl()).append("[");
            }
            sb.append(license.getName());
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append("]");
            }
            licenseInfo.append(processor.createBlock(licenseInfo, "paragraph", sb.toString(),
                    new HashMap<String, Object>() {{
                        put("hardbreaks-option", "");
                    }}, options));
            overviewDoc.append(licenseInfo);
        }
    }

    private void addTermsOfServiceInfo(Document document, Section overviewDoc, Info info) {
        String termsOfService = info.getTermsOfService();
        if (StringUtils.isNotBlank(termsOfService)) {
            Block paragraph = processor.createBlock(overviewDoc, "paragraph", termsOfService);
            overviewDoc.append(paragraph);
        }
    }

    private void appendDescription(StructuralNode node, String description) {
        if (StringUtils.isNotBlank(description)) {
            Block paragraph = processor.createBlock(node, "paragraph", description);
            node.append(paragraph);
        }
    }

    public List createList(ContentNode parent, String context, Map<String, Object> attributes, Map<String, Object> options) {
        Ruby rubyRuntime = JRubyRuntimeContext.get(parent);

        options.put(Options.ATTRIBUTES, RubyHashUtil.convertMapToRubyHashWithStrings(rubyRuntime, attributes));

        RubyHash convertedOptions = RubyHashUtil.convertMapToRubyHashWithSymbols(rubyRuntime, options);

        IRubyObject[] parameters = {
                ((ContentNodeImpl) parent).getRubyObject(),
                RubyUtils.toSymbol(rubyRuntime, context),
                convertedOptions};
        return (List) NodeConverter.createASTNode(rubyRuntime, NodeConverter.NodeType.LIST_CLASS, parameters);
    }
}
