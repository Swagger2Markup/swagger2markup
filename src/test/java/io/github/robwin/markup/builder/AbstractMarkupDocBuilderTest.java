package io.github.robwin.markup.builder;

import io.github.robwin.markup.builder.asciidoc.AsciiDoc;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class AbstractMarkupDocBuilderTest {

    AbstractMarkupDocBuilder builder;

    @Before
    public void setUp() {
        builder = mock(AbstractMarkupDocBuilder.class, Mockito.CALLS_REAL_METHODS);
    }

    private String normalize(String anchor) {
        return builder.normalizeAnchor(AsciiDoc.SPACE_ESCAPE, anchor);
    }

    private void assertNormalization(String result, String anchor) {
        assertEquals(result, normalize(anchor));
    }

    @Test
    public void testNormalizeAnchor() throws Exception {
        assertNormalization("", "");
        assertNormalization("anchor", "anchor");
        assertNormalization("anchor", "aNcHoR");
        assertNormalization("__anchor__", "_ anchor _");
        assertNormalization("-_anchor_-", "- anchor -");
        assertNormalization("classic-simple_anchor", "classic-simple_anchor");
        assertNormalization("an_chor", "     an    chor  ");
        assertNormalization("anchor", "#  anchor  &");
        assertNormalization(DigestUtils.md5Hex("\u0240"), "\u0240");
        assertNormalization(normalize("\u0240"), " \u0240 ");
        assertNormalization(DigestUtils.md5Hex("µu_\u0240this_-_"), "  µ&|ù \u0240This .:/-_#  ");
        assertNormalization("this_is_a_funky_string", "Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ");
        assertNormalization("", "  @#&(){}[]!$*%+=/:.;,?\\<>| ");
    }
}