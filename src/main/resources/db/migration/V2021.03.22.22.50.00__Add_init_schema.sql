CREATE SEQUENCE company_id_seq;

CREATE TABLE company (
  id BIGINT PRIMARY KEY DEFAULT nextval('company_id_seq'),
  name VARCHAR NOT NULL
);


CREATE SEQUENCE employee_id_seq;

CREATE TABLE employee (
  id BIGINT PRIMARY KEY DEFAULT nextval('employee_id_seq'),
  name VARCHAR NOT NULL,
  surname VARCHAR NOT NULL,
  email VARCHAR NOT NULL,
  salary NUMERIC NOT NULL,
  address VARCHAR DEFAULT '',
  company BIGINT NOT NULL,
  FOREIGN KEY (company) REFERENCES company (id)
);
