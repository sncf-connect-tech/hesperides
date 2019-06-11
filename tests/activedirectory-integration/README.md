Those tests require a real ActiveDirectory instance to connect to.

They use the BDD _features_ defined in the `tests/bdd` module and tagged `@require-real-ad` or `@auth-related`.

## Execution

    mvn verify -pl tests/activedirectory-integration -Dauth.lambdaUserName=... -Dauth.lambdaUserPassword=... -Dauth.lambdaUserParentGroupDN=... \
                                                     -Dauth.prodUserName=... -Dauth.prodUserPassword=... \
                                                     -Djavax.net.ssl.trustStore=.../path/to/certificates/trustore
