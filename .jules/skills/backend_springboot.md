# Backend Spring Boot Skills

## Tech Stack
- Java 21 (Virtual Threads Enabled)
- Spring Boot 3 + Spring MVC
- Database: H2 (MVP) -> PostgreSQL
- Async/Messaging: Spring Cloud Stream

## Coding Guidelines
1. **Virtual Threads**: Ensure configuration explicitly enables Virtual Threads (`spring.threads.virtual.enabled=true`). Avoid thread-pinning blocking calls.
2. **Layered Architecture**: Strictly follow Controller -> Service -> Repository layers.
3. **Immutability**: Prefer Java `record` for DTOs and Data structures whenever possible.
4. **Error Handling**: Use `@RestControllerAdvice` for global exception handling. Do not expose internal stack traces to the API response.

## PR Checklist
- [ ] 所有變更皆符合 Java 21 與 Spring Boot 3 標準。
- [ ] 已通過單元測試 (`mvn test`)，Test Coverage > 80%。
- [ ] API 變更已同步更新至 `docs/` 或 OpenAPI specification。
- [ ] 已同步撰寫與更新 `CHANGELOG.md` 中提及對應的 Task ID 與變更內容。
