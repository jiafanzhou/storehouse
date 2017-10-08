create table storehouse_user (
	id bigserial not null primary key,
	created_at timestamp not null,
	name varchar(40) not null,
	email varchar(70) not null unique,
	password varchar(100) not null,
	type varchar(20) not null
);

create table storehouse_user_role (
	user_id bigint not null,
	role varchar(30) not null,
	primary key(user_id, role),
	constraint fk_user_roles_user foreign key(user_id) references storehouse_user(id)
);

/**
 * Create the admin user, cannot be done by REST API
 */
insert into storehouse_user (created_at, name, email, password, type) values (current_timestamp, 'Admin', 'admin@domain.com', '73l8gRjwLftklgfdXT+MdiMEjJwGPVMsyVxe16iYpk8=', 'EMPLOYEE');
insert into storehouse_user_role (user_id, role) values((select id from storehouse_user where email = 'admin@domain.com'), 'EMPLOYEE');
insert into storehouse_user_role (user_id, role) values((select id from storehouse_user where email = 'admin@domain.com'), 'ADMIN');


create table storehouse_order (
    id bigserial not null primary key,
    created_at timestamp not null,
    customer_id bigint not null,
    total decimal(5,2) not null,
    current_status varchar(20) not null,
    constraint fk_order_customer foreign key(customer_id) references storehouse_user(id)
);

create table storehouse_order_item (
    order_id bigint not null,
    quantity int not null,
    price decimal(5,2) not null,
    primary key(order_id),
    constraint fk_order_item_order foreign key(order_id) references storehouse_order(id)
);

create table storehouse_order_orderhistory (
    order_id bigint not null,
    status varchar(20) not null,
    created_at timestamp not null,
    primary key(order_id, status),
    constraint fk_order_history_order foreign key(order_id) references storehouse_order(id)
);