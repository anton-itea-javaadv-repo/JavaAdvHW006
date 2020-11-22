create schema hw006 collate utf8mb4_general_ci;

use hw006;

create table aSimpleTable (
    id int auto_increment not null
        primary key,
    name varchar (30) not null,
    age int not null
);

select *
from aSimpleTable;
