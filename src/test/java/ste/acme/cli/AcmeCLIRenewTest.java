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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.Ignore;
import org.junit.Test;
import org.shredzone.acme4j.connector.Resource;
import static ste.acme.cli.Format.PKCS12;
import ste.xtest.concurrent.WaitFor;
import ste.xtest.net.NetTools;

/**
 *
 */
public class AcmeCLIRenewTest extends AcmeCLIExec {

    private static final char[] PASSWORD = "keystore".toCharArray();

    @Test
    public void renew_acme_certificate_with_defaults() throws Exception {
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);

        execJava("renew", "acmetest:renew://cacert1.com", "mydomain.com");

        //System.out.println(err());
        //System.out.println(out());

        then(out())
            .contains("Renewing SSL certificates for domain mydomain.com from https://cacert1.com/" + Resource.NEW_ORDER)
            .contains("using account credentials in " + new File(HOME, "account.pem").getAbsolutePath())
            .contains("using domain credentials in " + new File(HOME, "domain.pem").getAbsolutePath())
            .contains("storing the new certificate in " + new File(HOME, "domain.crt").getAbsolutePath())
            ;

        then(new File(HOME, "domain.crt")).exists().hasContent(
            IOUtils.resourceToString("/cert.pem", Charset.defaultCharset())
        );
    }

    @Test
    public void renew_acme_certificate_with_parameters() throws Exception {
        //
        // default domain, output
        //

        //
        // Prepare file system
        //
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);
        FileUtils.moveFile(new File(HOME, "account.pem"), new File(HOME, "account2.pem"));

        execJava(
            "renew", "acmetest:renew://cacert1.com", "mydomain.com",
            "--account-keys", "account2.pem"
        );

        System.out.println(err());
        System.out.println(out());

        then(out())
            .contains("Renewing SSL certificates for domain mydomain.com from https://cacert1.com/" + Resource.NEW_ORDER)
            .contains("using account credentials in " + new File(HOME, "account2.pem").getAbsolutePath())
            .contains("using domain credentials in " + new File(HOME, "domain.pem").getAbsolutePath())
            .contains("storing the new certificate in " + new File(HOME, "domain.crt").getAbsolutePath())
            ;

        then(new File(HOME, "domain.crt")).exists().hasContent(
            IOUtils.resourceToString("/cert.pem", Charset.defaultCharset())
        );

        //
        // default account, output
        //

        //
        // Prepare file system
        //
        FileUtils.deleteDirectory(HOME);
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);
        FileUtils.moveFile(new File(HOME, "domain.pem"), new File(HOME, "domain2.pem"));

        execJava(
            "renew", "acmetest:renew://cacert1.com", "mydomain.com",
            "--domain-keys", "domain2.pem"
        );

        then(out())
            .contains("Renewing SSL certificates for domain mydomain.com from https://cacert1.com/" + Resource.NEW_ORDER)
            .contains("using account credentials in " + new File(HOME, "account.pem").getAbsolutePath())
            .contains("using domain credentials in " + new File(HOME, "domain2.pem").getAbsolutePath())
            .contains("storing the new certificate in " + new File(HOME, "domain.crt").getAbsolutePath())
            ;

        then(new File(HOME, "domain.crt")).exists().hasContent(
            IOUtils.resourceToString("/cert.pem", Charset.defaultCharset())
        );

        //
        // default account, domain
        //

        //
        // Prepare file system
        //
        FileUtils.deleteDirectory(HOME);
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);

        execJava(
            "renew", "acmetest:renew://cacert1.com", "mydomain.com",
            "--out", "newcert.crt"
        );

        //System.out.println(err());
        //System.out.println(out());

        then(out())
            .contains("Renewing SSL certificates for domain mydomain.com from https://cacert1.com/" + Resource.NEW_ORDER)
            .contains("using account credentials in " + new File(HOME, "account.pem").getAbsolutePath())
            .contains("using domain credentials in " + new File(HOME, "domain.pem").getAbsolutePath())
            .contains("storing the new certificate in " + new File(HOME, "newcert.crt").getAbsolutePath())
            ;

        then(new File(HOME, "newcert.crt")).exists().hasContent(
            IOUtils.resourceToString("/cert.pem", Charset.defaultCharset())
        );

        //
        // no defaults
        //

        //
        // Prepare file system
        //
        FileUtils.deleteDirectory(HOME);
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);
        FileUtils.moveFile(new File(HOME, "account.pem"), new File(HOME, "account2.pem"));
        FileUtils.moveFile(new File(HOME, "domain.pem"), new File(HOME, "domain2.pem"));

        execJava(
            "renew", "acmetest:renew://cacert1.com", "mydomain.com",
            "--domain-keys", "domain2.pem", "--account-keys", "account2.pem", "--out", "newcert.crt"
        );

        then(out())
            .contains("Renewing SSL certificates for domain mydomain.com from https://cacert1.com/" + Resource.NEW_ORDER)
            .contains("using account credentials in " + new File(HOME, "account2.pem").getAbsolutePath())
            .contains("using domain credentials in " + new File(HOME, "domain2.pem").getAbsolutePath())
            .contains("storing the new certificate in " + new File(HOME, "newcert.crt").getAbsolutePath())
            ;

        then(new File(HOME, "newcert.crt")).exists().hasContent(
            IOUtils.resourceToString("/cert.pem", Charset.defaultCharset())
        );
    }

    @Test
    public void renew_with_challenge() throws Exception {
        //
        // Prepare file system
        //
        FileUtils.deleteDirectory(HOME);
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);

        //
        // Run the tool, the output shall tell us how to satisfy the challenge
        //
        final Process P = startJava(
            "renew", "acmetest:renew-with-challenge://cacert1.com", "mydomain.com",
            "--out", "newcert.crt",
            "--polling-interval", "1000"
        );

        Thread.sleep(750); // let the server start

        //System.out.println(err());
        //System.out.println(out());

        String out = out();

        then(out)
            .contains("HTTP challenge")
            .contains("Listener started on port ")
            .contains("http://mydomain.com/.well-known/acme-challenge/rSoI9JpyvFi-ltdnBW0W1DjKstzG7cHixjzcOjwzAEQ");

        //
        // Retrieve the challenge
        //
        final String port = extractChallengePort(out);
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder().uri(
            URI.create("http://localhost:" + port + "/.well-known/acme-challenge/rSoI9JpyvFi-ltdnBW0W1DjKstzG7cHixjzcOjwzAEQ")
        ).build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        then(response.statusCode()).isEqualTo(200);
        then(response.body()).isNotBlank();

        final File cert = new File(HOME, "newcert.crt");
        P.waitFor(5, TimeUnit.SECONDS);

        then(out()).contains("Finalizing the order with the CA")
                   .contains("Order processed, getting the certificate")
                   .contains("Writing the certificate to " + cert.getAbsolutePath())
                   .contains("Congratulations! Your renewed certificated is ready.");

        then(cert).hasContent(
            IOUtils.resourceToString("/cert.pem", Charset.defaultCharset())
        );

        //
        // The process will not exit if any daemon thread is still running. We
        // want to make sure the the server used for the challenge is properly
        // shut down.
        //
        P.waitFor(5, TimeUnit.SECONDS);
    }

    @Test
    public void renew_with_challenge_on_given_port() throws Exception {
        //
        // Prepare file system
        //
        FileUtils.deleteDirectory(HOME);
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);

        //
        // Pick an available port. Note that race conditions are possible...
        //
        final int PORT = new NetTools().pickAvailablePort();

        //
        // Run the tool, the output shall tell us how to satisfy the challenge
        //
        final Process P = startJava(
            "renew", "acmetest:renew-with-challenge://cacert1.com", "mydomain.com",
            "--out", "newcert.crt",
            "--polling-interval", "1000", "--port", String.valueOf(PORT)
        );

        Thread.sleep(1000); // let the server start

        //System.out.println(err());
        //System.out.println(out());

        then(out()).contains("Listener started on port " + PORT);

        //
        // Retrieve the challenge
        //
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder().uri(
            URI.create("http://localhost:" + PORT + "/.well-known/acme-challenge/rSoI9JpyvFi-ltdnBW0W1DjKstzG7cHixjzcOjwzAEQ")
        ).build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        then(response.statusCode()).isEqualTo(200);
        then(response.body()).isNotBlank();

        final File cert = new File(HOME, "newcert.crt");
        new WaitFor(5000, () -> { return cert.exists(); } );

        then(cert).hasContent(
            IOUtils.resourceToString("/cert.pem", Charset.defaultCharset())
        );

        //
        // The process will not exit if any daemon thread is still running. We
        // want to make sure the the server used for the challenge is properly
        // shut down.
        //
        P.waitFor(5, TimeUnit.SECONDS);
    }

    @Test
    public void renew_with_challenge_timeout() throws Exception {
        //
        // Prepare file system
        //
        FileUtils.deleteDirectory(HOME);
        FileUtils.copyDirectory(new File("src/test/data/default"), HOME);

        //
        // Run the tool, the output shall tell us how to satisfy the challenge
        //
        final Process P = startJava(
            "renew", "acmetest:renew-with-missing-challenge://cacert1.com", "mydomain.com",
            "--out", "newcert.crt",
            "--polling-interval", "1000", "--challenge-timeout", "500ms"
        );

        P.waitFor(5, TimeUnit.SECONDS);

        //System.out.println(err());
        //System.out.println(out());

        then(out()).contains("Unsuccessful challenge: no challenge received in 0.5S");
        then(new File(HOME, "newcert.crt")).doesNotExist();
    }

    @Test
    public void renew_and_store_in_p12_keystore() throws Exception {
        final File KEYSTORE = new File(HOME, "keystore.p12");
        final String SECRET = "1234567890";

        AcmeCLI.main(
            "renew", "acmetest:renew://cacert1.com", "mydomain.com",
            "--account-keys", "src/test/data/default/account.pem",
            "--domain-keys", "src/test/data/default/domain.pem",
            "--out", KEYSTORE.getAbsolutePath(), "--format", PKCS12.toString(), "--secret", SECRET
        );

        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(new FileInputStream(KEYSTORE), SECRET.toCharArray());

        then(STDOUT.getLog()).contains("Congratulations! Your renewed certificated is ready.");

        then(keyStore.containsAlias("mydomain.com")).isTrue();
    }

    @Test
    public void renew_and_store_in_p12_keystore_with_missing_password() throws Exception {
        final File KEYSTORE = new File(HOME, "keystore.p12");
        final String SECRET = "1234567890";

        AcmeCLI.main(
            "renew", "acmetest:renew://cacert1.com", "mydomain.com",
            "--account-keys", "src/test/data/default/account.pem",
            "--domain-keys", "src/test/data/default/domain.pem",
            "--out", KEYSTORE.getAbsolutePath(), "--format", PKCS12.toString()
        );

        then(STDOUT.getLog()).contains("A keystore password must be provided for output " + PKCS12 + " (use " + Constants.OPT_SECRET + ")");
        then(KEYSTORE).doesNotExist();
    }

    @Ignore
    @Test
    public void testSomeMethod() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        PEMParser pemParser = new PEMParser(new FileReader("/opt/uzz-api-0.0.1-SNAPSHOT/config/uzz.fornari.net.pem"));
        //PEMParser pemParser = new PEMParser(new FileReader("/opt/uzz-api-0.0.1-SNAPSHOT/config/letsencrypt-8bca605269b3fe4e71909d77bf5d9a86.pem"));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PEMKeyPair object = (PEMKeyPair)pemParser.readObject();
        System.out.println("prvate info: " + object.getPrivateKeyInfo().getAttributes());
        System.out.println("public info: " + object.getPublicKeyInfo().getPublicKeyData());
        KeyPair kp = converter.getKeyPair(object);
        PrivateKey privateKey = kp.getPrivate();

        System.out.println(privateKey);
        pemParser.close();

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List<Certificate> chainList = new ArrayList<>();

        // Load the main certificate
        try (FileInputStream fis = new FileInputStream("/opt/uzz-api-0.0.1-SNAPSHOT/config/domain.crt")) {
            chainList.add(cf.generateCertificate(fis));
        }

        // Convert list to array
        Certificate[] chain = chainList.toArray(new Certificate[0]);

        // Create PKCS12 keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(null, null);
        keyStore.setKeyEntry("tomcat", privateKey, PASSWORD, chain);

        // Save keystore to file
        try (FileOutputStream fos = new FileOutputStream("keystore.p12")) {
            keyStore.store(fos, PASSWORD);
        }
    }

    // --------------------------------------------------------- private methods

    private String extractChallengePort(final String s) throws Exception {
        Pattern p = Pattern.compile("Listener started on port (\\d+)\\n");
        Matcher m = p.matcher(s);

        if (m.find()) {
            return m.group(1);
        }

        throw new Exception("challenge uri not found");
    }

}
