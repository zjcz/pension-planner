ALTER TABLE state_pension RENAME COLUMN annualAmount TO yearlyAmount;
ALTER TABLE state_pension ADD COLUMN startAge INTEGER NOT NULL DEFAULT 66;
ALTER TABLE state_pension ADD COLUMN taxRate INTEGER NOT NULL DEFAULT 20;
ALTER TABLE state_pension ADD COLUMN takesEffectYear INTEGER NOT NULL DEFAULT 2026;

ALTER TABLE state_pension_audit RENAME COLUMN annualAmount TO yearlyAmount;
ALTER TABLE state_pension_audit ADD COLUMN startAge INTEGER;
ALTER TABLE state_pension_audit ADD COLUMN taxRate INTEGER;
ALTER TABLE state_pension_audit ADD COLUMN takesEffectYear INTEGER;
