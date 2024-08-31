CREATE TABLE fin_accounts (
                              account_id SERIAL,
                              user_id bigint NOT NULL,
                              account_ref varchar(50) NOT NULL
)
;

CREATE TABLE fin_transfers (
                               transfer_id SERIAL,
                               sender_account_id bigint NOT NULL,
                               recipient_account_id bigint NOT NULL,
                               ammount decimal(15,2) DEFAULT 0 NOT NULL,
                               currency varchar(10) NOT NULL,
                               transfer_date bigint NOT NULL,
                               description varchar(200)
)
;

CREATE TABLE fin_users (
                           user_id SERIAL,
                           username varchar(50),
                           password varchar(100),
                           name varchar(100),
                           surname varchar(100),
                           role varchar(50) NOT NULL
)
;

ALTER TABLE fin_users
    ADD CONSTRAINT UQ_fin_users_username UNIQUE (username)
;

ALTER TABLE fin_users ADD CONSTRAINT PK_fin_users
    PRIMARY KEY (user_id)
;

ALTER TABLE fin_accounts ADD CONSTRAINT PK_fin_accounts
    PRIMARY KEY (account_id)
;

ALTER TABLE fin_accounts
    ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES fin_users (user_id)
;

ALTER TABLE fin_transfers ADD CONSTRAINT PK_fin_transfers
    PRIMARY KEY (transfer_id)
;

ALTER TABLE fin_transfers
    ADD CONSTRAINT fk_account_sender_id FOREIGN KEY (sender_account_id) REFERENCES fin_accounts (account_id),
    ADD CONSTRAINT fk_account_recipient_id FOREIGN KEY (recipient_account_id) REFERENCES fin_accounts (account_id)
;
