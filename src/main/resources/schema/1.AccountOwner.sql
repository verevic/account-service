create table AccountOwner(
  id        integer generated always as identity primary key,
  name      varchar(50) not null,
  address   varchar(200),
  email     varchar(50),
);
