FROM jboss/keycloak:4.2.1.Final

COPY ./config/soteria-realm.json /opt/jboss/keycloak/

ENTRYPOINT [ "/opt/jboss/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0", "-Dkeycloak.import=/opt/jboss/keycloak/soteria-realm.json"]
