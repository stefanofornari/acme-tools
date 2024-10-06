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

import java.io.File;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 */
public class AcmeCLITest extends AcmeCLIExec {

    @Test
    public void show_error_messages_on_std_out() {
        AcmeCLI.main("renew", "acme://somewhere", "mydomain.com");
        then(STDOUT.getLog()).contains("Something went wrong: No ACME provider found for acme://somewhere");

        STDOUT.clearLog();

        AcmeCLI.main("renew", "acmetest:account-error://cacert1.com", "mydomain.com", "--account-keys", "src/test/data/default/account.pem");
        then(STDOUT.getLog()).contains("Something went wrong: No account exists with the provided key");
    }

    @Test
    public void write_session_log_ok() throws Exception {
        final String accountKeys = new File("src/test/data/default/account.pem").getAbsolutePath();
        execJava(
            "renew", "acmetest:new-account://cacert1.com", "mydomain.com",
            "--account-keys", accountKeys
        );
        then(new File(HOME, "acme-tools.log")).exists()
            .content().contains("Renewing SSL certificates for domain mydomain.com");
    }

    @Test
    public void write_session_log_error() throws Exception {
        final String accountKeys = new File("src/test/data/default/account.pem").getAbsolutePath();
        execJava(
            "renew", "acmetest:account-error://cacert1.com", "mydomain.com",
            "--account-keys", accountKeys
        );
        then(new File(HOME, "acme-tools.log")).exists()
            .content().contains("INFO ste.acme-tools Something went wrong: No account exists with the provided key")
                      .contains("SEVERE ste.acme-tools org.shredzone.acme4j.exception.AcmeServerException: No account exists with the provided key");
    }
}
