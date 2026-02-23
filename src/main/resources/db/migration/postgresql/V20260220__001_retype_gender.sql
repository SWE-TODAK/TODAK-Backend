ALTER TABLE users
ALTER COLUMN gender TYPE varchar(10)
USING gender::text;