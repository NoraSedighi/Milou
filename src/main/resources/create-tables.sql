use milou_db;

create table users (
    id int auto_increment primary key ,
    name nvarchar(100) not null ,
    email nvarchar(200) not null unique ,
    password nvarchar(100) not null
);

create table emails (
    id int auto_increment primary key ,
    code nvarchar(6) not null unique ,
    sender_email nvarchar(100) not null ,
    subject nvarchar(100),
    body text,
    sent_at datetime default current_timestamp
);

create table email_recipients (
    id int auto_increment primary key ,
    email_code nvarchar(6) not null,
    recipient_email nvarchar(100) not null,
    is_read boolean default false,
    foreign key (email_code) references emails(code)
);

select * from users;
select * from emails;
select * from email_recipients;