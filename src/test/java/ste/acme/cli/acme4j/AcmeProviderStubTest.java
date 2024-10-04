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
package ste.acme.cli.acme4j;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.EnumSet;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import org.junit.Test;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeServerException;
import org.shredzone.acme4j.provider.AcmeProvider;
import static org.shredzone.acme4j.toolbox.AcmeUtils.base64UrlEncode;
import org.shredzone.acme4j.toolbox.JSON;
import org.shredzone.acme4j.toolbox.JoseUtils;
import static ste.xtest.Constants.BLANKS;

/**
 *
 */
public class AcmeProviderStubTest {

    @Test
    public void constructors() throws Exception {
        AcmeProviderStub p = new AcmeProviderStub();

        for (String uri: new String[] {"https://cacert1.com", "https://cacert2.com"}) {
            then(p.config.get(uri).asObject().toMap())
                .containsExactlyInAnyOrderEntriesOf(TestUtils.directoryFor(uri)
            );
            then(p.responseQueue).isEmpty();
        }
    }

    @Test
    public void accept_returns_true_only_if_scheme_is_acmetest() {
        final AcmeProviderStub S = new AcmeProviderStub();

        for (String blank : BLANKS) {
            then(S.accepts(null)).isFalse();
        }
        then(S.accepts(URI.create("http://somewhere.com"))).isFalse();
        then(S.accepts(URI.create("acmetest://somewhere.com"))).isTrue();
    }

    @Test
    public void resolve_returns_the_provided_url() throws Exception {
        final AcmeProviderStub S = new AcmeProviderStub();

        thenThrownBy(() -> S.resolve(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("uri can not be null");
        then(S.resolve(URI.create("acmetest://somewhere.com"))).isEqualTo(URI.create("https://somewhere.com").toURL());
        then(S.resolve(URI.create("http://somewhere.com"))).isEqualTo(URI.create("https://somewhere.com").toURL());

        thenThrownBy(() -> S.resolve(URI.create("/somewhere.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("uri does not contain a valid URL: URI is not absolute");
    }

    @Test
    public void withResponses_stores_responses() {
        final AcmeProviderStub S = new AcmeProviderStub();
        final AcmeResponseStub R1 = new AcmeResponseStub(100, "res1");
        final AcmeResponseStub R2 = new AcmeResponseStub(200, "res2");
        final AcmeResponseStub R3 = new AcmeResponseStub(300, "res3");

        then(S.responses()).isEmpty();

        then(S.withResponses(R1)).isSameAs(S);
        then(S.responses()).containsExactly(R1);
        S.withResponses(R2, R3);
        then(S.responses()).containsExactly(R2, R3);
    }

    @Test
    public void certificate_renew() throws Exception {
        final Instant expireOn = Instant.parse("2015-03-01T14:09:00Z");
        final Session session = new Session("acmetest:renew://cacert1.com");
        final AcmeProviderStub S = ((AcmeProviderStub)session.provider());
        final Login login = S.createLogin(session);

        Order order = login.newOrder()
                .domain("example.org")
                .notAfter(expireOn) // optional
                .create();
        order.execute(TestUtils.createKeyPair());

        then(order).isNotNull();
        then(order.getStatus()).isEqualTo(Status.PENDING);

        while (!EnumSet.of(Status.VALID, Status.INVALID).contains(order.getStatus())) {
            Thread.sleep(100);
            order.fetch();
        }

        then(order.getAuthorizations()).isEmpty();
        then(order.getError()).isEmpty();
        then(order.getExpires()).isNotEmpty().hasValue(expireOn);

        X509Certificate cert = order.getCertificate().getCertificate();
        then(cert.getSerialNumber()).isEqualTo("7031332152228940693");
        then(cert.getSubjectX500Principal().getName()).isEqualTo("CN=example.com");
    }

    @Test
    public void read_session_configuration_at_accept() throws Exception {
        AcmeProviderStub p = (AcmeProviderStub)new Session("acmetest://cacert1.com").provider();
        then(p.responseQueue).isEmpty();

        p = (AcmeProviderStub)new Session("acmetest:renew://cacert1.com").provider();
        then(p.responseQueue).containsExactly(
            new AcmeResponseStub(200, "updateOrderResponse"), new AcmeResponseStub(200, "updateOrderResponseValid"),
            new AcmeResponseStub(200, "updateOrderResponse"), new AcmeResponseStub(200, "updateOrderResponseValid"),
            new AcmeResponseStub(200, "updateOrderResponseValid")
        );

        p = (AcmeProviderStub)new Session("acmetest:dummy://cacert1.com").provider();
        then(p.responseQueue).containsExactly(
            new AcmeResponseStub(200, "updateAccountResponse"), new AcmeResponseStub(201, "requestOrderResponse"),
            new AcmeResponseStub(400, "loginError")
        );
    }

    @Test
    public void load_directory_from_resources() throws Exception {
        final Session S = new Session("acmetest://cacert1.com");
        final AcmeProvider P = S.provider();

        then(P.directory(S, P.resolve(S.getServerUri()).toURI()).toMap())
                .containsExactlyInAnyOrderEntriesOf(TestUtils.directoryFor("https://cacert1.com"));
    }

    @Test
    public void create_challange_returns_a_valid_challenge() throws Exception {
        final String TOKEN =
            "rSoI9JpyvFi-ltdnBW0W1DjKstzG7cHixjzcOjwzAEQ";

        final Session S = new Session("acmetest://cacert1.com");
        final AcmeProviderStub P = (AcmeProviderStub)new Session("acmetest://cacert1.com").provider();
        final Login L = P.createLogin(S);

        final Http01Challenge C = (Http01Challenge)P.createChallenge(
            L, JSON.parse("{\"token\":\"" + TOKEN + "\"}")
        );

        then(C.getType()).isEqualTo(Http01Challenge.TYPE);
        then(C.getStatus()).isEqualTo(Status.PENDING);
        then(C.getToken()).isEqualTo(TOKEN);
        then(C.getAuthorization()).isEqualTo(
            TOKEN + '.' + base64UrlEncode(JoseUtils.thumbprint(L.getKeyPair().getPublic()))
        );
    }

    @Test
    public void send_request_sequence() throws Exception {
        final Session S = new Session("acmetest:dummy://cacert1.com");
        final AcmeProviderStub P = (AcmeProviderStub)S.provider();

        AcmeConnectionStub C = (AcmeConnectionStub)P.connect(S.getServerUri(), null);
        then(C.sendRequest(URI.create("https://cacert1.com/dummy/1").toURL(), S, null)).isEqualTo(200);
        then(C.readJsonResponse().toMap()).containsExactlyEntriesOf(TestUtils.getJSON("updateAccountResponse").toMap());

        C = (AcmeConnectionStub)P.connect(S.getServerUri(), null);
        then(C.sendRequest(URI.create("https://cacert1.com/dummy/2").toURL(), S, null)).isEqualTo(201);
        then(C.readJsonResponse().toMap()).containsExactlyEntriesOf(TestUtils.getJSON("requestOrderResponse").toMap());

        AcmeConnectionStub C1 = (AcmeConnectionStub)P.connect(S.getServerUri(), null);
        thenThrownBy(() -> {
            C1.sendRequest(URI.create("https://cacert1.com/dummy/3").toURL(), S, null);
        }).isInstanceOf(AcmeServerException.class)
        .hasMessage("No account exists with the provided key");
    }
}
