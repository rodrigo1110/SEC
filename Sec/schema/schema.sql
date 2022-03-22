DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS movement;
CREATE TABLE account
(
    accountId INTEGER       NOT NULL PRIMARY KEY,
    pubKey    VARCHAR(5000) NOT NULL,
    balance   INTEGER       NOT NULL
);

CREATE TABLE movement
(
    movementId         INTEGER NOT NULL PRIMARY KEY,
    amount             INTEGER NOT NULL,
    sourceAccount      INTEGER NOT NULL,
    destinationAccount INTEGER NOT NULL
);