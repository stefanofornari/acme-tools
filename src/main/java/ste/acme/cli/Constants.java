/*
 * acme-tools
 * ----------
 *
 * Copyright (C) 2024 Stefano Fornari. Licensed under the
 * EUPL-1.2 or later (see LICENSE).
 *
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Stefano Fornari.  This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * STEFANO FORNARI MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. STEFANO FORNARI SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package ste.acme.cli;

/**
 *
 */
public class Constants {
    public static final String DEFAULT_ACCOUNT_FILE = "account.pem";
    public static final String DEFAULT_DOMAIN_FILE = "domain.pem";
    public static final String DEFAULT_CERTIFICATE_FILE = "domain.crt";
    public static final String DEFAULT_POLLING_INTERVAL = "3000";
    public static final String DEFAULT_CHALLENGE_TIMEOUT = "30s";

    public static final String OPT_HELP = "--help";
    public static final String OPT_VERSION = "--version";
    public static final String OPT_ACCOUNT = "--account";
    public static final String OPT_DOMAIN = "--domain";
    public static final String OPT_CERTIFICATE = "--certificate";
    public static final String OPT_POLLING_INTERVAL = "--polling-interval";
    public static final String OPT_PORT = "--port";
    public static final String OPT_CHALLENGE_TIMEOUT = "--challenge-timeout";
    public static final String OPT_CONTACT = "--contact";
}
