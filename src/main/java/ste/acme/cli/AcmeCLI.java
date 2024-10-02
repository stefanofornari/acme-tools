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

import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.Security;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.connector.Resource;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.provider.AcmeProvider;
import org.shredzone.acme4j.util.KeyPairUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParseResult;

/**
 *
 */
@CommandLine.Command(
        name = "acme-tools",
        versionProvider = AcmeCLI.AcmeToolsVersion.class,
        header = {
            "@|blue  ▗▄▖  ▗▄▄▖▗▖  ▗▖▗▄▄▄▖    ▗▄▄▄▖▗▄▖  ▗▄▖ ▗▖    ▗▄▄▖ |@",
            "@|blue ▐▌ ▐▌▐▌   ▐▛▚▞▜▌▐▌         █ ▐▌ ▐▌▐▌ ▐▌▐▌   ▐▌    |@",
            "@|blue ▐▛▀▜▌▐▌   ▐▌  ▐▌▐▛▀▀▘      █ ▐▌ ▐▌▐▌ ▐▌▐▌    ▝▀▚▖ |@",
            "@|blue ▐▌ ▐▌▝▚▄▄▖▐▌  ▐▌▐▙▄▄▖      █ ▝▚▄▞▘▝▚▄▞▘▐▙▄▄▖▗▄▄▞▘ |@",
            "@|white                                                  |@"
        }
)
public class AcmeCLI {

    private static final long SLEEP = 3000;

    @CommandLine.Option(names = Constants.OPT_HELP, usageHelp = true, description = "display this help and exit")
    boolean printHelp;

    @CommandLine.Option(names = Constants.OPT_VERSION, versionHelp = true, description = "show version information")
    private boolean printVersion;

    public static void main(String... args) {
        Security.addProvider(new BouncyCastleProvider());
        try {
            new CommandLine(new AcmeCLI())
                .setExecutionExceptionHandler(new CLIExceptionHandler())
                .execute(args);
        } catch (Throwable x) {
            System.out.println("Something went wrong: " + x.getMessage());
            //
            // TODO: move it to the log
            //
            x.printStackTrace();
        }
    }

    @Command(name = "new-account", description = "create a new account")
    protected void newAccount(
        @CommandLine.Parameters(
            arity = "1",
            paramLabel = "<endpoint>",
            description = "ACME CA endpoint or URI e.g. https://someca.com, acme://example.org/staging")
        String endpoint,
        @CommandLine.Option(
            names=Constants.OPT_ACCOUNT_KEYS,
            required=false,
            description="optional account keys file",
            defaultValue = Constants.DEFAULT_ACCOUNT_KEYS)
        File accountFile,
        @CommandLine.Option(
            names=Constants.OPT_CONTACT,
            required=false,
            description="optional contact email to provide to the CA"
        )
        String email
    ) throws IOException, AcmeException {
        Session session = new Session(endpoint);
        AcmeProvider provider = session.provider();

        System.out.println(
            "Creating new account and credentials for " +
            provider.resolve(session.getServerUri()) +
            ((email != null) ? " with contact " + email : "")
        );
        System.out.println("Storing the new credentials in " + accountFile.getAbsolutePath());

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair();

        AccountBuilder accountBuilder = new AccountBuilder()
            .agreeToTermsOfService()
            .useKeyPair(accountKeyPair);
        if (email != null) {
            accountBuilder.addEmail(email);
        }

        Account account = accountBuilder.create(session);
        try (FileWriter fw = new FileWriter(accountFile)) {
            KeyPairUtils.writeKeyPair(accountKeyPair, fw);
        }

        System.out.println("New account created with URL " + account.getLocation());
    }

