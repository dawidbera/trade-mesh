export const environment = {
  production: true,
  graphqlUrl: 'http://gateway-service-dawidbera-dev.apps.rm2.thpm.p1.openshiftapps.com/graphql',
  graphqlWsUrl: 'ws://gateway-service-dawidbera-dev.apps.rm2.thpm.p1.openshiftapps.com/graphql',
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'trademesh',
    clientId: 'trademesh-frontend'
  }
};
