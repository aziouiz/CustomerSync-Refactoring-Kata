create table customer(
    id uuid not null constraint customer_pkey primary key,
    address_city         varchar(255),
    address_postalcode   varchar(255),
    address_street       varchar(255),
    bonus_points_balance integer,
    company_number       varchar(255),
    customer_type        varchar(255),
    external_id          varchar(255),
    master_external_id   varchar(255),
    name                 varchar(255),
    preferred_store      varchar(255)
);

create table shopping_list(
    id uuid not null primary key,
    customer_id uuid not null references customer
);

create table shopping_list_product(
    shopping_list_id uuid not null references shopping_list,
    product varchar(255) not null
);
