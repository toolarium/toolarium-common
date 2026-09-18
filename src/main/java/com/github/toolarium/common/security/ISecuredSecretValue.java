/*
 * ISecuredSecretValue.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.security;

import java.util.function.Function;


/**
 * Extends {@link ISecuredValue} with memory-safe secret access via {@link #use(Function)}
 * and {@link #useChars(Function)}, backed by off-heap storage.
 *
 * @author patrick
 */
public interface ISecuredSecretValue extends ISecuredValue<SecureSecret> {

    /**
     * Passes a temporary {@code byte[]} copy of the secret to the given function, then
     * zeros the copy immediately after the function returns.
     *
     * @param <R> the return type of {@code fn}
     * @param fn the function to receive the temporary copy; must not be {@code null}
     * @return the value returned by {@code fn}
     * @throws NullPointerException if {@code fn} is {@code null}
     * @throws IllegalStateException if this secret has already been destroyed
     */
    <R> R use(Function<byte[], R> fn);


    /**
     * Passes a temporary {@code char[]} copy of the secret to the given function, then
     * zeros the copy immediately after the function returns.
     *
     * @param <R> the return type of {@code fn}
     * @param fn the function to receive the temporary copy; must not be {@code null}
     * @return the value returned by {@code fn}
     * @throws NullPointerException if {@code fn} is {@code null}
     * @throws IllegalStateException if this secret has already been destroyed
     */
    <R> R useChars(Function<char[], R> fn);
}
