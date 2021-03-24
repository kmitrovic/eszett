-- Generated with javax.persistence.schema-generation with some small editing
create sequence company_id_seq start 1 increment 1;
create sequence employee_id_seq start 1 increment 1;

create table company (
  id int8 not null,
  name varchar(500) not null,
  primary key (id));
alter table company add constraint company_name_key unique (name);

create table employee (
  id int8 not null,
  username varchar(60),
  password varchar(60),
  name varchar(255) not null,
  surname varchar(255) not null,
  email varchar(320) not null,
  salary float8 not null,
  address varchar(500),
  company_id int8 not null,
  primary key (id));
alter table employee add constraint employee_username_key unique (username);
alter table employee add constraint employee_company_fkey foreign key (company_id) references company(id);
