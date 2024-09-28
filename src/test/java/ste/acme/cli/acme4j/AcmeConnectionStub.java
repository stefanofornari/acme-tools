/*
 * acme4j - Java ACME client
 *
 * Copyright (C) 2015 Richard "Shred" Körber
 *   http://acme4j.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ste.acme.cli.acme4j;

import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.connector.Connection;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.toolbox.JSON;
import org.shredzone.acme4j.toolbox.JSONBuilder;

/**
 * Dummy implementation of {@link Connection} that always fails. Single methods are
 * supposed to be overridden for testing.
 */
public class AcmeConnectionStub implements Connection {

    private static final String MIME_JSON = "application/json";
    private static final String MIME_CERTIFICATE_CHAIN = "application/pem-certificate-chain";

    private static final String ACMESTUB_NONCE = "anonce";

    private final URL locationUrl;
    private final String resource;

    public AcmeConnectionStub(final URL locationUrl, final String resource) {
        this.locationUrl = locationUrl;
        this.resource = resource;

        System.out.println("new connection for " + locationUrl + " with resource " + resource);
    }

    @Override
    public void resetNonce(Session session) {
        session.setNonce(ACMESTUB_NONCE);
    }

    @Override
    public int sendRequest(URL url, Session session, ZonedDateTime ifModifiedSince) {
        return 200;
    }

    @Override
    public int sendCertificateRequest(URL url, Login login)
    throws AcmeException {
        return performRequest(url, null, login.getSession(), login.getKeyPair(),
                login.getAccountLocation(), MIME_CERTIFICATE_CHAIN);
    }

    @Override
    public int sendSignedPostAsGetRequest(URL url, Login login)
    throws AcmeException {
        return performRequest(url, null, login.getSession(), login.getKeyPair(),
                login.getAccountLocation(), MIME_JSON);
    }

    @Override
    public int sendSignedRequest(URL url, JSONBuilder claims, Login login)
    throws AcmeException {
        return performRequest(url, claims, login.getSession(), login.getKeyPair(),
                login.getAccountLocation(), MIME_JSON);
    }

    @Override
    public int sendSignedRequest(URL url, JSONBuilder claims, Session session, KeyPair keypair)
    throws AcmeException {
        return performRequest(url, claims, session, keypair, null, MIME_JSON);
    }

    @Override
    public JSON readJsonResponse() {
        return TestUtils.getJSON(resource);
    }

    @Override
    public List<X509Certificate> readCertificates() {
        try {
            return TestUtils.createCertificate("/cert.pem");
        } catch (IOException x) {
            x.printStackTrace();
        }

        return List.of();
    }

    @Override
    public Optional<Instant> getRetryAfter() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getNonce() {
        return Optional.of(ACMESTUB_NONCE);
    }

    @Override
    public URL getLocation() {
        return locationUrl;
    }

    @Override
    public Optional<ZonedDateTime> getLastModified() {
        return Optional.empty();
    }

    @Override
    public Optional<ZonedDateTime> getExpiration() {
        return Optional.empty();
    }

    @Override
    public Collection<URL> getLinks(String relation) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void close() {
        // closing is always safe
    }

    // --------------------------------------------------------- private methids

    private int performRequest(URL url, JSONBuilder claims, Session session,
        KeyPair keypair, URL accountLocation, String accept)
    throws AcmeException {
        System.out.println("performRequest " + url + " with session " + session);
        return 200;
    }

}
