import { ApplicationConfig, provideZoneChangeDetection, APP_INITIALIZER, importProvidersFrom, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { KeycloakService, KeycloakAngularModule } from 'keycloak-angular';
import { provideApollo } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { InMemoryCache, split } from '@apollo/client/core';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { getMainDefinition } from '@apollo/client/utilities';

import { routes } from './app.routes';
import { environment } from '../environments/environment';
import { provideHighcharts } from 'highcharts-angular';

function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
    keycloak.init({
      config: environment.keycloak,
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri:
          window.location.origin + '/assets/silent-check-sso.html'
      }
    }).catch(err => {
      console.warn('Keycloak initialization failed', err);
      return true; // Resolve anyway to let the app load
    });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withFetch()),
    provideHighcharts({ instance: () => import('highcharts') }),
    importProvidersFrom(KeycloakAngularModule),
    /* {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService]
    }, */
    HttpLink,
    provideApollo(() => {
      const httpLink = inject(HttpLink);
      const http = httpLink.create({
        uri: environment.graphqlUrl
      });

      const ws = new GraphQLWsLink(
        createClient({
          url: environment.graphqlWsUrl
        })
      );

      const link = split(
        ({ query }) => {
          const definition = getMainDefinition(query);
          return (
            definition.kind === 'OperationDefinition' &&
            definition.operation === 'subscription'
          );
        },
        ws,
        http
      );

      return {
        link,
        cache: new InMemoryCache()
      };
    })
  ]
};
