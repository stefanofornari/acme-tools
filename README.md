# acme-tools
Command-line (for now) tools to deal with ACME certificates

```
Usage: acme-tools renew [--account=<account>] [--certificate=<certificate>]
                        [--challenge-timeout=<challengeTimeout>]
                        [--domain=<domain>]
                        [--polling-interval=<pollingInterval>] [--port=<port>]
                        <endpoint>
renew a previously created certificate
      <endpoint>            ACME CA endpoint or URI e.g. https://someca.com,
                              acme://example.org/staging
      --account=<account>   optional account keys file
      --certificate=<certificate>
                            optional certificate file
      --challenge-timeout=<challengeTimeout>
                            max time to wait for a challenge in human readable
                              form (e.g. 1m 30s)
      --domain=<domain>     optional domain keys file
      --polling-interval=<pollingInterval>
                            interval in millisecond used when polling for events
      --port=<port>         tcp port to use to listen for CA challenge request;
                              if not provided an available port will be picked
                              randomly
```
Based on [acme4j](https://github.com/shred/acme4j).
[Find short URL for you CA here](https://shredzone.org/maven/acme4j/ca/index.html).