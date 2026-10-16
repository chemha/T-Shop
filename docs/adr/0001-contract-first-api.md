# 0001. Contract-first API with OpenAPI

## Context

The API is the product of this project: a frontend (or anyone else) builds against it. The
documentation must never drift from what the server actually does.

## Decision

The API is written first in [`openapi.yaml`](../../src/main/resources/static/openapi.yaml). At
build time OpenAPI Generator creates one Java interface per tag and the DTOs, including Bean
Validation constraints taken from the contract (`maxLength`, `minimum`...). Controllers implement
those interfaces, so a path, parameter or type that differs from the contract doesn't compile.

Swagger UI displays the same `openapi.yaml`, not a description generated from the code.

## Consequences

- The contract is reviewed on its own, before any code, and is the single source of truth.
- Validation rules are declared once.
- Generated DTOs are mutable classes with setters, not records, and the generator's own
  conventions (`@Validated` interfaces, `org.springframework.lang.Nullable`) must be accommodated.
- Mapping entities to DTOs is extra code; MapStruct generates it at compile time and fails the
  build if a DTO field is left unmapped.
