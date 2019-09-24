create table AccountOperation(
  id          integer generated always as identity primary key,
  account_id  integer foreign key references Account(id),
  created     datetime default current_timestamp,
  details     varchar(200),
  balance     float not null,
  ccy         varchar(3) not null
);
