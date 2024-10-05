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
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 *
 */
public class AcmeCLICertInfo extends AcmeCLIExec {

    @Rule
    public final SystemOutRule OUT = new SystemOutRule().enableLog();

    @Before
    public void before() throws IOException {
        super.before();
        OUT.clearLog();
    }

    @Test
    public void check_certificate_info_with_relative_path() throws Exception {
        //
        // We use exec here because default file names are relative therefore
        // we want them created in the temporary HOME
        //
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);
        execJava("info", "domain.crt");

        //System.out.println(err());
        //System.out.println(out());

        then(out())
            .contains("Reading certificate " + new File(HOME, "domain.crt").getAbsolutePath())
            .contains("Subject: CN=domain, L=Minas Tirith, ST=Gondor, C=XX")
            .contains("Validity: [From: Sat Oct 05 11:20:43 CEST 2024,")
            .contains("To: Tue Oct 03 11:20:43 CEST 2034]");
    }

    @Test
    public void check_certificate_invalid_path() throws Exception {
        AcmeCLI.main("info", "domain.crt");

        then(OUT.getLog())
            .contains("Invalid certificate file " + new File("domain.crt").getAbsolutePath() + ": domain.crt (No such file or directory)")
            .doesNotContain("Subject: CN=domain, L=Minas Tirith, ST=Gondor, C=XX");

        OUT.clearLog();

        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);
        final File F = new File(HOME, "domain.crt"); F.setReadable(false);

        AcmeCLI.main("info", F.getAbsolutePath());

        then(OUT.getLog())
            .contains("Invalid certificate file " + F.getAbsolutePath() + ": ")
            .doesNotContain("Subject: CN=domain, L=Minas Tirith, ST=Gondor, C=XX");
    }

    @Test
    public void check_certificate_invalid_certificate() throws Exception {
        AcmeCLI.main("info", new File("src/test/data/default/account.pem").getAbsolutePath());

        then(OUT.getLog())
            .contains("Invalid certificate, it does not seem to be a X509 certificate: signed overrun, bytes = 918")
            .doesNotContain("Subject: CN=domain, L=Minas Tirith, ST=Gondor, C=XX");
    }
}
