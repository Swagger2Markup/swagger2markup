package io.github.swagger2markup;

import io.github.swagger2markup.adoc.converter.AsciidocConverter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.jruby.extension.internal.JRubyProcessor;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OpenApi2AsciiDoc {

    public static final String OVERVIEW_TITLE = "Overview";
    private final Asciidoctor asciidoctor;
    private final AsciidocConverter converter;
    private final HashMap<String, Object> asciiDocOpts;
    private final JRubyProcessor processor;
    private final int sectionLevel = -1;

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
        addOpenApiInfo(rootDocument, openAPI);
        return converter.convert(rootDocument, "document", new HashMap<>());
    }

    private void addOpenApiInfo(Document rootDocument, OpenAPI openAPI) {
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
        overviewDoc.setTitle(OVERVIEW_TITLE);

        addOverviewDescription(document, overviewDoc, info);
        addLicenseInfo(overviewDoc, info);
        addTermsOfServiceInfo(document, overviewDoc, info);
    }

    private void addOverviewDescription(Document document, Section overviewDoc, Info info) {
        String description = info.getDescription();
        if (StringUtils.isNotBlank(description)) {
            Block paragraph = processor.createBlock(overviewDoc, "paragraph", description);
            overviewDoc.append(paragraph);
            document.append(overviewDoc);
        }
    }

    private void addLicenseInfo(Section overviewDoc, Info info) {
        License license = info.getLicense();
        if (null != license) {
            Map<Object, Object> options = new HashMap<>();
            Section licenseInfo = processor.createSection(overviewDoc, options);
            licenseInfo.setTitle("License");
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append(license.getUrl()).append("[");
            }
            sb.append(license.getName());
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append("]");
            }
            HashMap<String, Object> attributes = new HashMap<String, Object>(){{
                put("hardbreaks-option", "");
            }};
            licenseInfo.append(processor.createBlock(licenseInfo, "paragraph", sb.toString(), attributes, options));
            overviewDoc.append(licenseInfo);
        }
    }

    private void addTermsOfServiceInfo(Document document, Section overviewDoc, Info info) {
        String termsOfService = info.getTermsOfService();
        if (StringUtils.isNotBlank(termsOfService)) {
            Block paragraph = processor.createBlock(overviewDoc, "paragraph", termsOfService);
            overviewDoc.append(paragraph);
            document.append(overviewDoc);
        }
    }
}
