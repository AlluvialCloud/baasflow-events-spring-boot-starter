## Configuration
Supply a standard spring configuration (properties or yaml) where you define Kafka brokers and topics for your scenarios. YAML example:
```
baasflow:
    commons.events:
        audit:
            kafka:
                brokers: "localhost:9092"
                topic: auditlog
        generic:
            kafka:
                brokers: "localhost:9092"
                topic: events
```


## Library usage
Add the library as a dependency in pom.xml, eg.:
```
        <dependency>
            <groupId>com.baasflow.commons.events</groupId>
            <artifactId>baasflow-commons-events-spring-boot-starter</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
```

then autowire com.baasflow.commons.events.EventService to your code and use its available methods.

## CLI runner
The CLI runner can send Baasflow Events right from the command line. Example:
`SPRING_PROFILES_ACTIVE=test java -jar target/*-cli.jar -e event1 -t audit -m module1 -s success -l INFO` 
