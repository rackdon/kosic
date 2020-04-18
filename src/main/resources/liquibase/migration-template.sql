--liquibase formatted sql

--changeset roc:migrationId rollbackSplitStatements:true
select 1

--comment: Each text for rollback must be preceded by --rollback

--rollback
--rollback
--rollback
