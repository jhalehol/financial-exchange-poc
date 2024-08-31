import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserTransfersComponent } from './user-transfers.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule, MatInputModule, MatNativeDateModule, MatPaginatorModule } from '@angular/material';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from '../../app-routing.module';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LoginComponent } from '../login/login.component';
import { NavigationBarComponent } from '../navigation-bar/navigation-bar.component';
import { APP_BASE_HREF } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { UserAccount } from '../../models/userAccount';
import { AccountTransfer } from '../../models/account-transfer';
import { of } from 'rxjs';
import { TransferType } from '../../models/transfer-type.enum';
import { AccountTransfersPage } from '../../models/account-transfers-page';


describe('UserTransfersComponent', () => {
  let component: UserTransfersComponent;
  let fixture: ComponentFixture<UserTransfersComponent>;

  const accountsMock = [];
  const account1 = new UserAccount();
  account1.accountId = 1;
  account1.accountRef = '0000001';
  const account2 = new UserAccount();
  account2.accountId = 2;
  account2.accountRef = '0000002';
  accountsMock.push(account1);
  accountsMock.push(account2);

  const accountTransfers = [];
  const transfer1 = new AccountTransfer();
  transfer1.relatedAccount = '0000001';
  transfer1.originalAmount = 10000;
  transfer1.finalAmount = 10000;
  transfer1.originalCurrency = 'USD';
  transfer1.finalCurrency = 'USD';
  transfer1.description = 'Payment for software';
  transfer1.transferDate = 1626394767;
  accountTransfers.push(transfer1);
  const transfer2 = new AccountTransfer();
  transfer2.relatedAccount = '0000002';
  transfer2.originalAmount = -100000000;
  transfer2.finalAmount = -2500;
  transfer2.originalCurrency = 'COP';
  transfer2.finalCurrency = 'USD';
  transfer2.transferDate = 1626394767;
  accountTransfers.push(transfer2);
  const transferPage: AccountTransfersPage = new AccountTransfersPage();
  transferPage.transfers = accountTransfers;
  transferPage.totalElements = accountTransfers.length;

  const accountService: any = {
    getAuthenticatedAccounts: jasmine.createSpy('getAuthenticatedAccounts').and.returnValue(of(accountsMock)),
    getAccountTransfers: jasmine.createSpy('getAccountTransfers').and.returnValue(of(transferPage))
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
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
        BrowserAnimationsModule,
        MatToolbarModule,
        MatNativeDateModule
      ],
      declarations: [
        UserTransfersComponent,
        LoginComponent,
        NavigationBarComponent],
      providers: [
        {provide: APP_BASE_HREF, useValue: '/'}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserTransfersComponent);
    component = fixture.componentInstance;
    component.accountsService = accountService;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('when loadUserAccounts then load accounts and transfers', () => {
    // Act
    component.loadUserAccounts();

    // Assert
    expect(accountService.getAuthenticatedAccounts).toHaveBeenCalled();
    expect(accountService.getAccountTransfers).toHaveBeenCalled();
    expect(component.allTransfers.length).toBe(accountTransfers.length);
  });

  it('when filterTransferResults then should filter by selected type', () => {
    // Arrange
    component.selectedTransferType = new FormControl();
    component.selectedTransferType.setValue(TransferType.Credit);

    // Act
    const result = component.filterTransferResults(accountTransfers);

    // Assert
    expect(result.length).toBe(1);
  });
});
