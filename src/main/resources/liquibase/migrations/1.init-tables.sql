--liquibase formatted sql

--changeset kosic:1 rollbackSplitStatements:true
create extension if not exists "uuid-ossp";

create table if not exists groups (
    id uuid default uuid_generate_v4() not null primary key,
    name text unique,
    members smallint,
    created_on timestamp without time zone not null,
    dissolved_on timestamp without time zone
);

create table if not exists albums (
    id uuid default uuid_generate_v4() not null primary key,
    group_id uuid not null,
    name text not null,
    created_on timestamp without time zone not null,
    constraint album_group_fk foreign key (group_id) references groups(id)
    on delete no action on update no action
);

create table if not exists songs (
    id uuid default uuid_generate_v4() not null primary key,
    album_id uuid not null,
    name text not null,
    duration int not null,
    created_on timestamp without time zone not null,
    meta jsonb,
    constraint song_album_fk foreign key (album_id) references albums(id)
    on delete no action on update no action
);

--comment: Each text for rollback must be preceded by --rollback

--rollback drop table if exists songs;
--rollback drop table if exists albums;
--rollback drop table if exists groups;
--rollback drop extension if exists "uuid-ossp";
