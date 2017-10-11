# Project Title

Storehouse application facilitates a company to move quickly for their products from their warehouse to the frontdesk.

- It tracks the customer's ordering status from the beginning to the end.
- It tracks customer's ordering position in the queue and provides an estimate waiting time.
- It allows employee and manager to view the orders queue.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

The development and testing is based a Linux system, it is advised to install the same on your environment.

```
$ lsb_release --all
Distributor ID: Ubuntu
Description:    Ubuntu 16.04.2 LTS
Release:        16.04
Codename:       xenial
```

### Installing

(1) Install PostgreSQL DB server

Make sure there is a database table named "storehouse", and uses the default PostgreSQL user "postgres" and password "postgres" to access the database and its tables.

```
$ psql --version
psql (PostgreSQL) 9.5.8

$ psql -d storehouse -U postgres
psql (9.5.8)
Type "help" for help.

storehouse=# \d
                      List of relations
 Schema |             Name              |   Type   |  Owner   
--------+-------------------------------+----------+----------
 public | storehouse_order              | table    | postgres
 public | storehouse_order_id_seq       | sequence | postgres
 public | storehouse_order_item         | table    | postgres
 public | storehouse_order_orderhistory | table    | postgres
 public | storehouse_user               | table    | postgres
 public | storehouse_user_id_seq        | sequence | postgres
 public | storehouse_user_role          | table    | postgres
(7 rows)
```

The database script to create the above tables can be found here:

https://github.com/jiafanzhou/storehouse/blob/master/storehouse-app/storehouse-model/src/main/resources/db.sql

```
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
```

(2) Install the Java JDK8

```
$ javac -version
javac 1.8.0_73

$ java -version
java version "1.8.0_73"
Java(TM) SE Runtime Environment (build 1.8.0_73-b02)
Java HotSpot(TM) 64-Bit Server VM (build 25.73-b02, mixed mode)

```

(3) Install Maven

```
$ mvn --version
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T16:41:47+00:00)
Maven home: /opt/apache-maven-3.3.9
Java version: 1.8.0_73, vendor: Oracle Corporation
Java home: /opt/java/jdk1.8.0_73/jre
Default locale: en_IE, platform encoding: UTF-8
OS name: "linux", version: "4.4.0-96-generic", arch: "amd64", family: "unix"
```

(4) Install the Wildfly AS

```
$ ./standalone.sh --version
=========================================================================

  JBoss Bootstrap Environment

  JBOSS_HOME: /opt/wildfly-8.1.0.Final

  JAVA: /opt/java/jdk1.8.0_73/bin/java

  JAVA_OPTS:  -server -Xms64m -Xmx512m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true

=========================================================================

Java HotSpot(TM) 64-Bit Server VM warning: ignoring option MaxPermSize=256m; support was removed in 8.0
10:33:06,538 INFO  [org.jboss.modules] (main) JBoss Modules version 1.3.3.Final
WildFly 8.1.0.Final "Kenny"
```

And make sure that the correct standalone-full.xml is copied to the ${JBOSS_HOME}/standalone/configuration/standalone-full.xml

You can find a copy of this standalone-full.xml in the below link:
https://github.com/jiafanzhou/storehouse/blob/master/storehouse-app/storehouse-resource-war/src/main/resources/standalone-full.xml

Basically, it is a slightly modified version of the standalone-full.xml which comes from the bundled wildfly AS, and added the following modification:

