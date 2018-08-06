# keycloak-soteria
Use KeyCloak as your Soteria Authentication Provider

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

## Requirements

This project is built using KeyCloak 4.2.1 and Soteria 1.0.  All development and testing 
is performed against [Wildfly 11](https://www.wildfly.org), but should work on subsequent 
verisons with no problem.  The library itself is packaged via Maven as an EJB JAR.

If you already have an existing KeyCloak server, you may certainly use that.  Otherwise, this 
project includes Docker Compose files to stand up your own KeyCloak environment with 
a Realm called *Soteria* that you can use.  This instance leverage PostgreSQL as its 
backend database.  It comes pre-configured with some user accounts.  All that you need to do is configure your KeyCloak Client for use with your 
Java EE application.

## Building the Library

Building the `keycloak-soteria` library is as simple as running `mvn install`.  All 
dependencies are defined within the `pom.xml` file.  Once it is built, you can include 
the resulting JAR file within your application.

## Configuring a KeyCloak Client

## Integrating with a Java EE Application

