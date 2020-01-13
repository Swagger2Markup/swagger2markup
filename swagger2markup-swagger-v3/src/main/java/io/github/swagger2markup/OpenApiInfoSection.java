package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.BlockImpl;
import io.github.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Optional;

import static io.github.swagger2markup.OpenApiHelpers.*;

public class OpenApiInfoSection {

    public static void addInfoSection(Document rootDocument, OpenAPI openAPI) {
        Info apiInfo = openAPI.getInfo();
        rootDocument.setAttribute("openapi", openAPI.getOpenapi(), true);
        addDocumentTitle(rootDocument, apiInfo);
        addAuthorInfo(rootDocument, apiInfo);
        addVersionInfo(rootDocument, apiInfo);
        appendOverview(rootDocument, apiInfo);
        appendExternalDoc(rootDocument, openAPI.getExternalDocs());
    }

    public static void addDocumentTitle(Document rootDocument, Info apiInfo) {
        String title = apiInfo.getTitle();
        if (StringUtils.isNotBlank(title)) {
            rootDocument.setTitle(title);
        }
    }

    public static void addVersionInfo(Document rootDocument, Info info) {
        String version = info.getVersion();
        if (StringUtils.isNotBlank(version)) {
            rootDocument.setAttribute("revnumber", version, true);
        }
    }

    public static void addAuthorInfo(Document rootDocument, Info info) {
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

    public static void appendOverview(Document document, Info info) {
        Section overviewDoc = new SectionImpl(document);
        overviewDoc.setTitle(SECTION_TITLE_OVERVIEW);

        appendDescription(overviewDoc, info.getDescription());
        appendTermsOfServiceInfo(overviewDoc, info);
        appendLicenseInfo(overviewDoc, info);
        document.append(overviewDoc);
    }

    public static void appendLicenseInfo(Section overviewDoc, Info info) {
        License license = info.getLicense();
        if (null != license) {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append(license.getUrl()).append("[");
            }
            sb.append(license.getName());
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append("]");
            }
            BlockImpl paragraph = new ParagraphBlockImpl(overviewDoc);
            paragraph.setSource(sb.toString());
            overviewDoc.append(paragraph);
        }
    }

    public static void appendTermsOfServiceInfo(Section overviewDoc, Info info) {
        String termsOfService = info.getTermsOfService();
        if (StringUtils.isNotBlank(termsOfService)) {
            Block paragraph = new ParagraphBlockImpl(overviewDoc);
            paragraph.setSource(termsOfService + "[" + LABEL_TERMS_OF_SERVICE + "]");
            overviewDoc.append(paragraph);
        }
    }
}
