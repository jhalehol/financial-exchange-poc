import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { UserAccount } from '../models/userAccount';
import { HttpClient} from '@angular/common/http';
import { AccountTransfersPage } from '../models/account-transfers-page';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from './auth.service';

const GET_ACCOUNTS_URL = 'api/account/list/authenticated';
const GET_TRANSFERS_URL = 'api/transfer';

@Injectable({
  providedIn: 'root'
})
export class AccountsService {

  constructor(private httpClient: HttpClient,
              private authService: AuthService) { }

  getAuthenticatedAccounts(): Observable<UserAccount[]> {
    return this.httpClient.get<UserAccount[]>(GET_ACCOUNTS_URL)
      .pipe(
        map((data: UserAccount[]) => {
          return data;
        }), catchError( error => {
          this.authService.handleAuthError(error);
          return of([]);
        })
      );
  }

  getAccountTransfers(account: String, startDate: number, endDate: number, pageNumber, pageSize): Observable<AccountTransfersPage> {
    return this.httpClient
      .get<AccountTransfersPage>(`${GET_TRANSFERS_URL}/${account}/${startDate}/${endDate}/${pageNumber}/${pageSize}`)
      .pipe(
        map((data: AccountTransfersPage) => {
          return data;
        }), catchError( error => {
          this.authService.handleAuthError(error);
          return of(new AccountTransfersPage());
        })
      );
  }
}
