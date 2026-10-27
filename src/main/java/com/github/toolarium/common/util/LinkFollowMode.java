/*
 * LinkFollowMode.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.common.util;


/**
 * Defines how symbolic links are handled when a directory tree is processed, e.g. by
 * {@link FileUtil#searchFiles(java.nio.file.Path, String, String, LinkFollowMode)}.
 *
 * @author patrick
 */
public enum LinkFollowMode {

    /** Symbolic links are not followed: linked files and linked directories are skipped. */
    NONE,

    /** Symbolic links (files and directories) are followed only if their target is inside the root directory. */
    WITHIN_ROOT,

    /** Symbolic links (files and directories) are always followed, also if their target is outside the root directory. */
    ALL
}
