INSERT INTO company (id, name) VALUES
  (nextval('company_id_seq'), 'EBF'),
  (nextval('company_id_seq'), 'Itekako'),
  (nextval('company_id_seq'), 'empty');

INSERT INTO employee (id, username, password, name, surname, email, salary, address, company_id, is_superuser) VALUES
  -- Company EBF (2 regular users):
  (nextval('employee_id_seq'), 'john', '$2y$12$oh40NxvuuyH7qNj/k4yT5ebZuXOBRzaEgbaxCyRp4866ajTteSULi',  -- pass: john01
    'John', 'Doe', 'john@example.com', 1500, NULL, (SELECT id FROM company WHERE name = 'EBF'), FALSE),
  (nextval('employee_id_seq'), NULL, NULL,
    'Jane', 'Doe', 'jane@example.com', 777.77, NULL, (SELECT id FROM company WHERE name = 'EBF'), FALSE),
  -- Company Itekako (single superuser):
  (nextval('employee_id_seq'), 'kris', '$2y$12$7QPF0t0fyivu3G5/vK0Rue5SOpHa5F0AU4g1CXCxQp50VVeWIVvvG',  -- pass: kris01
    'Kristijan', 'Mitrovic', 'km@example.com', 1000, NULL, (SELECT id FROM company WHERE name = 'Itekako'), TRUE);
