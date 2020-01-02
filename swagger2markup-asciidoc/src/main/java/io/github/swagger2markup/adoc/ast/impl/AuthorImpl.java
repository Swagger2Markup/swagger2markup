package io.github.swagger2markup.adoc.ast.impl;

import org.asciidoctor.ast.Author;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthorImpl implements Author {

    private static final String AUTHOR_ATTRIBUTE_NAME = "author";
    private static final String LASTNAME_ATTRIBUTE_NAME = "lastname";
    private static final String FIRST_NAME_ATTRIBUTE_NAME = "firstname";
    private static final String EMAIL_ATTRIBUTE_NAME = "email";
    private static final String INITIALS_ATTRIBUTE_NAME = "authorinitials";
    private static final String MIDDLE_NAME_ATTRIBUTE_NAME = "middlename";

    private String fullName;
    private String lastName;
    private String firstName;
    private String middleName;
    private String email;
    private String initials;

    public static Author getInstance(Map<String, Object> attributes) {
        return getAuthor(attributes, "");
    }

    public static List<Author> getAuthors(Map<String, Object> attributes) {

        List<Author> authors = new ArrayList<>();

        boolean noMoreAuthors = false;

        int suffix = 1;

        while (!noMoreAuthors) {

            if (attributes.containsKey(AUTHOR_ATTRIBUTE_NAME + "_" + suffix)) {
                authors.add(getAuthor(attributes, "_" + suffix));
                suffix++;
            } else {
                noMoreAuthors = true;
            }

        }

        return authors;

    }

    private static Author getAuthor(Map<String, Object> attributes, String suffix) {
        org.asciidoctor.jruby.ast.impl.AuthorImpl author = new org.asciidoctor.jruby.ast.impl.AuthorImpl();

        if (attributes.containsKey(AUTHOR_ATTRIBUTE_NAME + suffix)) {
            author.setFullName((String) attributes.get(AUTHOR_ATTRIBUTE_NAME + suffix));
        }

        if (attributes.containsKey(LASTNAME_ATTRIBUTE_NAME + suffix)) {
            author.setLastName((String) attributes.get(LASTNAME_ATTRIBUTE_NAME + suffix));
        }

        if (attributes.containsKey(FIRST_NAME_ATTRIBUTE_NAME + suffix)) {
            author.setFirstName((String) attributes.get(FIRST_NAME_ATTRIBUTE_NAME + suffix));
        }

        if (attributes.containsKey(MIDDLE_NAME_ATTRIBUTE_NAME + suffix)) {
            author.setMiddleName(attributes.get(MIDDLE_NAME_ATTRIBUTE_NAME) + suffix);
        }

        if (attributes.containsKey(EMAIL_ATTRIBUTE_NAME + suffix)) {
            author.setEmail((String) attributes.get(EMAIL_ATTRIBUTE_NAME + suffix));
        }

        if (attributes.containsKey(INITIALS_ATTRIBUTE_NAME + suffix)) {
            author.setInitials((String) attributes.get(INITIALS_ATTRIBUTE_NAME + suffix));
        }

        return author;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    @Override
    public String toString() {

        StringBuilder authorStringRepresentation = new StringBuilder();

        if (this.getFullName() != null) {
            authorStringRepresentation.append(this.getFullName());
        }

        if (this.getEmail() != null) {
            authorStringRepresentation.append(" <").append(this.getEmail()).append(">");
        }

        return authorStringRepresentation.toString();
    }

}
