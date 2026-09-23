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

## System-test candidates

The `TPF Candidate Build` workflow runs with read-only permissions and no secrets. It checks out the
exact PR head (or main push), assigns the reactor a commit-specific `-pr.<number>.<sha12>` or
`-main.<sha12>` version, verifies and installs it, then uploads only Maven files and preliminary build
metadata. The separate, trusted `TPF Candidate Publish` workflow validates that build and the current
PR head before publishing the allowlisted files to this repository's GitHub Packages Maven registry.
It then creates the final `tpf-candidate-manifest` and `tpf-candidate-event` artifacts and dispatches
`tpf-candidate-v1` to the framework repository. It does not run project or fork code.

Fork pull requests require the `safe-to-system-test` label before candidate publication. Configure
`SYSTEM_TEST_APP_ID` as a repository variable and `SYSTEM_TEST_APP_PRIVATE_KEY` as a repository secret for a GitHub App
installed on `The-Pipeline-Framework/pipelineframework` with Contents write permission. The package
publisher uses the workflow `GITHUB_TOKEN` with Packages write permission; no Central credentials or
GPG key are used by candidate publication.
