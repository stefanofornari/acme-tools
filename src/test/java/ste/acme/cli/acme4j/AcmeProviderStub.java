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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.Security;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.connector.Connection;
import org.shredzone.acme4j.connector.NetworkSettings;
import static org.shredzone.acme4j.connector.Resource.NEW_ACCOUNT;
import org.shredzone.acme4j.provider.AcmeProvider;
import org.shredzone.acme4j.toolbox.JSON;
import org.shredzone.acme4j.util.KeyPairUtils;
import static ste.acme.cli.acme4j.TestUtils.getJSON;

/**
 *
 */
public class AcmeProviderStub implements AcmeProvider {
    public static final String SCHEME = "acmetest";

    protected final JSON config;
    protected final Deque<AcmeResponseStub> responseQueue = new ArrayDeque();


    public AcmeProviderStub() {
        Security.addProvider(new BouncyCastleProvider());
        config = TestUtils.getJSON("directory");
    }

    public Collection<AcmeResponseStub> responses() {
        return responseQueue;
    }

    public AcmeProviderStub withResponses(AcmeResponseStub... responses) {
        responseQueue.clear(); responseQueue.addAll(List.of(responses));

        return this;
    }

    /**
     * Creates a {@link Login} that uses this {@link AcmeProvider}.
     */
    public Login createLogin(final Session session) throws IOException {
        return session.login(
            URI.create(
                directory(session, session.getServerUri()).get(NEW_ACCOUNT.path()).asString()
            ).toURL(),
            KeyPairUtils.createKeyPair()
        );
    }

    @Override
    public Connection connect(URI serverUri, NetworkSettings networkSettings) {
        return new AcmeConnectionStub(resolve(serverUri), responseQueue.poll());
    }

    @Override
    public JSON directory(Session session, URI serverUri) {
        return config.get(resolve(serverUri).toString()).asObject();
    }

    @Override
    public boolean accepts(final URI uri) {
        if (uri == null) {
            return false;
        }
        final String scheme = uri.getScheme();

        if ((scheme == null) || !scheme.equals(SCHEME)) {
            return false;
        }

        //
        // Scheme is here accepted. If a subscheme is provided, it is used to
        // load this provider configuration from the resources
        //
        final String schemeSpecific = uri.getSchemeSpecificPart();
        final int pos = schemeSpecific.indexOf("://");
        if (pos > 0) {
            //
            // we have a subscheme
            //
            JSON stubs = TestUtils.getJSON("stubs/" + schemeSpecific.substring(0, pos));
            stubs.get("responseQueue").asArray().forEach((stub) -> {
                JSON response = stub.asObject();
                responseQueue.offer(
                    new AcmeResponseStub(response.get("status").asInt(), response.get("resource").asString())
                );
            });
        }

        return true;
    }

    @Override
    public URL resolve(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri can not be null");
        }

        final String schemeSpecific = uri.getSchemeSpecificPart();
        try {
            int pos = schemeSpecific.indexOf("//");
            return (pos >= 0) ?
                URI.create("https:" + schemeSpecific.substring(pos)).toURL() :
                uri.toURL();
        } catch (Exception x) {
            throw new IllegalArgumentException("uri does not contain a valid URL: " + x.getMessage());
        }
    }

    @Override
    public Challenge createChallenge(Login login, JSON data) {
        return new Http01Challenge(login, getJSON("httpChallenge"));
    }
}
