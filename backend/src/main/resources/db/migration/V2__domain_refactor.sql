CREATE TABLE user_settings_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL UNIQUE,
    targetIncome INTEGER,
    retirementDate DATE,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
);

INSERT INTO user_settings_new (id, userId, targetIncome, retirementDate)
SELECT id, userId,
       CASE WHEN targetIncome IS NULL THEN NULL ELSE CAST(ROUND(targetIncome * 100) AS INTEGER) END,
       CASE
           WHEN retirementDate IS NULL THEN NULL
           WHEN typeof(retirementDate) = 'integer' THEN date(retirementDate / 1000, 'unixepoch')
           ELSE date(retirementDate)
       END
FROM user_settings;

DROP TABLE user_settings;
ALTER TABLE user_settings_new RENAME TO user_settings;

DROP TABLE pension_statement;
DROP TABLE other_income;
DROP TABLE state_pension;
DROP TABLE pension;
DROP TABLE pension_audit;
DROP TABLE pension_statement_audit;
DROP TABLE state_pension_audit;
DROP TABLE other_income_audit;

CREATE TABLE pension (
    pensionId INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    name TEXT NOT NULL,
    maturityDate DATE NOT NULL,
    notes TEXT,
    status TEXT NOT NULL,
    statusDate DATE,
    color TEXT,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
);

CREATE TABLE pension_statement (
    statementId INTEGER PRIMARY KEY AUTOINCREMENT,
    pensionId INTEGER NOT NULL,
    statementDate DATE NOT NULL,
    planValue INTEGER NOT NULL,
    projectedAnnualAmount INTEGER NOT NULL,
    yearlyCharges INTEGER,
    transferValue INTEGER,
    amountPaidIn INTEGER,
    statementNotes TEXT,
    FOREIGN KEY (pensionId) REFERENCES pension(pensionId) ON DELETE CASCADE
);

CREATE TABLE state_pension (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL UNIQUE,
    name TEXT NOT NULL DEFAULT 'State Pension',
    annualAmount INTEGER NOT NULL,
    notes TEXT,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
);

CREATE TABLE other_income (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    name TEXT NOT NULL,
    annualAmount INTEGER NOT NULL,
    notes TEXT,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
);

CREATE TABLE pension_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pensionId INTEGER,
    userId INTEGER,
    name TEXT,
    maturityDate DATE,
    notes TEXT,
    status TEXT,
    statusDate DATE,
    color TEXT
);

CREATE TABLE pension_statement_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statementId INTEGER,
    pensionId INTEGER,
    userId INTEGER,
    statementDate DATE,
    planValue INTEGER,
    projectedAnnualAmount INTEGER,
    yearlyCharges INTEGER,
    transferValue INTEGER,
    amountPaidIn INTEGER,
    statementNotes TEXT
);

CREATE TABLE state_pension_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id INTEGER,
    userId INTEGER,
    name TEXT,
    annualAmount INTEGER,
    notes TEXT
);

CREATE TABLE other_income_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id INTEGER,
    userId INTEGER,
    name TEXT,
    annualAmount INTEGER,
    notes TEXT
);

CREATE INDEX idx_pension_userId ON pension(userId);
CREATE INDEX idx_pension_statement_pensionId ON pension_statement(pensionId);
CREATE INDEX idx_state_pension_userId ON state_pension(userId);
CREATE INDEX idx_other_income_userId ON other_income(userId);
CREATE INDEX idx_pension_audit_pensionId ON pension_audit(pensionId);
CREATE INDEX idx_pension_statement_audit_statementId ON pension_statement_audit(statementId);
CREATE INDEX idx_state_pension_audit_id ON state_pension_audit(id);
CREATE INDEX idx_other_income_audit_id ON other_income_audit(id);