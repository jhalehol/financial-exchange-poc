import { Injectable } from '@angular/core';
import { UserToken } from '../models/user-token';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';

const LOGIN_PAGE = '/login';
const USERNAME_KEY = 'user-name';
const TOKEN_KEY = 'user-token';
const GET_TOKEN_URL = 'oauth2/token';
const UNAUTHORIZED_ERRORS = [401, 403];
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private userAuthenticated = new BehaviorSubject<boolean>(false);

  constructor(private httpClient: HttpClient,
              private router: Router) { }

  public removeSessionData() {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.removeItem(USERNAME_KEY);
    this.userAuthenticated.next(false);
  }

  public saveToken(tokenInfo: UserToken) {
    this.removeSessionData();
    window.sessionStorage.setItem(TOKEN_KEY, tokenInfo.token);
    window.sessionStorage.setItem(USERNAME_KEY, tokenInfo.userName);
    this.userAuthenticated.next(true);
  }

  public getToken(): string {
    return window.sessionStorage.getItem(TOKEN_KEY);
  }

  public getUserName(): String {
    return window.sessionStorage.getItem(USERNAME_KEY);
  }

  public authenticateUser(username: string, password: string): Observable<UserToken> {
    return this.httpClient.post<UserToken>(GET_TOKEN_URL, {
      username,
      password},
      httpOptions);
  }

  public isAuthenticatedNonOnservable(): boolean {
    return this.getToken() != null;
  }

  public isAuthenticated(): Observable<boolean> {
    const isLogged: boolean = this.isAuthenticatedNonOnservable();
    this.userAuthenticated.next(isLogged);
    return this.userAuthenticated.asObservable();
  }

  public handleAuthError(error): boolean {
    if (error && error.status) {
      const isAuthError = UNAUTHORIZED_ERRORS.some(code => code === error.status);
      if (isAuthError) {
        this.removeSessionData();
        this.router.navigate([LOGIN_PAGE]);
        return true;
      }
    }

    return false;
  }
}
