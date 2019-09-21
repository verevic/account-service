create table Account(
  id        integer generated always as identity primary key,
  owner_id  integer foreign key references AccountOwner(id),
  amount    float not null,
  ccy       varchar(3) not null /*todo: think of a default value*/
);
