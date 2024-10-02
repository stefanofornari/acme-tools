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

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import ste.xtest.junit.BugFree;

/**
 *
 */
public class AcmeCLITest extends BugFree {

    @Rule
    public final SystemOutRule OUT = new SystemOutRule().enableLog();

    @Test
    public void show_error_messages_on_std_out() {
        AcmeCLI.main("renew", "acme://somewhere");
        then(OUT.getLog()).contains("Something went wrong: No ACME provider found for acme://somewhere");

        OUT.clearLog();

        AcmeCLI.main("renew", "acmetest:account-error://cacert1.com", "--account-keys", "src/test/data/default/account.pem");
        then(OUT.getLog()).contains("Something went wrong: No account exists with the provided key");
    }
}
