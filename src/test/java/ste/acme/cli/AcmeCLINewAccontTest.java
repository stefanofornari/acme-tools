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
import java.io.FileReader;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.BDDAssertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.shredzone.acme4j.util.KeyPairUtils;
import ste.xtest.exec.BugFreeExec;

/**
 *
 */
public class AcmeCLINewAccontTest extends BugFreeExec {

    @Rule
    public final SystemOutRule OUT = new SystemOutRule().enableLog();

    @Test
    public void new_account_with_defaults() throws Exception {
        //
        // We use exec here because default file names are relative therefore
        // we want them created in the temporary HOME
        //
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);
        exec(
            "java",
//            "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1044",  // uncomment and run the test for debug
            AcmeCLI.class.getCanonicalName(), "new-account", "acmetest:new-account://cacert1.com"
        );

        //System.out.println(err());
        //System.out.println(out());

        then(out())
            .contains("Creating new account and credentials for https://cacert1.com")
            .contains("Storing the new credentials in " + new File(HOME, "account.pem").getAbsolutePath())
            .contains("New account created with URL https://cacert1.com"
        );

        final File F = new File(HOME, "account.pem");
        then(F).exists();
        try {
            try (FileReader fr = new FileReader(F)) {
                KeyPairUtils.readKeyPair(fr);
            }
        } catch (Exception x) {
            fail("no exception expected");
        }
        //
        // The existing shall be rewritten
        //
        then(FileUtils.readFileToString(F, Charset.defaultCharset()))
            .isNotEqualTo(FileUtils.readFileToString(new File("src/test/data/default/account.pem"), Charset.defaultCharset()));
    }

    @Test
    public void new_account_with_key_file_and_contact() throws Exception {
        //
        // We can invoke AcmeCLI directly as we pass absolute file names
        //
        final File PEM = new File(HOME, "account.pem");
        AcmeCLI.main("new-account", "acmetest:new-account://cacert1.com",
            "--account-keys", PEM.getAbsolutePath(),
            "--contact", "someone@somewhere.com"
        );

        then(OUT.getLog())
            .contains("Creating new account and credentials for https://cacert1.com with contact someone@somewhere.com")
            .contains("Storing the new credentials in " + PEM.getAbsolutePath())
            .contains("New account created with URL https://cacert1.com"
        );

        //
        // THe output file exists and contains a keypair
        //
        then(PEM).exists();
        try {
            try (FileReader fr = new FileReader(PEM)) {
                KeyPairUtils.readKeyPair(fr);
            }
        } catch (Exception x) {
            fail("no exception expected");
        }
    }
}
