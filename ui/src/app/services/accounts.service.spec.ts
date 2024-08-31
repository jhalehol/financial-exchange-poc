import { TestBed } from '@angular/core/testing';

import { AccountsService } from './accounts.service';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from '../app-routing.module';
import { NavigationBarComponent } from '../components/navigation-bar/navigation-bar.component';
import { LoginComponent } from '../components/login/login.component';
import { UserTransfersComponent } from '../components/user-transfers/user-transfers.component';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule, MatInputModule, MatPaginatorModule } from '@angular/material';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { APP_BASE_HREF } from '@angular/common';
import { Observable, of } from 'rxjs';
import { UserAccount } from '../models/userAccount';
import { AccountTransfersPage } from '../models/account-transfers-page';

describe('AccountsService', () => {
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

  it('when getAuthenticatedAccounts should call client', () => {
    // Arrange
    const httpClientSpy: any = {
      get: jasmine.createSpy('get').and.returnValue(of([new UserAccount()]))
    };
    const service: AccountsService = new AccountsService(httpClientSpy, null);

    // Act
    const result: Observable<UserAccount[]> = service.getAuthenticatedAccounts();

    // Assert
    expect(httpClientSpy.get).toHaveBeenCalled();
    result.subscribe(data => {
      expect(data.length).toBe(1);
    });

  });

  it('when getAccountTransfers should call client', () => {
    // Arrange
    const httpClientSpyTransfers: any  = {
      get: jasmine.createSpy('get').and.returnValue(of(new AccountTransfersPage()))
    };

    const service: AccountsService = new AccountsService(httpClientSpyTransfers, null);

    // Act
    service.getAuthenticatedAccounts();

    // Assert
    expect(httpClientSpyTransfers.get).toHaveBeenCalled();
  });
});
