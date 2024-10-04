ACME TOOLS
===
Command-line (for now) tools to deal with ACME certificates

# acme-tools
```
Usage: acme-tools [--help] [--version] [COMMAND]
      --help      display this help and exit
      --version   show version information
Commands:
  new-account  create a new account
  renew        renew a previously created certificate
```

To create a new account
====
```
Usage: acme-tools new-account [--account-keys=<accountFile>]
                              [--contact=<email>] <endpoint>
create a new account
      <endpoint>          ACME CA endpoint or URI e.g. https://someca.com, acme:
                            //example.org/staging
      --account-keys=<accountFile>
                          optional account keys file
      --contact=<email>   optional contact email to provide to the CA
```

To renew a certificate
====
```
Usage: acme-tools renew [--account-keys=<account>]
                        [--certificate=<certificate>]
                        [--challenge-timeout=<challengeTimeout>]
                        [--domain-keys=<domain>]
                        [--polling-interval=<pollingInterval>] [--port=<port>]
                        <endpoint> <domain>
renew a previously created certificate
      <endpoint>      ACME CA endpoint or URI e.g. https://someca.com, acme:
                        //example.org/staging
      <domain>        the domain to renew the certificate for
      --account-keys=<account>
                      optional account keys file
      --certificate=<certificate>
                      optional certificate file
      --challenge-timeout=<challengeTimeout>
                      max time to wait for a challenge in human readable form
                        (e.g. 1m 30s)
      --domain-keys=<domain>
                      optional domain keys file
      --polling-interval=<pollingInterval>
                      interval in millisecond used when polling for events
      --port=<port>   tcp port to use to listen for CA challenge request; if
                        not provided an available port will be picked randomly
```

Credits
====

Based on [acme4j](https://github.com/shred/acme4j).
[Find short URL for you CA here](https://shredzone.org/maven/acme4j/ca/index.html).