# keycloak-soteria
Using KeyCloak as your Soteria Authentication Provider

## Introduction

[KeyCloak](https://www.keycloak.org) is an open-source project that provides identity management, 
SSO, identity federation, and more.  The project originated with RedHat, and recently 
merged with the PicketLink project to expand its native capabilities.  The [Soteria](https://github.com/eclipse-ee4j/soteria) 
project serves as the reference implementation for [JSR 375](https://jcp.org/en/jsr/detail?id=375). 
 Its primary purpose is to provide a modern security API for Java EE applications.

This project aims to bridge the two by creating a custom [HttpAuthenticationMechanism](https://javaee.github.io/security-api/apidocs/javax/security/enterprise/authentication/mechanism/http/HttpAuthenticationMechanism.html) 
that leverages KeyCloak for [OpenID Connect](http://openid.net/connect) authentication. 
 KeyCloak will serve as the underlying identity and authentication provider.  When 
 authentication is required, the application will redirect to KeyCloak with the necessary 
 parameters to begin the federated authentication.