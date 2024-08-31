export class AccountTransfer {

  description: String;
  relatedAccount: String;
  originalAmount: number;
  originalCurrency: String;
  finalAmount: number;
  finalCurrency: String;
  observations: String;
  transferDate: number;
  transferDateObject: object;
  finalAmountAbsolute: number;
  originalAmountAbsolute: number;
  isCredit: boolean;
  userRelatedAccount: String;
}
