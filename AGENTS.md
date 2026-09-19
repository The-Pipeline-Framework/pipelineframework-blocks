# Repository instructions

This repository owns packaged reusable pipeline Blocks for The Pipeline Framework. A Block is compile-time
pipeline composition distributed as an ordinary Maven dependency. Applications retain connector bindings and
Command authority for capabilities used by a Block.

Keep this repository dependent only on released TPF contracts, released connectors, and ordinary third-party
libraries. Runtime implementations, Quarkus deployment processors, Spring adapters, examples, and Expansion
distribution machinery do not belong here.

Always use an isolated Maven local repository:

```sh
./mvnw <goals> -Dmaven.repo.local="$PWD/.m2/repository"
```

Do not introduce Maven profiles except `central-publishing`, which may attach, sign, and deploy the canonical
reactor but must not select a different source universe or module graph.
