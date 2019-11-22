package io.github.swagger2markup.adoc.ast;

import org.asciidoctor.ast.*;

import java.util.List;
import java.util.Map;

public class DocumentImpl implements Document {
    /**
     * @return The Title structure for this document.
     * @see Title
     */
    @Override
    public Title getStructuredDoctitle() {
        return null;
    }

    /**
     * @return The title as a String.
     * @see Title
     */
    @Override
    public String getDoctitle() {
        return null;
    }

    /**
     * @return The title as a String.
     * @see Title
     * @deprecated Please use {@link #getDoctitle()}
     */
    @Override
    public String doctitle() {
        return null;
    }

    /**
     * @param backend
     * @return basebackend attribute value
     */
    @Override
    public boolean isBasebackend(String backend) {
        return false;
    }

    /**
     * @param backend
     * @return basebackend attribute value
     * @deprecated Please use {@link #isBasebackend(String)}
     */
    @Override
    public boolean basebackend(String backend) {
        return false;
    }

    /**
     * @return options defined in document.
     */
    @Override
    public Map<Object, Object> getOptions() {
        return null;
    }

    /**
     * Gets the current counter with the given name and increases its value.
     * At the first invocation the counter will return 1.
     * After the call the value of the counter is set to the returned value plus 1.
     *
     * @param name
     * @return
     */
    @Override
    public int getAndIncrementCounter(String name) {
        return 0;
    }

    /**
     * Gets the current counter with the given name and increases its value.
     * At the first invocation the counter will return the given initial value.
     * After the call the value of the counter is set to the returned value plus 1.
     *
     * @param name
     * @param initialValue
     * @return
     */
    @Override
    public int getAndIncrementCounter(String name, int initialValue) {
        return 0;
    }

    /**
     * @return Whether the sourcemap is enabled.
     */
    @Override
    public boolean isSourcemap() {
        return false;
    }

    /**
     * Toggles the sourcemap option.
     * <p>
     * This method must be called before the document is parsed, such as
     * from a Preprocessor extension. Otherwise, it has no effect.
     *
     * @param state The state in which to put the sourcemap (true = on, false = off).
     */
    @Override
    public void setSourcemap(boolean state) {

    }

    /**
     * @deprecated Please use {@linkplain #getTitle()} instead
     */
    @Override
    public String title() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public String getCaption() {
        return null;
    }

    @Override
    public void setCaption(String caption) {

    }

    /**
     * @deprecated Please use {@linkplain #getStyle()} instead
     */
    @Override
    public String style() {
        return null;
    }

    @Override
    public String getStyle() {
        return null;
    }

    @Override
    public void setStyle(String style) {

    }

    /**
     * @return The list of child blocks of this block
     * @deprecated Please use {@linkplain #getBlocks()} instead
     */
    @Override
    public List<StructuralNode> blocks() {
        return null;
    }

    /**
     * @return The list of child blocks of this block
     */
    @Override
    public List<StructuralNode> getBlocks() {
        return null;
    }

    /**
     * Appends a new child block as the last block to this block.
     *
     * @param block The new child block added as last child to this block.
     */
    @Override
    public void append(StructuralNode block) {

    }

    /**
     * @deprecated Please use {@linkplain #getContent()} instead
     */
    @Override
    public Object content() {
        return null;
    }

    @Override
    public Object getContent() {
        return null;
    }

    @Override
    public String convert() {
        return null;
    }

