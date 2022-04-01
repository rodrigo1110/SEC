DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS movement;
CREATE TABLE account
(
    pubKey    VARCHAR(5000) NOT NULL PRIMARY KEY,
    balance   REAL       NOT NULL
);

CREATE TABLE movement
(
    movementId         INTEGER NOT NULL PRIMARY KEY,
    amount             INTEGER NOT NULL,
    sourceAccount      INTEGER NOT NULL,
    destinationAccount INTEGER NOT NULL
);