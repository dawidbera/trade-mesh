import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { KeycloakService } from 'keycloak-angular';
import { Apollo } from 'apollo-angular'; // Import Apollo

describe('AppComponent', () => {
  let keycloakServiceMock: any;
  let apolloMock: any; // Declare mock variable for Apollo

  beforeEach(async () => {
    keycloakServiceMock = jasmine.createSpyObj('KeycloakService', ['init', 'isLoggedIn', 'loadUserProfile', 'getToken']);
    apolloMock = jasmine.createSpyObj('Apollo', ['client']); // Create a spy object for Apollo

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        { provide: KeycloakService, useValue: keycloakServiceMock },
        { provide: Apollo, useValue: apolloMock } // Provide the mock Apollo service
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
