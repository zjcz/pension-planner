CREATE TABLE users (
    userId INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    passwordHash TEXT NOT NULL,
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL UNIQUE,
    targetIncome REAL,
    retirementDate DATETIME,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

CREATE TABLE pension (
    pensionId INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    name TEXT NOT NULL,
    maturityDate DATETIME NOT NULL,
    notes TEXT,
    status TEXT NOT NULL,
    statusDate DATETIME,
    color TEXT,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

CREATE TABLE pension_statement (
    statementId INTEGER PRIMARY KEY AUTOINCREMENT,
    pensionId INTEGER NOT NULL,
    userId INTEGER NOT NULL,
    statementDate DATETIME NOT NULL,
    planValue REAL NOT NULL,
    projectedAnnualAmount REAL NOT NULL,
    yearlyCharges REAL,
    transferValue REAL,
    amountPaidIn REAL,
    statementNotes TEXT,
    FOREIGN KEY (pensionId) REFERENCES pension(pensionId),
    FOREIGN KEY (userId) REFERENCES users(userId)
);

CREATE TABLE state_pension (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL UNIQUE,
    name TEXT NOT NULL DEFAULT 'State Pension',
    annualAmount REAL NOT NULL,
    notes TEXT,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

CREATE TABLE other_income (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    name TEXT NOT NULL,
    annualAmount REAL NOT NULL,
    notes TEXT,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

CREATE TABLE pension_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pensionId INTEGER,
    userId INTEGER,
    name TEXT,
    maturityDate DATETIME,
    notes TEXT,
    status TEXT,
    statusDate DATETIME,
    color TEXT
);

CREATE TABLE pension_statement_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statementId INTEGER,
    pensionId INTEGER,
    userId INTEGER,
    statementDate DATETIME,
    planValue REAL,
    projectedAnnualAmount REAL,
    yearlyCharges REAL,
    transferValue REAL,
    amountPaidIn REAL,
    statementNotes TEXT
);

CREATE TABLE state_pension_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id INTEGER,
    userId INTEGER,
    name TEXT,
    annualAmount REAL,
    notes TEXT
);

CREATE TABLE other_income_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id INTEGER,
    userId INTEGER,
    name TEXT,
    annualAmount REAL,
    notes TEXT
);

CREATE INDEX idx_pension_userId ON pension(userId);
CREATE INDEX idx_pension_statement_userId ON pension_statement(userId);
CREATE INDEX idx_pension_statement_pensionId ON pension_statement(pensionId);
CREATE INDEX idx_state_pension_userId ON state_pension(userId);
CREATE INDEX idx_other_income_userId ON other_income(userId);
CREATE INDEX idx_pension_audit_pensionId ON pension_audit(pensionId);
CREATE INDEX idx_pension_statement_audit_statementId ON pension_statement_audit(statementId);
CREATE INDEX idx_state_pension_audit_id ON state_pension_audit(id);
CREATE INDEX idx_other_income_audit_id ON other_income_audit(id);
