import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { Apollo, gql } from 'apollo-angular';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'TradeMesh Dashboard';
  isLoggedIn = false;
  userProfile: any;
  prices$: Observable<any[]> | undefined;

  constructor(private keycloak: KeycloakService, private apollo: Apollo) {}

  async ngOnInit() {
    // Bypass Keycloak for now to allow dashboard testing
    this.isLoggedIn = true;
    this.userProfile = { firstName: 'Test', lastName: 'User' };
    this.fetchPrices();
    
    /* 
    try {
      this.isLoggedIn = await this.keycloak.isLoggedIn();
      if (this.isLoggedIn) {
        this.userProfile = await this.keycloak.loadUserProfile();
        this.fetchPrices();
      }
    } catch (e) {
      console.warn('Keycloak skip', e);
    }
    */
  }

  login() {
    console.log('Login bypassed');
    this.isLoggedIn = true;
    this.fetchPrices();
  }

  logout() {
    console.log('Logout bypassed');
    this.isLoggedIn = false;
  }

  fetchPrices() {
    this.prices$ = this.apollo.watchQuery<any>({
      query: gql`
        query GetPrices {
          allAssets {
            id
            symbol
            currentPrice {
              value
              timestamp
            }
          }
        }
      `
    }).valueChanges.pipe(map(result => result.data?.allAssets || []));

    // Subscribe to real-time updates
    this.apollo.subscribe({
      query: gql`
        subscription OnPriceUpdate {
          priceUpdates(assetId: "BTC") {
            value
            timestamp
          }
        }
      `
    }).subscribe({
      next: (result: any) => {
        if (result.data?.priceUpdates) {
          console.log('Price update received:', result.data.priceUpdates);
        }
      },
      error: (err: any) => console.error('Subscription error:', err)
    });
  }
}