    @Command(name = "renew", description = "renew a previously created certificate")
    protected void renew(
            @Mixin AcmePreferences preferences,
            @CommandLine.Parameters(
                arity = "1",
                paramLabel = "<endpoint>",
                description = "ACME CA endpoint or URI e.g. https://someca.com, acme://example.org/staging")
            String endpoint
    ) throws IOException, AcmeException {
        Session session = new Session(endpoint);

        Login login = new AccountBuilder()
                .onlyExisting() // Do not create a new account
                .agreeToTermsOfService()
                .useKeyPair(KeyPairUtils.readKeyPair(new FileReader(preferences.account())))
                .createLogin(session);

        System.out.println(session.getMetadata().getTermsOfService());
        System.out.println(session.getMetadata().getWebsite());

        System.out.println("Renewing SSL certificates from " + session.resourceUrl(Resource.NEW_ORDER));
        System.out.println("using account credentials in " + new File(preferences.account()).getAbsolutePath());
        System.out.println("using domain credentials in " + new File(preferences.domain()).getAbsolutePath());
        System.out.println("storing the new certificate in " + new File(preferences.certificate()).getAbsolutePath());

        //
        // TODO: fix domain and duration
        //
        Order order = login.newOrder()
            .domain("example.org")
            .create();

        //
        // TODO: make it reactive
        //
        for (Authorization auth : order.getAuthorizations()) {
            if (auth.getStatus() == Status.PENDING) {
                System.out.println("Authorizing " + auth.getIdentifier());
                Optional<Http01Challenge> challenge = auth.findChallenge(Http01Challenge.class);
                if (challenge.isPresent()) {
                    try {
                        if (challenge(preferences, auth.getIdentifier().getIP().getHostName(), challenge.get())) {
                            System.out.println("Cahallenge passed successfully");
                            while (!EnumSet.of(Status.VALID, Status.INVALID).contains(auth.getStatus())) {
                                System.out.println("Authorization status sill not VALID");
                                auth.fetch();
                                try {
                                    Thread.sleep(preferences.pollingInterval());
                                } catch (InterruptedException x) {
                                    break;
                                }
                            }
                        }
                    } catch (AcmeException x) {
                        System.out.println("Unsuccessful challenge: " + x.getMessage());
                        return;
                    }
                }
            }
        }

        System.out.println("Finalizing the order with the CA");
        order.execute(
            KeyPairUtils.readKeyPair(new FileReader(new File(preferences.domain()).getAbsolutePath()))
        );

        /*
        This is a very simple example which can be improved in many ways:

        Limit the number of checks, to avoid endless loops if the order is stuck on server side.
        Use an asynchronous architecture instead of a blocking Thread.sleep().
        Check if order.fetch() returns a retry-after Instant, and wait for the next update at least until this moment is reached. See the example for a simple way to do that.
        */

        while (!EnumSet.of(Status.VALID, Status.INVALID).contains(order.getStatus())) {
            System.out.println("Order still not VALID");
            try {
                Thread.sleep(preferences.pollingInterval());
            } catch (InterruptedException x) {
                break;
            }
            order.fetch();
        }

        System.out.println("Order processed, getting the certificate");
        Certificate cert = order.getCertificate();

        final String certificate = new File(preferences.certificate()).getAbsolutePath();
        System.out.println("Writing the certificate to " + certificate);
        try (FileWriter out = new FileWriter(certificate)) {
            cert.writeCertificate(out);
        }

        System.out.println("Congratulations! Your reewed certificated is ready.");
    }

    // --------------------------------------------------------- private methods

    private boolean challenge(
        final AcmePreferences preferences, final String domain, final Http01Challenge challenge
    ) throws AcmeException {
        final String CHALLENGE_PATH = "/.well-known/acme-challenge/" + challenge.getToken();
        //
        // TODO: move to a ChallengeServer
        //
        System.out.println("HTTP challenge");
        final CountDownLatch latch = new CountDownLatch(1);

        int port = preferences.port();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 1);
            server.createContext(CHALLENGE_PATH, (exchange) -> {
                String response = challenge.getAuthorization();
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                latch.countDown();
            });
            server.start();

            port = server.getAddress().getPort(); // if the port was 0 an available port has been randomly picked
            System.out.println("Listener started on port " + port);
            System.out.println("Acme-tools is now ready to respond to the CA challenge. The CA server will try");
            System.out.printf("to connect to the URL http://%s:%d%s\n", domain, port, CHALLENGE_PATH);
            System.out.println("Please make sure that the above URL is accessible from internet.");

            challenge.trigger();

            boolean timeoutExpired = !latch.await(
                preferences.challengeTimeout().toMillis(), TimeUnit.MILLISECONDS
            );
            server.stop(0);

            if (timeoutExpired) {
                throw new AcmeException("no challenge received in " + preferences.challengeTimeout().toString().substring(2));
            }
        } catch (IOException | InterruptedException x) {
            throw new AcmeException(x.getMessage(), x);
        }

        return true;
    }

    // ---------------------------------------------------------AcmeToolsVersion

    protected static class AcmeToolsVersion implements CommandLine.IVersionProvider {

        @CommandLine.Spec
        CommandLine.Model.CommandSpec spec; // injected by picocli

        @Override
        public String[] getVersion() throws Exception {
            return new String[]{
                spec.name() + " v" + version()
            };
        }

        public String version() {
            Properties p = new Properties();
            try {
                p.load(AcmePreferences.class.getResourceAsStream("/version.properties"));
            } catch (IOException | NullPointerException x) {
                //
                // nothing we can do about it, N/A will be used
                //
            }
            return p.getProperty("version", "N/A");
        }
    }

    // ----------------------------------------------------- CLIExceptionHandler

    protected static class CLIExceptionHandler implements IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception x, CommandLine commandLine, ParseResult parseResult) {
            System.out.println("Something went wrong: " + x.getMessage());
            x.printStackTrace();
            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
    }

}
