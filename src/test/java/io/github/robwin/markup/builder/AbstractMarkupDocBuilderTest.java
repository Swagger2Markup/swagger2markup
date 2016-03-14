package io.github.robwin.markup.builder;

import io.github.robwin.markup.builder.asciidoc.AsciiDoc;
import io.github.robwin.markup.builder.markdown.Markdown;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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

    private void assertImportMarkupAsciiDoc(String expected, String text, int levelOffset) throws IOException {
        builder.documentBuilder = new StringBuilder();
        builder.importMarkupStyle1(Pattern.compile(String.format("^(%s{1,%d})\\s+(.*)$", AsciiDoc.TITLE, AbstractMarkupDocBuilder.MAX_TITLE_LEVEL + 1)), AsciiDoc.TITLE, new StringReader(text), levelOffset);
        assertEquals(expected, builder.documentBuilder.toString());
    }

    private void assertImportMarkupExceptionAsciiDoc(String expected, String text, int levelOffset) throws IOException {
        builder.documentBuilder = new StringBuilder();
        try {
            assertImportMarkupAsciiDoc(expected, text, levelOffset);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testImportMarkupAsciiDoc() throws IOException {
        assertImportMarkupAsciiDoc("\n\n", "", 0);
        assertImportMarkupAsciiDoc("\n\n", "", 4);
        assertImportMarkupExceptionAsciiDoc("Specified levelOffset (6) > max levelOffset (5)", "", 6);
        assertImportMarkupAsciiDoc("\n\n", "", -4);
        assertImportMarkupExceptionAsciiDoc("Specified levelOffset (-6) < min levelOffset (-5)", "", -6);

        assertImportMarkupAsciiDoc("\n= title\nline 1\nline 2\n\n", "=   title\r\nline 1\r\nline 2", 0);

        assertImportMarkupAsciiDoc("\nline 1\nline 2\n\n", "line 1\nline 2", 0);
        assertImportMarkupAsciiDoc("\nline 1\nline 2\n\n", "line 1\nline 2", 4);

        assertImportMarkupAsciiDoc("\n= title\nline 1\nline 2\n= title 2\nline 3\n\n", "= title\nline 1\nline 2\n= title 2\nline 3", 0);
        assertImportMarkupAsciiDoc("\n===== title\nline 1\nline 2\n\n", "= title\nline 1\nline 2", 4);
        assertImportMarkupAsciiDoc("\n= title\nline 1\nline 2\n\n", "===== title\nline 1\nline 2", -4);

        assertImportMarkupExceptionAsciiDoc("Specified levelOffset (5) set title 'title' level (1) > max title level (5)", "== title\nline 1\nline 2", 5);
        assertImportMarkupExceptionAsciiDoc("Specified levelOffset (-1) set title 'title' level (0) < 0", "= title\nline 1\nline 2", -1);
        assertImportMarkupExceptionAsciiDoc("Specified levelOffset (-3) set title 'title' level (1) < 0", "== title\nline 1\nline 2", -3);
    }

    private void assertImportMarkupMarkdown(String expected, String text, int levelOffset) throws IOException {
        builder.documentBuilder = new StringBuilder();
        builder.importMarkupStyle1(Pattern.compile(String.format("^(%s{1,%d})\\s+(.*)$", Markdown.TITLE, AbstractMarkupDocBuilder.MAX_TITLE_LEVEL + 1)), Markdown.TITLE, new StringReader(text), levelOffset);
        assertEquals(expected, builder.documentBuilder.toString());
    }

    private void assertImportMarkupExceptionMarkdown(String expected, String text, int levelOffset) throws IOException {
        builder.documentBuilder = new StringBuilder();
        try {
            assertImportMarkupMarkdown(expected, text, levelOffset);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testImportMarkupMarkdown() throws IOException {
        assertImportMarkupMarkdown("\n\n", "", 0);
        assertImportMarkupMarkdown("\n\n", "", 4);
        assertImportMarkupMarkdown("\n\n", "", -4);
        assertImportMarkupExceptionMarkdown("Specified levelOffset (6) > max levelOffset (5)", "", 6);
        assertImportMarkupExceptionMarkdown("Specified levelOffset (-6) < min levelOffset (-5)", "", -6);

        assertImportMarkupMarkdown("\n# title\nline 1\nline 2\n\n", "#   title\r\nline 1\r\nline 2", 0);

        assertImportMarkupMarkdown("\nline 1\nline 2\n\n", "line 1\nline 2", 0);
        assertImportMarkupMarkdown("\nline 1\nline 2\n\n", "line 1\nline 2", 4);

        assertImportMarkupMarkdown("\n# title\nline 1\nline 2\n# title 2\nline 3\n\n", "# title\nline 1\nline 2\n# title 2\nline 3", 0);
        assertImportMarkupMarkdown("\n##### title\nline 1\nline 2\n\n", "# title\nline 1\nline 2", 4);
        assertImportMarkupMarkdown("\n# title\nline 1\nline 2\n\n", "##### title\nline 1\nline 2", -4);

        assertImportMarkupExceptionMarkdown("Specified levelOffset (5) set title 'title' level (1) > max title level (5)", "## title\nline 1\nline 2", 5);
        assertImportMarkupExceptionMarkdown("Specified levelOffset (-1) set title 'title' level (0) < 0", "# title\nline 1\nline 2", -1);
        assertImportMarkupExceptionMarkdown("Specified levelOffset (-3) set title 'title' level (1) < 0", "## title\nline 1\nline 2", -3);
    }

    private void assertImportMarkupConfluenceMarkup(String expected, String text, int levelOffset) throws IOException {
        builder.documentBuilder = new StringBuilder();
        builder.importMarkupStyle2(Pattern.compile("^h([0-9])\\.\\s+(.*)$"), "h%d. %s", false, new StringReader(text), levelOffset);
        assertEquals(expected, builder.documentBuilder.toString());
    }

    private void assertImportMarkupExceptionConfluenceMarkup(String expected, String text, int levelOffset) throws IOException {
        builder.documentBuilder = new StringBuilder();
        try {
            assertImportMarkupConfluenceMarkup(expected, text, levelOffset);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testImportMarkupConfluenceMarkup() throws IOException {
        assertImportMarkupConfluenceMarkup("\n\n", "", 0);
        assertImportMarkupConfluenceMarkup("\n\n", "", 4);
        assertImportMarkupConfluenceMarkup("\n\n", "", -4);
        assertImportMarkupExceptionConfluenceMarkup("Specified levelOffset (6) > max levelOffset (5)", "", 6);
        assertImportMarkupExceptionConfluenceMarkup("Specified levelOffset (-6) < min levelOffset (-5)", "", -6);

        assertImportMarkupConfluenceMarkup("\nh1. title\nline 1\nline 2\n\n", "h1.   title\r\nline 1\r\nline 2", 0);

        assertImportMarkupConfluenceMarkup("\nline 1\nline 2\n\n", "line 1\nline 2", 0);
        assertImportMarkupConfluenceMarkup("\nline 1\nline 2\n\n", "line 1\nline 2", 4);

        assertImportMarkupConfluenceMarkup("\nh1. title\nline 1\nline 2\nh1. title 2\nline 3\n\n", "h1. title\nline 1\nline 2\nh1. title 2\nline 3", 0);
        assertImportMarkupConfluenceMarkup("\nh5. title\nline 1\nline 2\n\n", "h1. title\nline 1\nline 2", 4);
        assertImportMarkupConfluenceMarkup("\nh1. title\nline 1\nline 2\n\n", "h5. title\nline 1\nline 2", -4);

        assertImportMarkupExceptionConfluenceMarkup("Specified levelOffset (5) set title 'title' level (1) > max title level (5)", "h2. title\nline 1\nline 2", 5);
        assertImportMarkupExceptionConfluenceMarkup("Specified levelOffset (-1) set title 'title' level (0) < 0", "h1. title\nline 1\nline 2", -1);
        assertImportMarkupExceptionConfluenceMarkup("Specified levelOffset (-3) set title 'title' level (1) < 0", "h2. title\nline 1\nline 2", -3);
    }
}