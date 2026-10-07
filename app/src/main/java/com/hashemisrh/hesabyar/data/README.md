# Data architecture

The data layer will be added in the next milestone.

Planned entities:
- Bank
- Account
- SmsPattern
- RawSms
- Transaction
- Category
- BalanceAdjustment
- AppSettings

The raw SMS is retained unchanged. Account identity is primary; bank is derived from the registered account. Ambiguous SMS goes to review and is never guessed.
