import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from '../app-routing.module';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule, MatInputModule, MatPaginatorModule } from '@angular/material';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NavigationBarComponent } from '../components/navigation-bar/navigation-bar.component';
import { LoginComponent } from '../components/login/login.component';
import { UserTransfersComponent } from '../components/user-transfers/user-transfers.component';
import { APP_BASE_HREF } from '@angular/common';
import { Observable, of } from 'rxjs';
import { UserToken } from '../models/user-token';

describe('AuthService', () => {

  const USERNAME = 'user';
  const TOKEN = 'JWT';

  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientModule,
      AppRoutingModule,
      MatToolbarModule,
      MatCardModule,
      FormsModule,
      ReactiveFormsModule,
      MatFormFieldModule,
      HttpClientModule,
      AppRoutingModule,
      MatSelectModule,
      MatDatepickerModule,
      MatTableModule,
      MatPaginatorModule,
      MatFormFieldModule,
      MatInputModule,
      BrowserAnimationsModule
    ],
    declarations: [
      NavigationBarComponent,
      LoginComponent,
      UserTransfersComponent
    ],
    providers: [
      {provide: APP_BASE_HREF, useValue: '/'}
    ]
  }));

  it('when removeSessionData then remove keys', () => {
    // Arrange
    const userToken = new UserToken();
    userToken.userName = USERNAME;
    userToken.token = TOKEN;
    const httpSpy: any = {
      post: jasmine.createSpy('post').and.returnValue(of(userToken))
    };
    const service: AuthService = new AuthService(httpSpy, null);

    // Act
    const result: Observable<UserToken> = service.authenticateUser('user', 'pass');

    // Assert
    expect(httpSpy.post).toHaveBeenCalled();
    result.subscribe(userTokenResult => {
      expect(userTokenResult.userName).toBe(USERNAME);
      expect(userTokenResult.token).toBe(TOKEN);
    });
  });

  it('when handleAuthError then logout', () => {
    // Arrange
    const error = {
      status: 401
    };
    const router: any = {
      navigate: jasmine.createSpy('navigate').and.stub()
    };
    const service: AuthService = new AuthService(null, router);

    // Act
    const result: boolean = service.handleAuthError(error);

    // Assert
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    expect(result).toBe(true);
  });
});
