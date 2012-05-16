# oauthentic

Lightweight [OAuth 2](http://tools.ietf.org/html/draft-ietf-oauth-v2-26) client library for Clojure.

This library only handles the authorization aspects of OAuth 2. 

We recommend using [clj-http](https://github.com/dakrone/clj-http) for performing actual authenticated requests once you have a token.

[![Build Status](https://secure.travis-ci.org/pelle/oauthentic.png)](http://travis-ci.org/pelle/oauthentic)

## Usage

Add the following to your project.clj's dependencies section:

    [oauthentic "0.0.5"]

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

### PicoMoney

    => (build-authorization-url "https://picomoney.com/oauth/authorize" { :client-id "INSERT YOUR OWN ID" :redirect-uri "http://yoursite.com/oauth/endpoint"})
    "https://picomoney.com/oauth/authorize?redirect_uri=http%253A%252F%252Fyoursite.com%252Foauth%252Fendpoint&response_type=code&client_id=INSERT+YOUR+OWN+ID"

### FaceBook

    => (build-authorization-url "https://www.facebook.com/dialog/oauth" { :client-id "INSERT YOUR OWN ID" :redirect-uri "http://yoursite.com/oauth/endpoint" })
    "https://www.facebook.com/dialog/oauth?redirect_uri=http%253A%252F%252Fyoursite.com%252Foauth%252Fendpoint&response_type=code&client_id=INSERT+YOUR+OWN+ID"

### GitHub

    => (build-authorization-url "https://github.com/login/oauth/authorize" { :client-id "INSERT YOUR OWN ID" :redirect-uri "http://yoursite.com/oauth/endpoint" })
    "https://github.com/login/oauth/authorize?redirect_uri=http%253A%252F%252Fyoursite.com%252Foauth%252Fendpoint&response_type=code&client_id=INSERT+YOUR+OWN+ID"

You can also call it with the first parameter being a map containing information about the server. This map should contain the following keys:

- :authorization-url
- :client-id

You still pass the request specific parameters such as redirect-uri and scope in the second map.

    => (build-authorization-url { :authorization-url "https://picomoney.com/oauth/authorize" 
                                  :client-id "INSERT YOUR OWN ID"}
                                { :redirect-uri "http://yoursite.com/oauth/endpoint"})
    "https://picomoney.com/oauth/authorize?redirect_uri=http%253A%252F%252Fyoursite.com%252Foauth%252Fendpoint&response_type=code&client_id=INSERT+YOUR+OWN+ID"



You can either redirect the user to to the URL or use it as a link.

The user will be redirected back to the URL you provided in redirect_uri with a code in the http query parameters or an error code. If you received the code proceed to...

### Obtain Token with Authorization Code

To obtain a token you need the code you need the following parameters:

- code (from above step)
- redirect-uri (the same you used in the above step)
- client_id (get this from the provider)
- client_secret (get this from the provider)
- token url (get this from the provider)

In theory the following examples should work:

    ; PicoMoney
    => (fetch-token "https://picomoney.com/oauth/token" { :client-id "INSERT YOUR OWN ID" :client-secret "INSERT YOUR OWN SECRET" :code code :redirect-uri "INSERT YOUR ENDPOINT HERE"})
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}

    ; Facebook
    => (fetch-token "https://graph.facebook.com/oauth/access_token" { :client-id "INSERT YOUR OWN ID" :client-secret "INSERT YOUR OWN SECRET" :code code :redirect-uri "INSERT YOUR ENDPOINT HERE"})
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}

    ; GitHub
    => (fetch-token "https://github.com/login/oauth/access_token" { :client-id "INSERT YOUR OWN ID" :client-secret "INSERT YOUR OWN SECRET" :code code :redirect-uri "INSERT YOUR ENDPOINT HERE"})
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}

You can also call it with the first parameter being a map containing information about the server. This map should contain the following keys:

- :token-url
- :client-id
- :client-secret

You still need to pass request specific details in the second map, such as :code and :redirect-uri.

    => (fetch-token { :token-url "https://picomoney.com/oauth/token"
                      :client-id "INSERT YOUR OWN ID" 
                      :client-secret "INSERT YOUR OWN SECRET"}
                    { :code code :redirect-uri "INSERT YOUR ENDPOINT HERE"})
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}


### Obtain Token with Client Credentials

To obtain a token for your own application you can skip the authorization flow completely and use this [method](http://tools.ietf.org/html/draft-ietf-oauth-v2-26#section-4.4.

You need the following parameters:

- client_id (get this from the provider)
- client_secret (get this from the provider)
- token url (get this from the provider)

    ; PicoMoney
    => (fetch-token "https://picomoney.com/oauth/token" { :client-id "INSERT YOUR OWN ID" :client-secret "INSERT YOUR OWN SECRET" })
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}

    ; Facebook
    => (fetch-token "https://graph.facebook.com/oauth/access_token" { :client-id "INSERT YOUR OWN ID" :client-secret "INSERT YOUR OWN SECRET" })
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}

    ; GitHub
    => (fetch-token "https://github.com/login/oauth/access_token" { :client-id "INSERT YOUR OWN ID" :client-secret "INSERT YOUR OWN SECRET" })
    {:access-token "TOKEN FROM SERVICE" :token-type "bearer"}

### Ring Handler

There is a Ring Handler for automatically handling the above flow in a similar manner to Ruby's OmniAuth.

It lives in oauthentic.ring and can be installed like this:

    (def fb-login-handler (oauthentic.ring/oauthentic-handler login-handler error-handler { :authorization-url "https://github.com/login/oauth/authorize" 
                                                :token-url "https://github.com/login/oauth/access_token"
                                                :client-id "INSERT YOUR OWN ID" 
                                                :client-secret "INSERT YOUR OWN SECRET" })

Install the handler in your routes however you like.

Login function is a ring handler function that is passed the request with the token as :oauthentic-token in the request.

It should knows how to correctly set the session verify user based on the user database etc. As it's a ring handler it should return a correct response.

Error handler is another ring handler that is called if an error is returned from the service.

Default dumb implementations are available and can be used by leaving them out.

## License

Copyright (C) 2012 [Pelle Braendgaard](http://stakeventures.com) and [PicoMoney](http://picomoney.com)

Distributed under the Eclipse Public License, the same as Clojure.
