import { Component, OnInit } from '@angular/core';
import { AccountTransfer } from '../../models/account-transfer';
import { AccountsService } from '../../services/accounts.service';
import { UserAccount } from '../../models/userAccount';
import { TransferType } from '../../models/transfer-type.enum';
import { FormControl, Validators } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';

const PAGE_SIZE = 10;
const START_HOUR = 0;
const START_MIN = 0;
const END_HOUR = 11;
const END_MIN = 59;
const DEFAULT_DAYS_QUERY = 30;

@Component({
  selector: 'app-user-transfers',
  templateUrl: './user-transfers.component.html',
  styleUrls: ['./user-transfers.component.css']
})
export class UserTransfersComponent implements OnInit {

  transferTypes = Object.keys(TransferType);
  displayedColumns = ['date', 'type', 'account', 'description', 'fromAmount', 'finalAmount', 'observations'];
  allTransfers: AccountTransfer[] = [];
  transfersFiltered: AccountTransfer[] = [];
  accounts: UserAccount[];
  selectedTransferType: FormControl = new FormControl('', [
    Validators.required
  ]);
  selectedStartDate: FormControl;
  selectedEndDate: FormControl;
  selectedAccount: FormControl = new FormControl('', [
    Validators.required
  ]);
  selectedPage = 0;
  totalElements: number;
  pageSize: number = PAGE_SIZE;
  pageEvent: PageEvent;

  constructor(public accountsService: AccountsService) { }

  ngOnInit() {
    const currentDate = new Date();
    const previousDate = new Date();
    previousDate.setDate(previousDate.getDate() - DEFAULT_DAYS_QUERY);
    this.selectedStartDate = new FormControl(previousDate);
    this.selectedEndDate = new FormControl(currentDate);
    this.selectedTransferType.setValue(TransferType.All);
    this.loadUserAccounts();
  }

  loadUserAccounts() {
    this.accountsService.getAuthenticatedAccounts()
      .subscribe((accounts: UserAccount[]) => {
        this.accounts = accounts;
        if (accounts.length > 0) {
          this.selectedAccount.setValue(accounts[0].accountRef);
          this.loadAccountTransfers();
        }
      });
  }

  loadAccountTransfers(event?: PageEvent) {
    const startDate = this.getSelectedStartDate();
    const endDate = this.getSelectedEndDate();

    if (event) {
      this.selectedPage = event.pageIndex;
      this.pageSize = event.pageSize;
    } else {
      event = new PageEvent();
      event.pageIndex = this.selectedPage;
      event.pageSize = this.pageSize;
    }

    this.accountsService
      .getAccountTransfers(this.selectedAccount.value, startDate, endDate, this.selectedPage, this.pageSize)
      .subscribe(transfersPage => {
        if (transfersPage && transfersPage.transfers) {
          this.allTransfers = transfersPage.transfers.map(transfer => {
            transfer.transferDateObject = new Date(transfer.transferDate * 1000);
            transfer.finalAmountAbsolute = transfer.finalAmount < 0 ? transfer.finalAmount * -1 : transfer.finalAmount;
            transfer.originalAmountAbsolute = transfer.originalAmount < 0 ? transfer.originalAmount * -1 : transfer.originalAmount;
            transfer.isCredit = transfer.finalAmount < 0;
            return transfer;
          });

          this.transfersFiltered = this.filterTransferResults(this.allTransfers);
          this.totalElements = transfersPage.totalElements;
        }
      });

    return event;
  }

  filterTransferResults(accountTransfers: AccountTransfer[]) {
    const transferType = this.selectedTransferType.value;
    if (transferType === TransferType.Debit) {
      return accountTransfers.filter(transfer => transfer.finalAmount >= 0);
    } else if (transferType === TransferType.Credit) {
      return accountTransfers.filter(transfer => transfer.finalAmount < 0);
    } else {
      return accountTransfers;
    }
  }

  loadAllTransfers() {
    this.selectedTransferType.setValue(TransferType.All);
    this.loadAccountTransfers();
  }

  getSelectedStartDate(): number {
    const date = this.getDateTimeFromControl(this.selectedStartDate, START_HOUR, START_MIN);
    return this.getTimeSeconds(date);
  }

  getSelectedEndDate(): number {
    const date = this.getDateTimeFromControl(this.selectedEndDate, END_HOUR, END_MIN);
    return this.getTimeSeconds(date);
  }

  getTimeSeconds(date: number): number {
    return Math.round(date / 1000);
  }

  getDateTimeFromControl(control: FormControl, hour: number, minutes: number): number {
    const date = new Date(control.value);
    const purgedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), hour, minutes, 0);
    return purgedDate.getTime();
  }
}
