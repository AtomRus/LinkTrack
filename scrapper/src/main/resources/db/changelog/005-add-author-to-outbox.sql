--liquibase formatted sql

--changeset outbox:002
alter table outbox_event add column if not exists author text;

update outbox_event set author = 'unknown' where author is null;

alter table outbox_event alter column author set not null;
