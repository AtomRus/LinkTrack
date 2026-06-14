--liquibase formatted sql

--changeset outbox:001
create table if not exists outbox_event (
    event_id bigserial primary key,
    link_id bigint not null,
    url text not null,
    description text not null,
    tg_chat_ids jsonb not null,
    status varchar(16) not null default 'NEW',
    attempts int not null default 0,
    last_error text null,
    created_at timestamptz not null default now(),
    sent_at timestamptz null
);

create index if not exists idx_outbox_event_status_created_at
    on outbox_event (status, created_at);

