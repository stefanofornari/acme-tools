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
import org.assertj.core.api.BDDAssertions;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;
import static ste.xtest.Constants.BLANKS;
import ste.xtest.net.NetTools;

/**
 *
 */
public class AcmePreferencesTest {

    @Test
    public void initialization_getters_setters() throws Exception {
        final String FILE = "another/file.ext";

        AcmePreferences p = new AcmePreferences();

        then(p.account()).isEqualTo(Constants.DEFAULT_ACCOUNT_FILE);
        p.account(FILE); then(p.account()).isEqualTo(FILE);

        then(p.domain()).isEqualTo(Constants.DEFAULT_DOMAIN_FILE);
        p.domain(FILE); then(p.domain()).isEqualTo(FILE);

        then(p.certificate()).isEqualTo(Constants.DEFAULT_CERTIFICATE_FILE);
        p.certificate(FILE); then(p.certificate()).isEqualTo(FILE);

        then(p.pollingInterval()).isEqualTo(Integer.parseInt(Constants.DEFAULT_POLLING_INTERVAL));
        p.pollingInterval(10); then(p.pollingInterval()).isEqualTo(10);

        then(p.port()).isZero();
        final int port = new NetTools().pickAvailablePort();
        p.port(port); then(p.port()).isEqualTo(port);

        then(p.challengeTimeout()).isEqualTo(Duration.ofSeconds(30));
        p.challengeTimeout(Duration.ofSeconds(100)); then(p.challengeTimeout()).isEqualTo(Duration.ofSeconds(100));
        p.challengeTimeout("1m 30s 10ms"); then(p.challengeTimeout())
            .isEqualTo(Duration.ofMillis(10 /* ms */ + 30*1000 /* s */ + 60*1000 /* m */));
        p.challengeTimeout("2h2M0S 20Ms"); then(p.challengeTimeout())
            .isEqualTo(Duration.ofMillis(20 /* ms */ + 2*60*1000 /* m */ + 2*60*60*1000 /* h */));

        BDDAssertions.thenThrownBy(() -> {
            p.challengeTimeout("abcd");
        }).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("challengeTimeout 'abcd' does not contain any time period");

        for (String blank: BLANKS) {
            BDDAssertions.thenThrownBy(() -> {
                p.challengeTimeout(blank);
            }).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("challengeTimeout can not be blank");
        }
    }

}
