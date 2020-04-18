DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT
      FROM   pg_catalog.pg_user
      WHERE  usename = 'rackdon') THEN

      CREATE ROLE rackdon WITH PASSWORD 'rackdon' CREATEDB LOGIN;
      ALTER USER "rackdon" with superuser;
   END IF;
END
$$;
