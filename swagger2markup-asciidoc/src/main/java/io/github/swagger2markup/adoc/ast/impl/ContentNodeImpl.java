package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("SuspiciousMethodCalls")
public abstract class ContentNodeImpl implements ContentNode {

    private String id;
    private final String context;
    private final Map<String, Object> attributes;
    private final List<String> roles;
    private final ContentNode parent;

    public ContentNodeImpl(ContentNode parent, String context) {
        this(parent, context, new HashMap<>(), new ArrayList<>());
    }

    public ContentNodeImpl(ContentNode parent, String context, Map<String, Object> attributes, List<String> roles) {
        this.parent = parent;
        this.context = context;
        this.attributes = attributes;
        this.roles = roles;
    }

    @Override
    @Deprecated
    public String id() {
        return getId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    @Deprecated
    public String context() {
        return getContext();
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    @Deprecated
    public ContentNode parent() {
        return getParent();
    }

    @Override
    public ContentNode getParent() {
        return parent;
    }

    @Override
    @Deprecated
    public Document document() {
        return getDocument();
    }

    @Override
    public Document getDocument() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String getNodeName() {
        return getContext();
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
        return attributes;
    }

    @Override
    @Deprecated
    public Object getAttr(Object name, Object defaultValue, boolean inherit) {
        return getAttribute(name, defaultValue, inherit);
    }

    @Override
    @Deprecated
    public Object getAttr(Object name, Object defaultValue) {
        return getAttribute(name, defaultValue);
    }

    @Override
    @Deprecated
    public Object getAttr(Object name) {
        return getAttribute(name);
    }

    @Override
    public Object getAttribute(Object name, Object defaultValue, boolean inherit) {
        return getAttribute(name, defaultValue);
    }

    @Override
    public Object getAttribute(Object name, Object defaultValue) {
        return attributes.getOrDefault(name, defaultValue);
    }

    @Override
    public Object getAttribute(Object name) {
        return attributes.get(name);
    }

    @Override
    @Deprecated
    public boolean isAttr(Object name, Object expected, boolean inherit) {
        return isAttribute(name, expected, inherit);
    }

    @Override
    @Deprecated
    public boolean isAttr(Object name, Object expected) {
        return isAttribute(name, expected);
    }

    @Override
    public boolean isAttribute(Object name, Object expected, boolean inherit) {
        return isAttribute(name, expected);
    }

    @Override
    public boolean isAttribute(Object name, Object expected) {
        try {
            if (attributes.containsKey(name)) {
                return attributes.get(name).equals(expected);
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Deprecated
    public boolean hasAttr(Object name) {
        return hasAttribute(name);
    }

    @Override
    @Deprecated
    public boolean hasAttr(Object name, boolean inherited) {
        return hasAttribute(name, inherited);
    }

    @Override
    public boolean hasAttribute(Object name) {
        return attributes.containsKey(name);
    }

    @Override
    public boolean hasAttribute(Object name, boolean inherited) {
        return hasAttribute(name);
    }

    @Override
    @Deprecated
    public boolean setAttr(Object name, Object value, boolean overwrite) {
        return setAttribute(name, value, overwrite);
    }

    @Override
    public boolean setAttribute(Object name, Object value, boolean overwrite) {
        return setAttribute((String)name, value, overwrite);
    }

    public boolean setAttribute(String name, Object value, boolean overwrite) {
        try {
            if (overwrite) {
                attributes.put(name, value);
            } else {
                attributes.putIfAbsent(name, value);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isOption(Object name) {
        try {
            Object o = attributes.get(name);
            return null != o && o.toString().equals("");
        }catch (Exception ignored){
            return false;
        }
    }

    @Override
    public boolean isRole() {
        return false;
    }

    @Override
    public String getRole() {
        return String.join(",", roles);
    }

    @Override
    @Deprecated
    public String role() {
        return getRole();
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    @Override
    public void addRole(String role) {
        roles.add(role);
    }

    @Override
    public void removeRole(String role) {
        roles.remove(role);
    }

    @Override
    public boolean isReftext() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String getReftext() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String iconUri(String name) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String mediaUri(String target) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String imageUri(String targetImage) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String imageUri(String targetImage, String assetDirKey) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String readAsset(String path, Map<Object, Object> opts) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

    @Override
    public String normalizeWebPath(String path, String start, boolean preserveUriTarget) {
        throw new UnsupportedOperationException("Not implemented, yet");
    }

}
