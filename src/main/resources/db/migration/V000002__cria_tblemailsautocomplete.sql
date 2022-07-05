CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE tblemailsautocomplete(
	cod SERIAL PRIMARY KEY,
	email citext
);


ALTER TABLE tblemailsautocomplete ADD CONSTRAINT tblemailsautocomplete_unico UNIQUE (email);
