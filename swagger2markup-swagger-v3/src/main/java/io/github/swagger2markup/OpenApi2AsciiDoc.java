package io.github.swagger2markup;

import io.github.swagger2markup.adoc.converter.AsciidocConverter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.jruby.extension.internal.JRubyProcessor;
import org.asciidoctor.jruby.internal.JRubyAsciidoctor;

import java.util.HashMap;
import java.util.Optional;

public class OpenApi2AsciiDoc {

    public static final String OVERVIEW_TITLE = "Overview";
    public static final String OVERVIEW_ID = "_overview";
    private final Asciidoctor asciidoctor;
    private final AsciidocConverter converter;
    private final HashMap<String, Object> asciiDocOpts;
    private final JRubyProcessor processor;

    public OpenApi2AsciiDoc(Asciidoctor asciidoctor) {
        asciiDocOpts = new HashMap<String, Object>(){{
            put("backend", AsciidocConverter.NAME);
        }};
        this.asciidoctor = asciidoctor;
        this.asciidoctor.javaConverterRegistry().register(AsciidocConverter.class, AsciidocConverter.NAME);
        converter =  new AsciidocConverter(AsciidocConverter.NAME, asciiDocOpts);
        processor = new JRubyProcessor();
//        processor.setAsciidoctor(asciidoctor);
    }

    public String translate(OpenAPI openAPI) {
        Document rootDocument = asciidoctor.load("", asciiDocOpts);
        rootDocument.isBasebackend("");
        Info apiInfo = openAPI.getInfo();
        addDocumentTitle(rootDocument, apiInfo);
        addAuthorInfo(rootDocument, apiInfo);
        addVersionInfo(rootDocument, apiInfo);
        addOverview(rootDocument, apiInfo);
        return converter.convert(rootDocument, "document", new HashMap<>());
    }

    private void addDocumentTitle(Document rootDocument, Info apiInfo) {
        String title = apiInfo.getTitle();
        if(StringUtils.isNotBlank(title)) {
            rootDocument.setTitle(title);
        }
    }

    private void addVersionInfo(Document rootDocument, Info info){
        String version = info.getVersion();
        if(StringUtils.isNotBlank(version)) {
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
        String description = info.getDescription();
        if(StringUtils.isNotBlank(description)){
            Block overviewDoc = processor.createBlock(document, "preamble", description);
            overviewDoc.setTitle(OVERVIEW_TITLE);
            overviewDoc.setId(OVERVIEW_ID);
//            overviewDoc.
            document.append(overviewDoc);
        }
    }

}
