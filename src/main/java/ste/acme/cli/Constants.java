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

import static ste.acme.cli.Format.PEM;

/**
 *
 */
public class Constants {
    public static final String DEFAULT_ACCOUNT_KEYS = "account.pem";
    public static final String DEFAULT_DOMAIN_KEYS = "domain.pem";
    public static final String DEFAULT_CERTIFICATE = "domain.crt";
    public static final String DEFAULT_POLLING_INTERVAL = "3000";
    public static final String DEFAULT_CHALLENGE_TIMEOUT = "30s";
    public static final Format DEFAULT_OUT_FORMAT = PEM;

    public static final String OPT_HELP = "--help";
    public static final String OPT_VERSION = "--version";
    public static final String OPT_ACCOUNT_KEYS = "--account-keys";
    public static final String OPT_DOMAIN_KEYS = "--domain-keys";
    public static final String OPT_OUT = "--out";
    public static final String OPT_OUT_FORMAT = "--format";
    public static final String OPT_POLLING_INTERVAL = "--polling-interval";
    public static final String OPT_PORT = "--port";
    public static final String OPT_CHALLENGE_TIMEOUT = "--challenge-timeout";
    public static final String OPT_CONTACT = "--contact";
    public static final String OPT_SECRET = "--secret";
}
