create table public.developers
(
    id        serial primary key,
    email     varchar(255),
    firs_name varchar(255),
    last_name varchar(255),
    specialty varchar(255),
    status    varchar(255) check ((status)::text = ANY
                                  ((ARRAY ['ACTIVE':: character varying, 'DELETED':: character varying])::text))
);