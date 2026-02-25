import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { Apollo, gql } from 'apollo-angular';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as Highcharts from 'highcharts';
import { HighchartsChartComponent } from 'highcharts-angular';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HighchartsChartComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  chartOptions: any = {
    chart: { type: 'line', backgroundColor: '#1e1e1e' },
    title: { text: 'Real-time Market Data', style: { color: '#ffffff' } },
    xAxis: { type: 'datetime', labels: { style: { color: '#ffffff' } } },
    yAxis: { title: { text: 'Price', style: { color: '#ffffff' } }, labels: { style: { color: '#ffffff' } } },
    series: [{ name: 'Market Price', data: [], color: '#00ff00', type: 'line' }]
  };
  chartUpdateFlag = false;

  title = 'TradeMesh Dashboard';
  isLoggedIn = false;
  userProfile: any;
  prices$: Observable<any[]> | undefined;
  indicators$: Observable<any[]> | undefined;
  history$: Observable<any[]> | undefined;
  
  selectedAssetId = 'BTC';
  private subscription: any;

  constructor(private keycloak: KeycloakService, private apollo: Apollo) {}

  async ngOnInit() {
    this.isLoggedIn = true;
    this.userProfile = { firstName: 'Test', lastName: 'User' };
    this.fetchData();
  }

  login() {
    this.isLoggedIn = true;
    this.fetchData();
  }

  logout() {
    this.isLoggedIn = false;
  }

  selectAsset(id: string) {
    this.selectedAssetId = id;
    this.chartOptions.title.text = `Real-time Market Data: ${id}`;
    this.chartOptions.series[0].data = []; // Clear current chart data
    this.chartUpdateFlag = true;
    
    // Refresh data and subscription for the new asset
    this.fetchData();
  }

  fetchData() {
    // Clean up previous subscription if exists
    if (this.subscription) {
      this.subscription.unsubscribe();
    }

    const QUERY = gql`
      query GetDashboardData($id: String!) {
        allAssets {
          id
          symbol
          currentPrice {
            value
            timestamp
          }
          analytics {
            indicatorType
            value
            timestamp
            metadata
          }
          history(limit: 5) {
            price
            volume
            timestamp
            type
          }
          ohlcHistory(interval: "1m") {
            timestamp
            open
            high
            low
            close
            volume
          }
        }
      }
    `;

    this.prices$ = this.apollo.watchQuery<any>({
      query: QUERY,
      variables: { id: this.selectedAssetId },
      pollInterval: 5000
    }).valueChanges.pipe(map(result => {
      const assets = result.data?.allAssets || [];
      const selected = assets.find((a: any) => a.symbol === this.selectedAssetId);
      if (selected && selected.ohlcHistory) {
        this.updateCandlestickChart(selected.ohlcHistory);
      }
      return assets;
    }));

    // Subscription for selected asset updates
    this.subscription = this.apollo.subscribe({
      query: gql`
        subscription OnPriceUpdate($assetId: String!) {
          priceUpdates(assetId: $assetId) {
            value
            timestamp
          }
        }
      `,
      variables: { assetId: this.selectedAssetId }
    }).subscribe({
      next: (result: any) => {
        if (result.data?.priceUpdates) {
          const update = result.data.priceUpdates;
          this.updateChart(update.timestamp, update.value);
        }
      }
    });
  }

  updateCandlestickChart(history: any[]) {
    if (this.chartOptions.series) {
      const data = history.map(h => ({
        x: new Date(h.timestamp).getTime(),
        open: h.open,
        high: h.high,
        low: h.low,
        close: h.close
      }));
      this.chartOptions.series[0].data = data.map(d => [d.x, d.open, d.high, d.low, d.close]);
      this.chartOptions.chart.type = 'candlestick';
      this.chartUpdateFlag = true;
    }
  }

  updateChart(time: number, value: number) {
    if (this.chartOptions.series && this.chartOptions.chart.type === 'line') {
      const data = this.chartOptions.series[0].data as any[];
      data.push([time, value]);
      if (data.length > 20) data.shift();
      this.chartUpdateFlag = true;
    }
  }
}
