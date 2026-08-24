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

CREATE INDEX idx_tag_userId ON tag(userId);
CREATE INDEX idx_pension_tag_pensionId ON pension_tag(pensionId);
CREATE INDEX idx_pension_tag_tagId ON pension_tag(tagId);
CREATE INDEX idx_other_income_tag_otherIncomeId ON other_income_tag(otherIncomeId);
CREATE INDEX idx_other_income_tag_tagId ON other_income_tag(tagId);
