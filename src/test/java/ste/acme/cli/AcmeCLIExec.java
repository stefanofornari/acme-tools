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
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import ste.xtest.exec.BugFreeExec;

/**
 *
 *
 */
public class AcmeCLIExec extends BugFreeExec {

    @Rule
    public final SystemOutRule STDOUT = new SystemOutRule().enableLog();

    //
    // We can not override existing exec() otherwise the super class would call
    // the overriden method
    //
    protected int execJava(final String... args)
    throws IOException, InterruptedException {
        return super.exec(-1, allArgs(args));
    }

    //
    // We can not override existing exec() otherwise the super class would call
    // the overriden method
    //
    protected Process startJava(final String... args) throws IOException, InterruptedException {
        return super.start(allArgs(args));
    }

    // --------------------------------------------------------- private methods

    private String[] allArgs(final String[] args) {
        List<String> allArgs = new ArrayList<>();
        allArgs.add("java");
        //allArgs.add("-Xdebug"); allArgs.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1044", args); // uncomment and run the test for debug)
        allArgs.add("-Djava.util.logging.config.file=" + new File("src/test/resources/logging.properties").getAbsolutePath());
        allArgs.add(AcmeCLI.class.getCanonicalName());

        allArgs.addAll(List.of(args));

        return allArgs.toArray(new String[0]);
    }

}
