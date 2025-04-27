create table Compliance
(
    ComplianceID int auto_increment
        primary key,
    UserID       bigint                                    not null,
    Category     int                                       not null,
    OptedIn      tinyint(1)                                not null,
    DecidedAt    timestamp(6) default current_timestamp(6) not null,
    constraint Compliance_UserID_Category_unique
        unique (UserID, Category)
);

create table ServerConfigs
(
    ServerID                      bigint                     not null
        primary key,
    LogChannelID                  bigint                     null,
    LogJoin                       tinyint(1)  default 0      not null,
    LogLeave                      tinyint(1)  default 0      not null,
    LogKick                       tinyint(1)  default 0      not null,
    LogBan                        tinyint(1)  default 0      not null,
    LogNameChange                 tinyint(1)  default 0      not null,
    LogPurge                      tinyint(1)  default 0      not null,
    SelectableRolesLimit          int         default 1      not null,
    StarboardEnabled              tinyint(1)  default 0      not null,
    StarboardChannelID            bigint                     null,
    StarboardEmoji                varchar(32) default 'star' not null,
    StarboardThreshold            int         default 1      not null,
    StarboardAllowSelfStar        tinyint(1)  default 0      not null,
    QuickviewFuraffinityEnabled   tinyint(1)  default 1      not null,
    QuickviewFuraffinityThumbnail tinyint(1)  default 1      not null,
    QuickviewPicartoEnabled       tinyint(1)  default 1      not null,
    WikiMinQuality                int         default 50     not null,
    constraint check_ServerConfigs_0
        check (`SelectableRolesLimit` = -1 or `SelectableRolesLimit` >= 1),
    constraint check_ServerConfigs_1
        check (`StarboardThreshold` >= 1),
    constraint check_ServerConfigs_2
        check (`WikiMinQuality` between 0 and 100)
);

create table ServerSelectableRoles
(
    ServerID bigint not null,
    RoleID   bigint not null,
    constraint ServerSelectableRoles_ServerID_RoleID_unique
        unique (ServerID, RoleID),
    constraint fk_ServerSelectableRoles_ServerID__ServerID
        foreign key (ServerID) references ServerConfigs (ServerID)
            on update cascade on delete cascade
);

create table ServerWikiSources
(
    ServerID    bigint       not null,
    Destination varchar(100) not null,
    constraint ServerWikiSources_ServerID_Destination_unique
        unique (ServerID, Destination),
    constraint fk_ServerWikiSources_ServerID__ServerID
        foreign key (ServerID) references ServerConfigs (ServerID)
            on update cascade on delete cascade
);
