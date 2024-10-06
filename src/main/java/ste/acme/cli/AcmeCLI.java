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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
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
import static ste.acme.cli.Format.PEM;
import static ste.acme.cli.Format.PKCS12;

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

    private static final Logger LOG = Logger.getLogger("ste.acme-tools");

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
            err(x);
        }
    }

    @Command(name = "new-account", description = "create a new account", usageHelpWidth = 300)
    protected void newAccount(
        @CommandLine.Parameters(
            arity = "1",
            paramLabel = "<endpoint>",
            description = "ACME CA endpoint or URI e.g. https://someca.com, acme://example.org/staging")
        String endpoint,
        @CommandLine.Option(
            names=Constants.OPT_ACCOUNT_KEYS,
            required=false,
            description="optional account keys file (default: account.pem)",
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

        out(
            "Creating new account and credentials for " +
            provider.resolve(session.getServerUri()) +
            ((email != null) ? " with contact " + email : "")
        );
        out("Storing the new credentials in " + accountFile.getAbsolutePath());

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

        out("New account created with URL " + account.getLocation());
    }

    @Command(name = "renew", description = "renew a previously created certificate", usageHelpWidth = 300)
    protected void renew(
            @Mixin AcmePreferences preferences,
            @CommandLine.Parameters(
                arity = "1",
                index = "0",
                paramLabel = "<endpoint>",
                description = "ACME CA endpoint or URI e.g. https://someca.com, acme://example.org/staging")
            String endpoint,
            @CommandLine.Parameters(
                arity = "1",
                index = "1",
                paramLabel = "<domain>",
                description = "the domain to renew the certificate for")
            String domain
    ) throws IOException, AcmeException {
        checkRenewOptions(preferences);

        Session session = new Session(endpoint);

        out("Renewing SSL certificates for domain " + domain + " from " + session.resourceUrl(Resource.NEW_ORDER));
        out("using account credentials in " + new File(preferences.accountKeys()).getAbsolutePath());
        out("using domain credentials in " + new File(preferences.domainKeys()).getAbsolutePath());
        out("storing the new certificate in " + new File(preferences.out()).getAbsolutePath());

        Login login = new AccountBuilder()
                .onlyExisting() // Do not create a new account
                .agreeToTermsOfService()
                .useKeyPair(KeyPairUtils.readKeyPair(new FileReader(preferences.accountKeys())))
                .createLogin(session);

        // TODO: terms of services acceptance

        Order order = login.newOrder()
            .domain(domain)
            .create();

        //
        // TODO: make it reactive
        //
        for (Authorization auth : order.getAuthorizations()) {
            if (auth.getStatus() == Status.PENDING) {
                out("Authorizing " + auth.getIdentifier());
                Optional<Http01Challenge> challenge = auth.findChallenge(Http01Challenge.class);
                if (challenge.isPresent()) {
                    try {
                        challenge(preferences, auth, challenge.get());
                        out("Cahallenge passed successfully");
                    } catch (AcmeException x) {
                        out("Unsuccessful challenge: " + x.getMessage());
                        return;
                    }
                }
            }
        }

        out("Finalizing the order with the CA");
        order.execute(
            KeyPairUtils.readKeyPair(new FileReader(new File(preferences.domainKeys()).getAbsolutePath()))
        );

        /*
        This is a very simple example which can be improved in many ways:

        Limit the number of checks, to avoid endless loops if the order is stuck on server side.
        Use an asynchronous architecture instead of a blocking Thread.sleep().
        Check if order.fetch() returns a retry-after Instant, and wait for the next update at least until this moment is reached. See the example for a simple way to do that.
        */

        while (!EnumSet.of(Status.VALID, Status.INVALID).contains(order.getStatus())) {
            out("Order still not VALID");
            try {
                Thread.sleep(preferences.pollingInterval());
            } catch (InterruptedException x) {
                break;
            }
            order.fetch();
        }

        out("Order processed, getting the certificate");
        Certificate cert = order.getCertificate();

        final String outFilename = new File(preferences.out()).getAbsolutePath();
        out("Writing the certificate to " + outFilename);

        if (PEM.equals(preferences.format())) {
            try (FileWriter out = new FileWriter(outFilename)) {
                cert.writeCertificate(out);
            }
        } else {
            PrivateKey privateKey = null;
            try (
                PEMParser parser = new PEMParser(
                    new FileReader(preferences.domainKeys())
                )) {

                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                KeyPair keyPair = converter.getKeyPair((PEMKeyPair)parser.readObject());
                privateKey = keyPair.getPrivate();
            }

            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                List<java.security.cert.Certificate> chainList = new ArrayList<>();

                chainList.add(cert.getCertificate());

                java.security.cert.Certificate[] chain = chainList.toArray(new java.security.cert.Certificate[0]);

                // Create PKCS12 keystore
                KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
                keyStore.load(null, null);
                keyStore.setKeyEntry(domain, privateKey, preferences.secret().toCharArray(), chain);

                try (FileOutputStream fos = new FileOutputStream(preferences.out())) {
                    keyStore.store(fos, preferences.secret().toCharArray());
                }
            } catch (Exception x) {
                out("Somethig went wrong: " + x.getMessage());
                return;
            }
        }

        out("Congratulations! Your renewed certificated is ready.");
    }

    @Command(name = "info", description = "print information in the provided certificate", usageHelpWidth = 300)
    protected void info(
        @CommandLine.Parameters(
            arity = "1",
            paramLabel = "<certificate>",
            description = "the filepath of the certificate")
        File certificateFile
    ) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            try (FileInputStream fis = new FileInputStream(certificateFile)) {
                out("Reading certificate " + certificateFile.getAbsolutePath());
                X509Certificate certificate = (X509Certificate)cf.generateCertificate(fis);
                out(certificate);
            }

        } catch (CertificateException x) {
            out("Invalid certificate, it does not seem to be a X509 certificate: " + x.getMessage());
        } catch (FileNotFoundException x) {
            out("Invalid certificate file " + certificateFile.getAbsolutePath() + ": " + x.getMessage());
        } catch (IOException x) {
            out("Error reading the certificate: " + x.getMessage());
        }

    }

    // --------------------------------------------------------- private methods

    private void checkRenewOptions(final AcmePreferences preferences) throws IllegalArgumentException {
        if (PKCS12.equals(preferences.format())) {
            if (StringUtils.isEmpty(preferences.secret())) {
                throw new IllegalArgumentException(
                    "A keystore password must be provided for output " + PKCS12 + " (use " + Constants.OPT_SECRET + ")"
                );
            }
        }
    }

    private void challenge(
        final AcmePreferences preferences, final Authorization auth, final Http01Challenge challenge
    ) throws AcmeException {
        final String CHALLENGE_PATH = "/.well-known/acme-challenge/" + challenge.getToken();
        //
        // TODO: move to a ChallengeServer
        //
        out("HTTP challenge");

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
            });
            server.start();

            port = server.getAddress().getPort(); // if the port was 0 an available port has been randomly picked
            out("Listener started on port " + port);
            out("Acme-tools is now ready to respond to the CA challenge. The CA server will try");
            System.out.printf("to connect to the URL http://%s%s\n", auth.getIdentifier().getDomain(), CHALLENGE_PATH);
            out("Please make sure that the above URL is accessible from internet.");

            challenge.trigger();

            long pollingMillis = preferences.pollingInterval();
            long millisToWait = preferences.challengeTimeout().toMillis();
            while ((millisToWait > 0) && EnumSet.of(Status.PENDING, Status.PROCESSING).contains(auth.getStatus())) {
                out("Authorization status still processing");
                auth.fetch();
                try {
                    Thread.sleep(pollingMillis);
                    millisToWait -= pollingMillis;
                } catch (InterruptedException x) {
                    break;
                }
            }

            server.stop(0);

            if (auth.getStatus() != Status.VALID) {
                throw new AcmeException("no challenge received in " + preferences.challengeTimeout().toString().substring(2));
            }
        } catch (IOException x) {
            throw new AcmeException(x.getMessage(), x);
        }
    }

    private static void out(final Object o) {
        System.out.println(o);
        LOG.info(() -> String.valueOf(o));
    }

    private static void err(Throwable t) {
        out("Something went wrong: " + t.getMessage());
        LOG.severe(() -> ExceptionUtils.getStackTrace(t));
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
            err(x);
            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
    }

}
