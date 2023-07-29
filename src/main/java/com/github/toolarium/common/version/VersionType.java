/*
 * VersionType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.version;

/**
 * The version type
 * 
 * @author patrick
 */
public enum VersionType {
    /**
     * The default type of version: Major, minor and patch parts are required. Suffixes and build are optional.
     */
    STRICT,

    /**
     * Major part is required. Minor, patch, suffixes and build are optional.
     */
    LOOSE,

    /**
     * Follows the rules of NPM. Supports ^, x, *, ~, and more.
     * See https://github.com/npm/node-semver
     */
    NPM,

    /**
     * Follows the rules of ivy. Supports dynamic parts (e.g.: 4.2.+) and ranges
     * See http://ant.apache.org/ivy/history/latest-milestone/ivyfile/dependency.html
     */
    IVY;
}