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

    /**
     * @return the secret
     */
    public String secret() {
        return secret;
    }

    /**
     * @param secret the secret to set
     */
    public void secret(String secret) {
        this.secret = secret;
    }

    private static final Pattern PERIOD_PATTERN = Pattern.compile("([0-9]+)(ms|[dhms])");

    @Option(names=Constants.OPT_ACCOUNT_KEYS, required=false, description="optional account keys file (default: account.pem)", defaultValue = Constants.DEFAULT_ACCOUNT_KEYS)
    private String accountkeys = Constants.DEFAULT_ACCOUNT_KEYS;

    @Option(names=Constants.OPT_DOMAIN_KEYS, required=false, description="optional domain keys file (default: domain.pem)", defaultValue = Constants.DEFAULT_DOMAIN_KEYS)
    private String domainKeys = Constants.DEFAULT_DOMAIN_KEYS;

    @Option(names=Constants.OPT_OUT, required=false, description="optional filename for the certificate (default: domain.crt)", defaultValue = Constants.DEFAULT_CERTIFICATE)
    private String out = Constants.DEFAULT_CERTIFICATE;

    @Option(names=Constants.OPT_POLLING_INTERVAL, required=false, description="optional interval in millisecond used when polling for events (default: 3000)", defaultValue = Constants.DEFAULT_POLLING_INTERVAL)
    private int pollingInterval = Integer.parseInt(Constants.DEFAULT_POLLING_INTERVAL);

    @Option(names=Constants.OPT_PORT, required=false, description="tcp port to use to listen for CA challenge request; if not provided an available port will be picked randomly")
    private int port = 0; // randomly pick an available port

    /* see challengeTimeout(String) */
    private Duration challengeTimeout = Duration.ofSeconds(30);

    @Option(names=Constants.OPT_OUT_FORMAT, required=false, description="optional format for the output file; one of 'pem', 'pkcs12' (default: pem)")
    private Format format = Constants.DEFAULT_OUT_FORMAT;

    @Option(names=Constants.OPT_SECRET, required=false, description="optional password for the output file (e.g. PKCS12 keystore password)")
    private String secret = null;

    /**
     * @return the account keys file
     */
    public String accountKeys() {
        return accountkeys;
    }

    /**
     * @param accountKeys the account keys file to set
     */
    public void accountKeys(String accountKeys) {
        this.accountkeys = accountKeys;
    }

    /**
     * @return the domain keys file
     */
    public String domainKeys() {
        return domainKeys;
    }

    /**
     * @param domainKeys the domain keys file to set
     */
    public void domainKeys(String domainKeys) {
        this.domainKeys = domainKeys;
    }

    /**
     * @return the output file
     */
    public String out() {
        return out;
    }

    /**
     * @param out the output file
     */
    public void out(String out) {
        this.out = out;
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
        description="max time to wait for a challenge in human readable form (e.g. 1m 30s, default: 30s)",
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

    /**
     * @return the format
     */
    public Format format() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void format(Format format) {
        this.format = format;
    }
}
