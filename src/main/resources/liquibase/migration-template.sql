--liquibase formatted sql

--changeset kosic:migrationId rollbackSplitStatements:true
select 1

--comment: Each text for rollback must be preceded by --rollback

--rollback
--rollback
--rollback
