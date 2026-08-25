ALTER TABLE pension ADD COLUMN providerName TEXT;
ALTER TABLE pension ADD COLUMN policyNumber TEXT;
ALTER TABLE pension ADD COLUMN workplaceName TEXT;

ALTER TABLE pension_audit ADD COLUMN providerName TEXT;
ALTER TABLE pension_audit ADD COLUMN policyNumber TEXT;
ALTER TABLE pension_audit ADD COLUMN workplaceName TEXT;
