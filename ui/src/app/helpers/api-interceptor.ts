import { Injectable } from '@angular/core';
import { HttpEvent } from '@angular/common/http';
import { HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

const AUTHORIZATION_KEY = 'Authorization';

@Injectable()
export class ApiInterceptor implements HttpInterceptor {

  apiUrl: String;
  constructor(private authService: AuthService) {
    this.apiUrl = environment.apiUrl;
  }

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    let authReq = req;
    const token = this.authService.getToken();
    if (token != null) {
      authReq = req.clone({
        url: `${this.apiUrl}${req.url}`,
        headers: req.headers.set(AUTHORIZATION_KEY, 'Bearer ' + token)
      });
    } else {
      authReq = req.clone({
        url: `${this.apiUrl}${req.url}`,
      });
    }

    return next.handle(authReq);
  }
}
