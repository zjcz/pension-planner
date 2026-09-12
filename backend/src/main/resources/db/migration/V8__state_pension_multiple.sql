-- Allow multiple state pension records per user by removing the UNIQUE constraint on userId.
-- SQLite cannot drop column constraints, so the table is rebuilt without it.
CREATE TABLE state_pension_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER NOT NULL,
    name TEXT NOT NULL DEFAULT 'State Pension',
    yearlyAmount INTEGER NOT NULL,
    takesEffectYear INTEGER NOT NULL DEFAULT 2026,
    notes TEXT,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
);

INSERT INTO state_pension_new (id, userId, name, yearlyAmount, takesEffectYear, notes)
SELECT id, userId, name, yearlyAmount, takesEffectYear, notes FROM state_pension;

DROP TABLE state_pension;
ALTER TABLE state_pension_new RENAME TO state_pension;

CREATE INDEX idx_state_pension_userId ON state_pension(userId);