- datasource (java:jboss/datasources/storehouse
- security-domain (storehouse)
- JMS queue (Orders)

(5) Deploy the ear application to Wildfly AS and Run

It is possible to either build the ear from source code (please refer the below "Build ear project from source") or directly copy the ear file provided (see link below) to ${JBOSS_HOME}/standalone/deployments/

Provided ear file:
https://github.com/jiafanzhou/storehouse/blob/master/storehouse-app/storehouse-ear/deployment_artifact/storehouse-ear-0.0.1-SNAPSHOT.ear

And start the Wildfly AS:

```
/opt/wildfly-8.1.0.Final/bin$ ./standalone.sh -c=standalone-full.xml
=========================================================================

  JBoss Bootstrap Environment

  JBOSS_HOME: /opt/wildfly-8.1.0.Final

  JAVA: /opt/java/jdk1.8.0_73/bin/java

  JAVA_OPTS:  -server -Xms64m -Xmx512m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true

=========================================================================

Java HotSpot(TM) 64-Bit Server VM warning: ignoring option MaxPermSize=256m; support was removed in 8.0
....
....
...
10:52:17,179 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: WildFly 8.1.0.Final "Kenny" started in 6467ms - Started 795 of 876 services (145 services are lazy, passive or on-demand)

```

If everything is installed and configured correctly, you should see the ear file is deployed correctly in the Wildfly console output

```
....
10:52:14,419 INFO  [org.jboss.weld.deployer] (MSC service thread 1-7) JBAS016005: Starting Services for CDI deployment: storehouse-ear-0.0.1-SNAPSHOT.ear
....
```

Note: if anything goes wrong during the startup of your Wildfly AS or storehouse ear deployment, please run some troubleshooting and contact the author directly. 

## Build ear project from source
It is possible to use maven to build the storehouse ear directly.

Just run the following command:

```
ejiafzh@ejiafzh-nuc:~/data/storehouse/storehouse-app$ ls
pom.xml  README.md  storehouse-ear  storehouse-integration  storehouse-model  storehouse-resource  storehouse-resource-war
ejiafzh@ejiafzh-nuc:~/data/storehouse/storehouse-app$ pwd
/home/ejiafzh/data/storehouse/storehouse-app
ejiafzh@ejiafzh-nuc:~/data/storehouse/storehouse-app$ mvn clean install -DskipTests
.....
```

If everything compiles fine and you should be able to see the ear being generated in the storehouse-ear/target directory

```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] storehouse-app ..................................... SUCCESS [  0.270 s]
[INFO] storehouse-model ................................... SUCCESS [  2.679 s]
[INFO] storehouse-resource ................................ SUCCESS [  0.714 s]
[INFO] storehouse-resource-war ............................ SUCCESS [  0.468 s]
[INFO] storehouse-integration ............................. SUCCESS [  0.138 s]
[INFO] storehouse-ear ..................................... SUCCESS [  0.312 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.731 s
[INFO] Finished at: 2017-10-10T10:56:53+01:00
[INFO] Final Memory: 31M/581M
[INFO] ------------------------------------------------------------------------
ejiafzh@ejiafzh-nuc:~/data/storehouse/storehouse-app$ ls storehouse-ear/target/storehouse-ear-0.0.1-SNAPSHOT.ear 
storehouse-ear/target/storehouse-ear-0.0.1-SNAPSHOT.ear
```

Note: if you run this build the first time, it may takes significant time to download the required maven dependencies.

## Running the tests

There are two sets of tests involved in this project, 1) JUnit Tests 2) Arquillian Tests.


### Run JUnit Tests

```
$ mvn clean test
```

### Run Arquillian Tests

```bash
$ mvn clean install -PintegrationTests-wildfly
```

## REST Endpoints for Order

Assuming that the root REST endpoint for Order is: http://localhost:8080/storehouse/api/orders

### Add a new Order
- URL: http://localhost:8080/storehouse/api/orders
- Type: POST
- BODY (RAW Json) example:
```
{
    "clientId": 3,
    "items": [
        {
            "quantity": 2
        }
    ]
}
```
- Roles: Customer
- Description: Customer must login first by their email and clientId must match the customer ID.

### Cancel an Order
- URL: http://localhost:8080/storehouse/api/orders/{customer_id}/cancel
- Type: GET
- BODY: None
- Roles: Customer, Employee, Admin
- Description: clientId must be passed in to cancel the order

### Get an Order Position and Estimated wait time
- URL: http://localhost:8080/storehouse/api/orders/stats/{id}
- Type: GET
- BODY: None
- Roles: Customer, Employee, Admin
- Description: clientId must be passed in to cancel the order

### Get all Orders Position and Estimated wait time
- URL: http://localhost:8080/storehouse/api/orders/stats/all
- Type: GET
- BODY: None
- Roles: Employee, Admin

### Consume Orders from the queue
- URL: http://localhost:8080/storehouse/api/orders/consume
- Type: GET
- BODY: None
- Roles: Employee, Admin


