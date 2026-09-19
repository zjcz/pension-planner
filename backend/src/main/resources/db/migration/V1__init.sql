CREATE TABLE users (
    userId INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    passwordHash TEXT NOT NULL,
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL UNIQUE,
    targetIncome INTEGER,
    retirementDate DATE,
    auditEnabled BOOLEAN NOT NULL DEFAULT 1,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
);

CREATE TABLE pension (
    pensionId INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    name TEXT NOT NULL,
    maturityDate DATE NOT NULL,
    notes TEXT,
    status TEXT NOT NULL,
    statusDate DATE,
    color TEXT,
    providerName TEXT,
    policyNumber TEXT,
    workplaceName TEXT,
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
    userId INTEGER NOT NULL,
    name TEXT NOT NULL DEFAULT 'State Pension',
    yearlyAmount INTEGER NOT NULL,
    takesEffectYear INTEGER NOT NULL DEFAULT 2026,
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
    color TEXT,
    providerName TEXT,
    policyNumber TEXT,
    workplaceName TEXT,
    tags TEXT
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
    yearlyAmount INTEGER,
    notes TEXT,
    takesEffectYear INTEGER
);

CREATE TABLE other_income_audit (
    auditId INTEGER PRIMARY KEY AUTOINCREMENT,
    action TEXT NOT NULL,
    auditTimestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id INTEGER,
    userId INTEGER,
    name TEXT,
    annualAmount INTEGER,
    notes TEXT,
    tags TEXT
);

CREATE TABLE tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    name TEXT NOT NULL,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

CREATE TABLE pension_tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pensionId INTEGER NOT NULL,
    tagId INTEGER NOT NULL,
    FOREIGN KEY (pensionId) REFERENCES pension(pensionId),
    FOREIGN KEY (tagId) REFERENCES tag(id)
);

CREATE TABLE other_income_tag (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    otherIncomeId INTEGER NOT NULL,
    tagId INTEGER NOT NULL,
    FOREIGN KEY (otherIncomeId) REFERENCES other_income(id),
    FOREIGN KEY (tagId) REFERENCES tag(id)
);

CREATE INDEX idx_pension_userId ON pension(userId);
CREATE INDEX idx_pension_statement_pensionId ON pension_statement(pensionId);
CREATE INDEX idx_state_pension_userId ON state_pension(userId);
CREATE INDEX idx_other_income_userId ON other_income(userId);
CREATE INDEX idx_pension_audit_pensionId ON pension_audit(pensionId);
CREATE INDEX idx_pension_statement_audit_statementId ON pension_statement_audit(statementId);
CREATE INDEX idx_state_pension_audit_id ON state_pension_audit(id);
CREATE INDEX idx_other_income_audit_id ON other_income_audit(id);
CREATE INDEX idx_tag_userId ON tag(userId);
CREATE INDEX idx_pension_tag_pensionId ON pension_tag(pensionId);
CREATE INDEX idx_pension_tag_tagId ON pension_tag(tagId);
CREATE INDEX idx_other_income_tag_otherIncomeId ON other_income_tag(otherIncomeId);
CREATE INDEX idx_other_income_tag_tagId ON other_income_tag(tagId);