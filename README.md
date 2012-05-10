# oauthentic

Lightweight [OAuth 2](http://tools.ietf.org/html/draft-ietf-oauth-v2-26) client library for Clojure.

This library only handles the authorization aspects of OAuth 2. 

We recommend using [clj-http](https://github.com/dakrone/clj-http) for performing actual authenticated requests once you have a token.

[![Build Status](https://secure.travis-ci.org/pelle/oauthentic.png)](http://travis-ci.org/pelle/oauthentic)

## Usage

Add the following to your project.clj's dependencies section:

    [oauthentic "0.0.1"]

Import the library:

    (use 'oauthentic.core)

### Obtain authorization

OAuth 2 will in most cases require you to send your user to an authorization url.

In almost all cases you will want to use the [Authorization Code Flow](http://tools.ietf.org/html/draft-ietf-oauth-v2-26#section-4.1) to obtain authorization and a token.

This uses a 2 step approach:

1. Redirect user to services authorization url
2. User is redirected back to your site with an authorization code, which you exchange for a token.

To obtain authorization you need the following:

- client id (get this from the provider)
- the services authorization url (get this from the provider)
- redirect-uri A URL for an endpoint on your site that handles the 2nd phase above

    # PicoMoney
    => (build-authorization-url "https://picomoney.com/oauth/authorize" { :client-id "INSERT YOUR OWN ID" :redirect-uri "http://yoursite.com/oauth/endpoint"})
    "https://picomoney.com/oauth/authorize?redirect_uri=http%253A%252F%252Fyoursite.com%252Foauth%252Fendpoint&response_type=code&client_id=INSERT+YOUR+OWN+ID"

    # FaceBook
    => (build-authorization-url "https://www.facebook.com/dialog/oauth" { :client-id "INSERT YOUR OWN ID" :redirect-uri "http://yoursite.com/oauth/endpoint" })
    "https://www.facebook.com/dialog/oauth?redirect_uri=http%253A%252F%252Fyoursite.com%252Foauth%252Fendpoint&response_type=code&client_id=INSERT+YOUR+OWN+ID"

    # GitHub
    => (build-authorization-url "https://github.com/login/oauth/authorize" { :client-id "INSERT YOUR OWN ID" :redirect-uri "http://yoursite.com/oauth/endpoint" })
    "https://github.com/login/oauth/authorize?redirect_uri=http%253A%252F%252Fyoursite.com%252Foauth%252Fendpoint&response_type=code&client_id=INSERT+YOUR+OWN+ID"

You can either redirect the user to to the URL or use it as a link.

The user will be redirected back to the URL you provided in redirect_uri with a code in the http query parameters or an error code. If you received the code proceed to...

### Obtain Token with Authorization Code

To obtain a token you need the code you need the following parameters:

- code (from above step)
- redirect-uri (the same you used in the above step)
- client_id (get this from the provider)
- client_secret (get this from the provider)
- token url (get this from the provider)

    # PicoMoney
    => (fetch-token "https://picomoney.com/oauth/token" { :client-id "INSERT YOUR OWN ID" :client-secret "INSERT YOUR OWN SECRET" :code code})
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}
  
### Obtain Token with Client Credentials

To obtain a token for your own application you can skip the authorization flow completely and use this [method](http://tools.ietf.org/html/draft-ietf-oauth-v2-26#section-4.4.

You need the following parameters:

- client_id (get this from the provider)
- client_secret (get this from the provider)
- token url (get this from the provider)

    # PicoMoney
    => (fetch-token "https://picomoney.com/oauth/token" { :client-id "INSERT YOUR OWN ID" :client-secret "INSERT YOUR OWN SECRET" })
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}


## License

Copyright (C) 2012 [Pelle Braendgaard](http://stakeventures.com) and [PicoMoney](http://picomoney.com)

Distributed under the Eclipse Public License, the same as Clojure.
