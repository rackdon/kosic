# kosic
Spring Boot example with Kotlin + Arrow and multiple databases


### Execute

```$xslt
$ /gradlew bootRun
```
if you receive the following problem whe trying to execute 
`Could not find or load main class org.gradle.wrapper.GradleWrapperMain`
run the following command to fix
```$xslt
$ gradle wrapper
```
The server is launched in 8080 port by default

### Run tests

There are 3 different ways of lunch the tests:

- Run unit tests
```$xslt
$ gradle test
```

- Run integration tests (needed up dependent systems as a db by hand)
```$xslt
$ gradle integrationTest
```

- Run all tests (needed up dependent systems as a db by hand)
```$xslt
$ gradle allTest
```

### Linter and formatter
We use a gradle plugin based on [ktlint](https://ktlint.github.io).

More info about the plugin [here](https://github.com/jeremymailen/kotlinter-gradle)

To launch the linter execute
```$xslt
$ gradle lintKotlin
```

To launch the formatter execute
```$xslt
$ gradle formatKotlin
```

### Database migrations (liquibase)
For local and test the values are taken from `application-local.yml` or `application-test.ymnl`
For prod is necessary specify the following values through env variable:
- -Dspring.datasource.url
- -Dspring.datasource.username
- -Dspring.datasource.passowrd

All commands need the argument `-Pdatabase=value` which specify the database where apply. 
The available values are `local`, `test`, `prod`.

##### Commands
- Update
```bash
$ gradle db -Pdatabase=local update
```

- Rollback
```bash
$ gradle db -Pdatabase=local rollbackCount -PliquibaseCommandValue=1
```


More commands info [here](http://www.liquibase.org/documentation/command_line.html)


##### New migrations
To create a new migration execute
```bash
$ gradle newDbMigration -PmigrationName=your-name
```
and then edit the new file located in `/src/main/resources/liquibase/migrations/x.your-name.sql`




