# keycloak-soteria
Use KeyCloak as your Soteria Authentication Provide

## Introduction

[KeyCloak](https://www.keycloak.org) is an open-source project that provides identity 
management, SSO, identity federation, and more.  The project originated with RedHat, 
and recently merged with the PicketLink project to expand its native capabilities.  
The [Soteria](https://github.com/eclipse-ee4j/soteria) project serves as the reference 
implementation for [JSR 375](https://jcp.org/en/jsr/detail?id=375). Its primary purpose 
is to provide a modern security API for Java EE applications

This project aims to bridge the two by creating a custom [HttpAuthenticationMechanism](https://javaee.github.io/security-api/apidocs/javax/security/enterprise/authentication/mechanism/http/HttpAuthenticationMechanism.html) 
that leverages KeyCloak for [OpenID Connect](http://openid.net/connect) authentication. 
KeyCloak will serve as the underlying identity and authentication provider.  When 
authentication is required, the application will redirect to KeyCloak with the necessary 
parameters to begin the federated authentication

## Requirements

This project is built using KeyCloak 4.4.0 and Soteria 1.0.  All development and 
testing is performed against [Wildfly 11](https://www.wildfly.org), but should work 
on subsequent verisons with no problem.  The library itself is packaged via Maven 
as an EJB JAR

If you already have an existing KeyCloak server, you may certainly use that.  Otherwise, 
this project includes Docker Compose files to stand up your own KeyCloak environment 
with a Realm called *Soteria* that you can use.  This instance leverage [PostgreSQL](https://www.postgresql.org) 
as its backend database.  It comes pre-configured with some user accounts.  The KeyCloak 
client is also defined, with the client configuration found in the [keycloak.json](https://github.com/sdnakhla/keycloak-soteria/blob/master/src/client/keycloak.json) 
file. This file is necessary when creating your Java EE application.  This KeyCloak 
client assumes that everything is running on `localhost`, with the path to your Java 
EE application being `http://localhost:8080/soteria-test`.

# Installation and Configuration

## Building the Library

Building the `keycloak-soteria` library is as simple as running `mvn install`.  All 
dependencies are defined within the `pom.xml` file.  Once it is built, you can include 
the resulting JAR file within your application

## Integrating with a Java EE Application

*NOTE:*  These instructions explain how to configure your Java EE web application 
to integrate with Soteria/KeyCloak on Wildfly 11.  Other app servers -- Payara, MicroProfile, 
Wildfly Swarm, etc. -- may have different configuration requirements

Integrating this library *does not* require you to configure your Wildfly instance 
with the KeyCloak subsystem.  While you may want to do that for other reasons, this 
library allows you to leverage KeyCloak without doing so.  First, you will need to 
modify your `WEB-INF/jboss-web.xml` descriptor file to specify the `jaspitest` security 
domain. It should look like this when you are finished

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jboss-web xmlns="http://www.jboss.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
      http://www.jboss.com/xml/ns/javaee
      http://www.jboss.org/j2ee/schema/jboss-web_5_1.xsd">
    <security-domain>jaspitest</security-domain>
</jboss-web>
```

## Configuring your KeyCloak Client

After configuring your `WEB-INF/json-web.xml` file, you must add the `keycloak.json` 
client configuration file.  This is the file that our authentication mechanism will 
use to communicate with the KeyCloak server.  This file should be placed on your classpath. 
 If using Maven as your build manager, you can place it under `src/main/resources`. 
  This is necessary for the integration to work properly.

## Securing your App

Once the KeyCloak client configuration is in place, you can begin to secure your Java 
EE web application using the standard security annotations, such as `@ServletSecurity`. 
 You may place this annotation on your Servlet or JAX-RS endpoint and define the roles 
 allowed to access that resource.  When an unauthenticated user browses to a secured 
 endpoint, Soteria will recognize this and redirect the user to KeyCloak for authentication 
 by using the `KeyCloakAuthenticationMechanism`.  Once authentication is complete, 
 the user will be redirected to the original secured resource.

## Testing your Integration

### Logging-in with KeyCloak

Soteria recognizes when an unauthenticated user is attempting to access a protected 
resource.  When this occurs, it will kick off the authentication process, eventually 
calling on the `KeyCloakAuthenticationMechanism` class to redirect the user over to 
KeyCloak to authenticate.  If you are using the KeyCloak Docker container from this 
repository, you can login with the username `user1` and password `password123`.  Once 
successfully authenticated, you will be redirected back to your application.  This 
process leverages the *Authorization Code* grant type.  The `KeyCloakAuthenticationMechanism` 
class will attempt to validate the authorization code provided by KeyCloak and exchange 
it for an Access Token.  If successful, you will be redirected to the originally requested 
protected resource.  You will notice that all role membership is derived from the access 
token and is included in your Java EE `Subject` and `Principal` objects.  It is easy to confirm 
this by calling [`HttpServletRequest#isUserInRole()`](https://docs.oracle.com/javaee/7/api/javax/servlet/http/HttpServletRequest.html#isUserInRole-java.lang.String-).

### Authenticating with a Bearer Token

KeyCloak also supports the *Password Grant* grant type, allowing you to perform 
an HTTP POST with the user credentials and receive an Access Token in the response. 
 You can use this token to make subsequent calls to your application through RESTful 
 services, using either the command line or programmatic methods.

To obtain the Access Token, you must perform a POST to your KeyCloak server with the 
necessary credentials.  As an example, you can do this from the command line using 
`curl`.  If you are testing using the KeyCloak Docker container created from this repository, 
you curl command would look like this:

```
curl -vki -d "client_id=soteria-test&client_secret=password&username=user1&password=password123&grant_type=password" http://localhost:8880/auth/realms/soteria/protocol/openid-connect/token
```

If successful, the response returned from KeyCloak will be a JSON payload with an Access 
Token.  It will look something like this:

```javascript
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJGSjg2R2NGM2pUYk5MT2NvNE52WmtVQ0lVbWZZQ3FvcXRPUWVNZmJoTmxFIn0.eyJqdGkiOiIwOWZmNDk3ZC04ZGNhLTQyNGEtOGJlYi0yZmY5YmMzY2IwM2IiLCJleHAiOjE1MzM2NDk4MjQsIm5iZiI6MCwiaWF0IjoxNTMzNjQ5NTg0LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0Ojg4ODAvYXV0aC9yZWFsbXMvc290ZXJpYSIsImF1ZCI6InNvdGVyaWEtdGVzdCIsInN1YiI6IjViNDc1ZWQ0LTAwZWItNGE1OC04MjRkLWYyZDEwMTJkZDVmZCIsInR5cCI6IkJlYXJlciIsImF6cCI6InNvdGVyaWEtdGVzdCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6ImY3NWI5NGY3LWEwMTctNDViOC1iMDQ5LTAyNWZjMDA5ZDY1MSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOltdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsicm9sZTEiLCJ1c2VycyJdfSwicmVzb3VyY2VfYWNjZXNzIjp7fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoidXNlcjEgdXNlcjEiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMSIsImdpdmVuX25hbWUiOiJ1c2VyMSIsImZhbWlseV9uYW1lIjoidXNlcjEiLCJlbWFpbCI6InVzZXIxQGV4YW1wbGUub3JnIn0.gGBUEzZUGUiDiQTs65t5C2dES9zFsD5JCcbEg9aFxVyfy16fwzfCjEdN0AibeejRV3YOM6hhnRnNmov65o-Oonk4yUCC1IniE8foaDK6I4qHBLTUvh-VECyfdpcmx1VksDM9B6yS_SSv94osN7Lai56YaM8M66HAgTxFVATgue0",
  "expires_in": 240,
  "refresh_expires_in": 600,
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJGSjg2R2NGM2pUYk5MT2NvNE52WmtVQ0lVbWZZQ3FvcXRPUWVNZmJoTmxFIn0.eyJqdGkiOiI2Nzg1OTBjNS05OGVmLTQ3YjItYTliNS0yYzBhYTQ2MzdmNmUiLCJleHAiOjE1MzM2NTAxODQsIm5iZiI6MCwiaWF0IjoxNTMzNjQ5NTg0LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0Ojg4ODAvYXV0aC9yZWFsbXMvc290ZXJpYSIsImF1ZCI6InNvdGVyaWEtdGVzdCIsInN1YiI6IjViNDc1ZWQ0LTAwZWItNGE1OC04MjRkLWYyZDEwMTJkZDVmZCIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJzb3RlcmlhLXRlc3QiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJmNzViOTRmNy1hMDE3LTQ1YjgtYjA0OS0wMjVmYzAwOWQ2NTEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsicm9sZTEiLCJ1c2VycyJdfSwicmVzb3VyY2VfYWNjZXNzIjp7fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIn0.ABTJa4rnEfpP3WEFCa7nMwcEjuvocXHlTr5TEG2fSegnBciRxPEW-cmawkqb34ghSEGeqLuoy3biOlQehe26EsfdxJOClJ2O9C4qt9pCBIp4TkdEhYQH0FNPLhOTLK3JKvANYWxkIPy-hoRE65S_0J6fv-vW7n1XKiiBdAYDPFY",
  "token_type": "bearer",
  "not-before-policy": 0,
  "session_state": "f75b94f7-a017-45b8-b049-025fc009d651",
  "scope": "email profile"
}
```

You can now take the `access_token` value and use that in REST calls to your protected 
Java EE endpoints.  To do so, you must add an `Authorization` header in your calls. 
 The value of this header should be `Bearer <Access Token Value>`.  Making a REST call 
 using curl would look like this:

```
curl -vki -H "Authorization: Bearer yJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwi..." http://localhost:8080/myapp/my_secured_endpoint
```

At this point, our KeyCloak `HttpAuthenticationMechanism` subclass will attempt to 
validate the Access Token against the KeyCloak endpoint.  If successful, your call 
will be able to continue and will be evaluated against any authorization constraints 
you have defined for the endpoint.
