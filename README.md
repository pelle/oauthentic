# oauthentic

Lightweight OAuth 2 client library for Clojure.

This library only handles the authorization aspects of OAuth 2. 

We recommend using [clj-http](https://github.com/dakrone/clj-http) for performing actual authenticated requests once you have a token.

## Usage

Add the following to your project.clj's dependencies section:

  [oauthentic "0.0.1"]

Import the library:

  (use 'oauthentic.core)

### Obtain authorization

OAuth 2 will in most cases require you to send your user to an authorization url.

You will need a client id, the services authorization url and URL for the service to redirect the user back to:

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

###

## License

Copyright (C) 2012 [Pelle Braendgaard](http://stakeventures.com) and [PicoMoney](http://picomoney.com)

Distributed under the Eclipse Public License, the same as Clojure.
