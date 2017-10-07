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