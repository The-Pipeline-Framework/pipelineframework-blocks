# Blocks Repository Instructions

This repository owns packaged reusable Pipeline Blocks. A Block is compile-time composition distributed as an
ordinary dependency; it does not own runtime implementation or application authority.

## Boundary

- Depend only on released TPF contracts, released Connectors, and ordinary third-party libraries.
- Keep Connector bindings, credentials, configuration, and Command authority with the consuming application.
- Keep runtime implementations, Quarkus deployment processors, Spring adapters, examples, and Expansion
  distribution machinery outside this repository.
- A Block may declare required callable or Connector capabilities but must not smuggle in provider authority.

## Cross-repository changes

Update canonical documentation or an ADR in `pipelineframework` when a change alters Block semantics or capability
ownership. Use the GitNexus `tpf` group for cross-repository impact and verify findings in the owning worktree. Do
not add source fallbacks for unpublished dependency changes.

## Build and publication

Owner-local verification is the first gate. `TPF Candidate Build` and the trusted publisher create an immutable,
commit-specific Blocks candidate for the coordination repository; `tpf/system-tests` records downstream evidence
on that exact source SHA. Use a compatibility set for coordinated repository changes, and require a green full
train for formal BOM or release promotion. Keep the stable owner suite command in `.github/tpf-system-tests.json`.

Always use the repository-local Maven cache:

```sh
./mvnw <goals> -Dmaven.repo.local="$PWD/.m2/repository"
```

Do not introduce Maven profiles except `central-publishing`. It may attach, sign, and deploy artifacts but must not
select another source universe, module graph, or build topology.

Do not commit, push, publish, or change another repository unless explicitly requested.
