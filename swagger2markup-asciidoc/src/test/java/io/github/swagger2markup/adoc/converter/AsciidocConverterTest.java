package io.github.swagger2markup.adoc.converter;

import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class AsciidocConverterTest {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Parameterized.Parameters(name = "Run {index}: file={0}")
    public static Iterable<?> data() {
        return Arrays.asList(
//                "visits_service_contract.adoc",
                "simple.adoc",
//                "arrows-and-boxes-example.ad",
//                "brokeninclude.asciidoc",
//                "changeattribute.adoc",
                "chronicles-example.adoc",
                "document-with-arrays.adoc"
        );
    }

    @Parameterized.Parameter
    public String asciidocFile;


    @Test
    public void converts_asciidoc_to_asciidoc() throws IOException {
        //Given
        String originalAsciiDoc = IOUtils.toString(getClass().getResourceAsStream("/asciidoc/original/" + asciidocFile), StandardCharsets.UTF_8);
        String expectedAsciiDoc = IOUtils.toString(getClass().getResourceAsStream("/asciidoc/expected/" + asciidocFile), StandardCharsets.UTF_8);

        //When
        asciidoctor.javaConverterRegistry().register(AsciidocConverter.class, AsciidocConverter.NAME);
        String result = asciidoctor.convert(originalAsciiDoc, OptionsBuilder.options().backend(AsciidocConverter.NAME).headerFooter(false).asMap());

        //Then
        assertEquals(expectedAsciiDoc, result);
    }
}
