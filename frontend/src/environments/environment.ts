export const environment = {
  production: false,
  graphqlUrl: 'http://localhost:8084/graphql',
  graphqlWsUrl: 'ws://localhost:8084/graphql',
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'trademesh',
    clientId: 'trademesh-frontend'
  }
};
