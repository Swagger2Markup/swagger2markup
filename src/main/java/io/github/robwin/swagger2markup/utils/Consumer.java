package io.github.robwin.swagger2markup.utils;

/**
 * Java 8 style Consumer functional interface
 */
public interface Consumer<T> {

    /**
     * The function itself
     * @param t the function argument
     */
    void accept(T t);

}
