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

  fetchData() {
    const QUERY = gql`
      query GetDashboardData {
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
      pollInterval: 5000
    }).valueChanges.pipe(map(result => {
      const assets = result.data?.allAssets || [];
      // Use BTC history for the main chart
      const btc = assets.find((a: any) => a.symbol === 'BTC');
      if (btc && btc.ohlcHistory) {
        this.updateCandlestickChart(btc.ohlcHistory);
      }
      return assets;
    }));

    // Subscription for BTC updates to drive the chart
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
          const update = result.data.priceUpdates;
          // When a real price tick comes in, we also update the chart
          // (Simple logic: just update the last point or add new one)
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
      // Using candlestick format for the chart
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
