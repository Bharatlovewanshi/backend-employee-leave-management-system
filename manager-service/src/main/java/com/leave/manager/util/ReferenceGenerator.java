package com.leave.manager.util;

import java.util.UUID;

/**
 * Utility class for generating unique reference numbers
 * for Manager Service operations.
 */
public final class ReferenceGenerator {

    /**
     * Private constructor to prevent object creation.
     */
    private ReferenceGenerator() {
    }

    /**
     * Generates a unique manager service reference number.
     *
     * @return unique reference number with MGR prefix
     */
    public static String generateReference() {
        return "MGR-" + UUID.randomUUID();
    }
}