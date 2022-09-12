# nva-cristin-service

Service for fetching data from the [Cristin API](https://api.cristin.no/v2/doc/index.html)

## Building locally

We need to add some test tag exclusions since the tagged tests are only used for integration 
testing on deployed instances.

### Windows

```gradlew build -x karateTest -x createTestUsers -x deleteTestUsers```

### Linux

```./gradlew build -x karateTest -x createTestUsers -x deleteTestUsers```

## Running karate tests

Karate tests are designed to be run in AWS via Github Actions. This requires a token that can
only be obtained when it is deployed as an AWS Cloudformation stack.

You can run the integration tests locally in your IDE by running the feature files directly for each 
scenario or each feature. If you have feature files that depend on a token, you can comment out 
token generation code and add your own token to the token variable.

Running it locally also needs an override of the environment variables, pointing to an existing
stack in AWS. That means we are running the tests locally against an already deployed AWS stack

### Example environment variables override in IntelliJ

In Edit Configuration -> Environment Variables :

```DOMAIN_NAME=api.dev.nva.aws.unit.no;BASE_PATH=cristin```

### AWS Stack

For karate tests we need an existing stack or use the dev stack from the regular pipeline.
If we want to test changes before they are in main stack, we have to deploy the stack manually.
We can either wait for a failing Github Karate Action and run tests against that stack, 
or deploy a stack manually.

## OpenApi

[NVA Developer Portal](https://portal.dev.nva.aws.unit.no/)

Or most recent in ```docs/cristin-proxy-swagger.yaml```

[Swagger Editor](https://editor.swagger.io/)