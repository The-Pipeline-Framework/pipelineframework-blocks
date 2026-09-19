# The Pipeline Framework Blocks

This repository publishes reusable, compile-time pipeline Blocks for The Pipeline Framework (TPF).
Applications own the connector bindings and Command authority for every capability required by a Block.

Published artifacts:

- `org.pipelineframework.blocks:document-text-extraction`
- `org.pipelineframework.blocks:graphql`
- `org.pipelineframework.blocks:graphql-agent`
- `org.pipelineframework.blocks:openapi-representation-mapper`

The Blocks consume released TPF contract and connector artifacts. They do not own a runtime implementation,
Quarkus extension, customer deployment, or TPF-operated worker infrastructure.

Build with an isolated Maven repository:

```sh
./mvnw clean verify -Dmaven.repo.local="$PWD/.m2/repository"
```

Override `pipelineframework.contracts.version` or `pipelineframework.connectors.version` only when deliberately
testing another compatible released contract set.