### Find an Order by ID
- URL: http://localhost:8080/storehouse/api/orders/{id}
- TYPE: GET
- BODY: None
- Roles: Customer, Employee, ADMIN

### Find all Orders
- URL: http://localhost:8080/storehouse/api/orders/all
- TYPE: GET
- BODY: None
- Roles: Employee

### Find all Orders by filter
- URL: http://localhost:8080/storehouse/api/orders?page=0&per_page=2&sort=-startDate
- TYPE: GET
- BODY: None
- Roles: Employee

### Change an Order Status
- URL: http://localhost:8080/storehouse/api/orders/{order_id}/status
- Type: POST
- BODY (RAW Json) example:
```
{
	"status": "DELIVERED"
}
```
- Roles: Logged in Customer, Employee, Admin
- Description: orderId must be passed in to change an order status


## REST Endpoints for Customer

Assuming that the root REST endpoint for Customer is: http://localhost:8080/storehouse/api/users

### Register a new Customer
- URL: http://localhost:8080/storehouse/api/users/
- Type: POST
- BODY (RAW Json) example:
```
{
    "name": "John Doe",
    "email": "john.doe@domain.com",
    "password": "12345678",
    "type": "CUSTOMER"
}
```
- Roles: None (anyone can register)

### Update an existing Customer
- URL: http://localhost:8080/storehouse/api/users/{id}
- Type: PUT
- BODY (RAW Json) example:
```
{
    "name": "John Doe",
    "email": "jiafan.zhou@domain.com",
    "password": "12345678",
    "type": "CUSTOMER"
}
```
- Roles: ADMIN, Logged in Customer

### Update Customer password
- URL: http://localhost:8080/storehouse/api/users/{id}/password
- TYPE: PUT
- BODY (RAW Json) example:
```
{
    "password": "new_password"
}
```
- Roles: ADMIN, Logged in Customer

### Find Customer by ID
- URL: http://localhost:8080/storehouse/api/users/{id}
- TYPE: GET
- BODY: None
- Roles: ADMIN

### Login
- URL: http://localhost:8080/storehouse/api/users/authenticate
- TYPE: POST
- BODY (RAW Json) example:
```
{
    "email": "john.doe@domain.com",
    "password": "12345678"
}
```
- Roles: ADMIN, EMPLOYEE, CUSTOMER

### Find all customers
- URL: http://localhost:8080/storehouse/api/users/all
- TYPE: GET
- BODY: None
- Roles: ADMIN

### Find all customers by filter
- URL: http://localhost:8080/storehouse/api/users?page=0&per_page=2&sort=-name
- TYPE: GET
- BODY: None
- Roles: ADMIN


### Find Customer by Email
- URL: http://localhost:8080/storehouse/api/users/email/{email}
- TYPE: GET
- BODY: None
- Roles: ADMIN


## Built With/Test With/Develop With

* [Maven](https://maven.apache.org/) - Dependency Management and build tool
* [Wildfly](http://wildfly.org/) - Enterprise JEE Application Server
* [Gson](https://github.com/google/gson) - Java Library to prase json to objects
* [Hamcrest](http://hamcrest.org/) - Used along with JUnit to provide extra feature
* [Mockito](http://site.mockito.org/) - Used to mock testing objects
* [Arquillian](http://arquillian.org/) - Java EE Integration tests
* [H2/HSQLDB](http://www.h2database.com/html/features.html) - Lightweight db used for Unit and Arquillian tests
* [Postman](https://www.getpostman.com/) - Used for manual tests on chrome extension
* [Eclipse](http://www.eclipse.org/) - Java Development IDE
* [PostgreSQL](https://www.postgresql.org/) - Relational opensource database


## Document
Please refer to the Wiki page of this project.

Javadocs can also be found at:
https://github.com/jiafanzhou/storehouse/tree/master/storehouse-app/docs

## Versioning

Use [GIT](https://git-scm.com/) for versioning. For the versions available, see the [releases on this repository](https://github.com/jiafanzhou/storehouse/releases). 

## Authors

* **Jiafan Zhou** - *Demo work* - [LinkedIn](https://ie.linkedin.com/in/jiafan-zhou-4341057)


## License

This project is licensed under the GNU GPL License - see the (https://en.wikipedia.org/wiki/GNU_General_Public_License) for details

