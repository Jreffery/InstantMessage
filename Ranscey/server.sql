create table ServiceUser (
id int(11) NOT NULL AUTO_INCREMENT,
    username varchar(16),
    `password` varchar(32),
    mail varchar(50),
    register_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    primary key(id),
    index index_username (username ASC)
)ENGINE=InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET=utf8;


create table App (
appid varchar(16) NOT NULL,
    appname varchar(16),
    appowner int(11),
    create_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    primary key(appid),
    foreign key (appowner) references ServiceUser(id) ON DELETE CASCADE ON UPDATE CASCADE,
index index_appid (appid)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table AppUser (
id int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(20),
    `password` varchar(32),
    appid varchar(16) NOT NULL,
    primary key(id),
    foreign key (appid) references App (appid) ON DELETE CASCADE ON UPDATE CASCADE
)ENGINE=InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET=utf8;