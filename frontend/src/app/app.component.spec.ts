import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { KeycloakService } from 'keycloak-angular';
import { Apollo } from 'apollo-angular';
import { provideHighcharts } from 'highcharts-angular';
import { of } from 'rxjs';

describe('AppComponent', () => {
  let keycloakServiceMock: any;
  let apolloMock: any;

  beforeEach(async () => {
    keycloakServiceMock = jasmine.createSpyObj('KeycloakService', ['init', 'isLoggedIn', 'loadUserProfile', 'getToken']);
    apolloMock = jasmine.createSpyObj('Apollo', ['watchQuery', 'subscribe']);
    apolloMock.watchQuery.and.returnValue({ valueChanges: of({ data: { allAssets: [] } }) });
    apolloMock.subscribe.and.returnValue(of({ data: {} }));

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        { provide: KeycloakService, useValue: keycloakServiceMock },
        { provide: Apollo, useValue: apolloMock },
        provideHighcharts({ instance: () => import('highcharts') })
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have the 'TradeMesh Dashboard' title`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('TradeMesh Dashboard');
  });

  it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('TradeMesh Dashboard');
  });
});
