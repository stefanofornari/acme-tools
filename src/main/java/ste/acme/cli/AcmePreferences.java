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

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Option;

/**
 *
 */

public class AcmePreferences {

    private static final Pattern PERIOD_PATTERN = Pattern.compile("([0-9]+)(ms|[dhms])");

    @Option(names=Constants.OPT_ACCOUNT_KEYS, required=false, description="optional account keys file", defaultValue = Constants.DEFAULT_ACCOUNT_KEYS)
    private String account = Constants.DEFAULT_ACCOUNT_KEYS;

    @Option(names=Constants.OPT_DOMAIN_KEYS, required=false, description="optional domain keys file", defaultValue = Constants.DEFAULT_DOMAIN_KEYS)
    private String domain = Constants.DEFAULT_DOMAIN_KEYS;

    @Option(names=Constants.OPT_CERTIFICATE, required=false, description="optional certificate file", defaultValue = Constants.DEFAULT_CERTIFICATE)
    private String certificate = Constants.DEFAULT_CERTIFICATE;

    @Option(names=Constants.OPT_POLLING_INTERVAL, required=false, description="interval in millisecond used when polling for events", defaultValue = Constants.DEFAULT_POLLING_INTERVAL)
    private int pollingInterval = Integer.parseInt(Constants.DEFAULT_POLLING_INTERVAL);

    @Option(names=Constants.OPT_PORT, required=false, description="tcp port to use to listen for CA challenge request; if not provided an available port will be picked randomly")
    private int port = 0; // randomly pick an available port

    /* see challengeTimeout(String) */
    private Duration challengeTimeout = Duration.ofSeconds(30);

    /**
     * @return the account
     */
    public String account() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void account(String account) {
        this.account = account;
    }

    /**
     * @return the domain
     */
    public String domain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void domain(String domain) {
        this.domain = domain;
    }

    /**
     * @return the certificate
     */
    public String certificate() {
        return certificate;
    }

    /**
     * @param certificate the certificate to set
     */
    public void certificate(String certificate) {
        this.certificate = certificate;
    }

    /**
     * @return the pollingInterval
     */
    public int pollingInterval() {
        return pollingInterval;
    }

    /**
     * @param pollingInterval the pollingInterval to set
     */
    public void pollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    /**
     * @return the port
     */
    public int port() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void port(int port) {
        this.port = port;
    }

    /**
     * @return the challengeTimeout
     */
    public Duration challengeTimeout() {
        return challengeTimeout;
    }

    /**
     * @param challengeTimeout the challengeTimeout to set
     */
    public void challengeTimeout(Duration challengeTimeout) {
        this.challengeTimeout = challengeTimeout;
    }

    /**
     * @param challengeTimeout the challengeTimeout to set as a string (e.i. 1m 30s 10ms)
     */
    @Option(
        names=Constants.OPT_CHALLENGE_TIMEOUT,
        required=false,
        description="max time to wait for a challenge in human readable form (e.g. 1m 30s)",
        defaultValue = Constants.DEFAULT_CHALLENGE_TIMEOUT
    )


    public void challengeTimeout(String challengeTimeout) {
        if (StringUtils.isBlank(challengeTimeout)) {
            throw new IllegalArgumentException("challengeTimeout can not be blank");
        }
        challengeTimeout = challengeTimeout.toLowerCase().replace(" ", "");
        Matcher matcher = PERIOD_PATTERN.matcher(challengeTimeout);

        this.challengeTimeout = Duration.ofSeconds(0);

        boolean found = false;
        while(matcher.find()) {
            found = true;

            int value = Integer.parseInt(matcher.group(1));
            String type = matcher.group(2);

            switch (type) {
                case "h":
                    this.challengeTimeout = this.challengeTimeout.plusHours(value);
                    break;
                case "m":
                    this.challengeTimeout = this.challengeTimeout.plusMinutes(value);
                    break;
                case "s":
                    this.challengeTimeout = this.challengeTimeout.plusSeconds(value);
                    break;
                case "ms":
                    this.challengeTimeout = this.challengeTimeout.plusMillis(value);
                    break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException(
                String.format("challengeTimeout '%s' does not contain any time period", challengeTimeout)
            );
        }
    }
}
