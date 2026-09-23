# The Pipeline Framework Blocks

This repository publishes reusable, compile-time Pipeline Blocks for TPF. A Block is an ordinary Maven dependency
containing Pipeline definitions; applications retain Connector bindings, credentials, configuration, and Command
authority for every capability it requires.

Published artifacts:

- `org.pipelineframework.blocks:document-text-extraction`
- `org.pipelineframework.blocks:graphql`
- `org.pipelineframework.blocks:graphql-agent`
- `org.pipelineframework.blocks:openapi-representation-mapper`

Blocks consume released TPF contract and Connector artifacts. They do not own a runtime implementation, Quarkus
extension, customer deployment, or separately operated worker infrastructure.

Build with an isolated Maven repository:

```sh
./mvnw clean verify -Dmaven.repo.local="$PWD/.m2/repository"
```

Use the `central-publishing` profile only to sign and deploy the canonical reactor. See
[Publish a Block](https://pipelineframework.org/develop/blocks/publish) and
[TPF Components and Repositories](https://pipelineframework.org/architecture/components-and-repositories).

## System-test candidates

`TPF Candidate Build` runs at the exact pull-request or `main` SHA with read-only permissions and no secrets. It
assigns the reactor a commit-specific `-pr.<number>.<sha12>` or `-main.<sha12>` version, verifies it, and uploads only
the allowlisted Maven files and preliminary metadata. The trusted `TPF Candidate Publish` workflow validates that
build and the current head, publishes those files to this repository's GitHub Packages registry, then dispatches
`tpf-candidate-v1` to the coordination repository. It does not execute project or fork code.

Fork pull requests require `safe-to-system-test`. Configure `SYSTEM_TEST_APP_ID` as a repository variable and
`SYSTEM_TEST_APP_PRIVATE_KEY` as a repository secret for the coordination GitHub App. Candidate publication uses no
Maven Central credentials or GPG key.
