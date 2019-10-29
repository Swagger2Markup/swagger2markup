package io.github.swagger2markup.markup.builder.internal;

import io.github.swagger2markup.markup.builder.LineSeparator;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupDocBuilders;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import io.github.swagger2markup.markup.builder.internal.asciidoc.AsciiDoc;
import io.github.swagger2markup.markup.builder.internal.markdown.Markdown;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class AbstractMarkupDocBuilderTest {

    AbstractMarkupDocBuilder builder;

    @Before
    public void setUp() {
        builder = mock(AbstractMarkupDocBuilder.class, Mockito.CALLS_REAL_METHODS);
        builder.newLine = "\n";
        builder.documentBuilder = new StringBuilder();
    }

    private String normalize(Markup markup, String anchor) {
        return builder.normalizeAnchor(markup, anchor);
    }

    private void assertNormalization(Markup markup, String result, String anchor) {
        assertEquals(result, normalize(markup, anchor));
    }

    @Test
    public void testNormalizeAnchorAsciiDoc() throws Exception {
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "", "");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "anchor", "anchor");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "anchor", "aNcHoR");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "anchor", "_ anchor _");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "anchor", "- anchor -");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "a_n-c_h_o-r", "_-a _ - n-_-_-c_-_-_h___o---r_-");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "classic-simple_anchor", "classic-simple_anchor");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "an_chor", "     an    chor  ");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "anchor", "#  anchor  &");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, DigestUtils.md5Hex("\u0240"), "\u0240");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, normalize(AsciiDoc.SPACE_ESCAPE, "\u0240"), " \u0240 ");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, DigestUtils.md5Hex("µ_u_\u0240this"), "  µ&|ù \u0240This .:/-_#  ");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "this_is_a_really_funky_string", "Tĥïŝ ĩš â really fůňķŷ Šťŕĭńġ");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "", "  @#&(){}[]!$*%+=/:.;,?\\<>| ");
        assertNormalization(AsciiDoc.SPACE_ESCAPE, "sub_action_html_query_value", " /sub/action.html/?query=value ");
    }

    @Test
    public void testNormalizeAnchorMarkdown() throws Exception {
        assertNormalization(Markdown.SPACE_ESCAPE, "", "");
        assertNormalization(Markdown.SPACE_ESCAPE, "anchor", "anchor");
        assertNormalization(Markdown.SPACE_ESCAPE, "anchor", "aNcHoR");
        assertNormalization(Markdown.SPACE_ESCAPE, "anchor", "_ anchor _");
        assertNormalization(Markdown.SPACE_ESCAPE, "anchor", "- anchor -");
        assertNormalization(Markdown.SPACE_ESCAPE, "a-n-c_h_o-r", "_-a _ - n-_-_-c_-_-_h___o---r_-");
        assertNormalization(Markdown.SPACE_ESCAPE, "classic-simple_anchor", "classic-simple_anchor");
        assertNormalization(Markdown.SPACE_ESCAPE, "an-chor", "     an    chor  ");
        assertNormalization(Markdown.SPACE_ESCAPE, "anchor", "#  anchor  &");
        assertNormalization(Markdown.SPACE_ESCAPE, DigestUtils.md5Hex("\u0240"), "\u0240");
        assertNormalization(Markdown.SPACE_ESCAPE, normalize(Markdown.SPACE_ESCAPE, "\u0240"), " \u0240 ");
        assertNormalization(Markdown.SPACE_ESCAPE, DigestUtils.md5Hex("µ-u-\u0240this"), "  µ&|ù \u0240This .:/-_#  ");
        assertNormalization(Markdown.SPACE_ESCAPE, "this-is-a-really-funky-string", "Tĥïŝ ĩš â really fůňķŷ Šťŕĭńġ");
        assertNormalization(Markdown.SPACE_ESCAPE, "", "  @#&(){}[]!$*%+=/:.;,?\\<>| ");
        assertNormalization(Markdown.SPACE_ESCAPE, "sub-action-html-query-value", " /sub/action.html/?query=value ");
    }

    @Test
    public void testCopy() {
        MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC, LineSeparator.UNIX).withAnchorPrefix("anchor-");
        MarkupDocBuilder copy = builder.copy(false);

        Assert.assertTrue(copy instanceof AbstractMarkupDocBuilder);
        AbstractMarkupDocBuilder internalCopy = (AbstractMarkupDocBuilder) copy;
        Assert.assertEquals(LineSeparator.UNIX.toString(), internalCopy.newLine);
        Assert.assertEquals("anchor-", internalCopy.anchorPrefix);

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC, LineSeparator.WINDOWS);
        copy = builder.copy(false);

        Assert.assertTrue(copy instanceof AbstractMarkupDocBuilder);
        internalCopy = (AbstractMarkupDocBuilder) copy;
        Assert.assertEquals(LineSeparator.WINDOWS.toString(), internalCopy.newLine);
        Assert.assertNull(internalCopy.anchorPrefix);

        builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.ASCIIDOC, LineSeparator.UNIX);
        builder.text("This is text");
        copy = builder.copy(true);

        Assert.assertTrue(copy instanceof AbstractMarkupDocBuilder);
        internalCopy = (AbstractMarkupDocBuilder) copy;
        Assert.assertEquals(LineSeparator.UNIX.toString(), internalCopy.newLine);
        Assert.assertNull(internalCopy.anchorPrefix);
        Assert.assertEquals("This is text", internalCopy.documentBuilder.toString());

    }
}