    @Override
    public List<StructuralNode> findBy(Map<Object, Object> selector) {
        return null;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    /**
     * Returns the content model.
     *
     * @return the content model
     * @see ContentModel
     */
    @Override
    public String getContentModel() {
        return null;
    }

    /**
     * Returns the source location of this block.
     * This information is only available if the {@code sourcemap} option is enabled when loading or rendering the document.
     *
     * @return the source location of this block or {@code null} if the {@code sourcemap} option is not enabled when loading the document.
     */
    @Override
    public Cursor getSourceLocation() {
        return null;
    }

    /**
     * Returns the list of enabled substitutions.
     *
     * @return A list of substitutions enabled for this node, e.g. <code>["specialcharacters", "quotes", "attributes", "replacements", "macros", "post_replacements"]</code> for paragraphs.
     * @see <a href="http://asciidoctor.org/docs/user-manual/#subs">Asciidoctor User Manual</a>
     */
    @Override
    public List<String> getSubstitutions() {
        return null;
    }

    /**
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_POST_REPLACEMENTS}
     * @return <code>true</code> if the name of the given substitution is enabled.
     * @see <a href="http://asciidoctor.org/docs/user-manual/#subs">Asciidoctor User Manual</a>
     */
    @Override
    public boolean isSubstitutionEnabled(String substitution) {
        return false;
    }

    /**
     * Removes the given substitution from this node.
     *
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_QUOTES}
     * @see <a href="http://asciidoctor.org/docs/user-manual/#subs">Asciidoctor User Manual</a>
     */
    @Override
    public void removeSubstitution(String substitution) {

    }

    /**
     * Adds the given substitution to this node at the end of the substitution list.
     *
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_MACROS}
     * @see <a href="http://asciidoctor.org/docs/user-manual/#subs">Asciidoctor User Manual</a>
     */
    @Override
    public void addSubstitution(String substitution) {

    }

    /**
     * Adds the given substitution to this node at the beginning of the substitution list.
     *
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_ATTRIBUTES}
     * @see <a href="http://asciidoctor.org/docs/user-manual/#subs">Asciidoctor User Manual</a>
     */
    @Override
    public void prependSubstitution(String substitution) {

    }

    /**
     * Sets the given substitutions on this node overwriting all other substitutions.
     *
     * @param substitution the name of a substitution, e.g. {@link #SUBSTITUTION_SPECIAL_CHARACTERS}
     * @see <a href="http://asciidoctor.org/docs/user-manual/#subs">Asciidoctor User Manual</a>
     */
    @Override
    public void setSubstitutions(String... substitution) {

    }

    /**
     * @return A unique ID for this node
     * @deprecated Please use {@link #getId()}
     */
    @Override
    public String id() {
        return null;
    }

    /**
     * @return A unique ID for this node
     */
    @Override
    public String getId() {
        return null;
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public String getNodeName() {
        return null;
    }

    /**
     * @deprecated Use {@linkplain #getParent()}  instead.
     */
    @Override
    public ContentNode parent() {
        return null;
    }

    @Override
    public ContentNode getParent() {
        return null;
    }

    /**
     * @deprecated Use {@linkplain #getContext()}  instead.
     */
    @Override
    public String context() {
        return null;
    }

    @Override
    public String getContext() {
        return null;
    }

    /**
     * @deprecated Use {@linkplain #getDocument()}  instead.
     */
    @Override
    public Document document() {
        return null;
    }

    @Override
    public Document getDocument() {
        return null;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    /**
     * @param name
     * @param defaultValue
     * @param inherit
     * @return
     * @deprecated Use {@link #getAttribute(Object, Object, boolean)} instead
     */
    @Override
    public Object getAttr(Object name, Object defaultValue, boolean inherit) {
        return null;
    }

    /**
     * @param name
     * @param defaultValue
     * @return
     * @deprecated Use {@link #getAttribute(Object, Object)} instead
     */
    @Override
    public Object getAttr(Object name, Object defaultValue) {
        return null;
    }

    /**
     * @param name
     * @return
     * @deprecated Use {@link #getAttribute(Object)} instead
     */
    @Override
    public Object getAttr(Object name) {
        return null;
    }

    @Override
    public Object getAttribute(Object name, Object defaultValue, boolean inherit) {
        return null;
    }

    @Override
    public Object getAttribute(Object name, Object defaultValue) {
        return null;
    }

    @Override
    public Object getAttribute(Object name) {
        return null;
    }

    /**
     * @param name
     * @return {@code true} if this node or the document has an attribute with the given name
     * @deprecated Use {@link #hasAttribute(Object)} instead
     */
    @Override
    public boolean hasAttr(Object name) {
        return false;
    }

    /**
     * @param name
     * @param inherited
     * @return {@code true} if the current node or depending on the inherited parameter the document has an attribute with the given name.
     * @deprecated Use {@link #hasAttribute(Object, boolean)} instead
     */
    @Override
    public boolean hasAttr(Object name, boolean inherited) {
        return false;
    }

    /**
     * @param name
     * @return {@code true} if this node or the document has an attribute with the given name
     */
    @Override
    public boolean hasAttribute(Object name) {
        return false;
    }

    /**
     * @param name
     * @param inherited
     * @return {@code true} if the current node or depending on the inherited parameter the document has an attribute with the given name.
     */
    @Override
    public boolean hasAttribute(Object name, boolean inherited) {
        return false;
    }

    /**
     * @param name
     * @param expected
     * @return
     * @deprecated Use {@link #isAttribute(Object, Object)} instead.
     */
    @Override
    public boolean isAttr(Object name, Object expected) {
        return false;
    }

    /**
     * @param name
     * @param expected
     * @param inherit
     * @return
     * @deprecated Use {@link #isAttribute(Object, Object, boolean)} instead.
     */
    @Override
    public boolean isAttr(Object name, Object expected, boolean inherit) {
        return false;
    }

    /**
     * @param name
     * @param expected
     * @return
     */
    @Override
    public boolean isAttribute(Object name, Object expected) {
        return false;
    }

    /**
     * @param name
     * @param expected
     * @param inherit
     * @return
     */
    @Override
    public boolean isAttribute(Object name, Object expected, boolean inherit) {
        return false;
    }

    /**
     * @param name
     * @param value
     * @param overwrite
     * @return
     * @deprecated Use {@link #setAttribute(Object, Object, boolean)} instead.
     */
    @Override
    public boolean setAttr(Object name, Object value, boolean overwrite) {
        return false;
    }

    @Override
    public boolean setAttribute(Object name, Object value, boolean overwrite) {
        return false;
    }

    @Override
    public boolean isOption(Object name) {
        return false;
    }

    @Override
    public boolean isRole() {
        return false;
    }

    @Override
    public boolean hasRole(String role) {
        return false;
    }

    @Override
    public String getRole() {
        return null;
    }

    /**
     * @deprecated Use {@linkplain #getRole()}  instead.
     */
    @Override
    public String role() {
        return null;
    }

    @Override
    public List<String> getRoles() {
        return null;
    }

    @Override
    public void addRole(String role) {

    }

    @Override
    public void removeRole(String role) {

    }

    @Override
    public boolean isReftext() {
        return false;
    }

    @Override
    public String getReftext() {
        return null;
    }

    @Override
    public String iconUri(String name) {
        return null;
    }

    @Override
    public String mediaUri(String target) {
        return null;
    }

    @Override
    public String imageUri(String targetImage) {
        return null;
    }

    @Override
    public String imageUri(String targetImage, String assetDirKey) {
        return null;
    }

    @Override
    public String readAsset(String path, Map<Object, Object> opts) {
        return null;
    }

    @Override
    public String normalizeWebPath(String path, String start, boolean preserveUriTarget) {
        return null;
    }
}
