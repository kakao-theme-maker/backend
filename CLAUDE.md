<!-- project 공통 규칙 / 컨벤션 -->

## 프로젝트 공통 규칙 및 컨벤션

### 네이밍 컨벤션

- path variable : camel case
- query string : camel case
- request body : camel case
- response body : camel case

### 코딩 컨벤션

- 새로 개발한 메서드에 대해서는 JavaDocs 주석 작성하기

```
/**
메서드 역할 ( 책임 ) 필수
- @param 파라미터 종류 ( 간단한거 같으면 생략 )
- @return 리턴값 ( 간단한거 같으면 생략 )
- @throw 예외값
*/
```

- NullPointException이나 비즈니스 규칙 상 범위를 벗어날 수 있는 값들에 대해서는 예외처리하기

### 아키텍처 규칙

- DB 접근은 반드시 Repository 레이어를 통해 진행
- 비즈니스 로직은 service 레이어에서 처리
- 단일 도메인 관련 비즈니스 로직은 service에서, 복합 서비스 로직은 facade에서 처리

### [CRITICAL]

- 프로젝트의 기존 구조와 구현 방식을 최대한 유지하면서 요구사항을 구현하기
- 불필요한 추상화나 미래의 요구사항을 위한 구조 변경은 하지 않기
- 구현 후 관련 테스트를 실행하고, 필요한 테스트가 없다면 적절한 테스트 추가하기
- 너무 과한 경우에 대한 테스트는 작성하지 않기
- 구현과 테스트가 완료된 후 Knowledge Graph(KG)를 갱신하기
  - python venv 위치: {projectRoot}/../venv
  - 실행 명령어: code-review-graph build
  - KG 갱신은 최종 코드 변경과 테스트가 완료된 이후 마지막 단계에서 수행하기

### 테스트 규칙

- 통합 테스트 코드는 반드시 작성
- 단위 테스트의 경우 변경의 핵심 로직 / 문제 발생 가능성이 높은 로직의 경우 작성
- 테스트 작성 시 테스트의 역할을 @DisplayName을 통해 명시

<!-- code-review-graph MCP tools -->

## MCP Tools: code-review-graph

**This project has a knowledge graph. Start with the code-review-graph
MCP tools to narrow scope, then read the source.** The graph is cheaper than scanning files and
gives you structural context (callers, dependents, test coverage) that file search cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes_tool` or `query_graph_tool` instead of Grep
- **Understanding impact**: `get_impact_radius_tool` instead of manually tracing imports
- **Code review**: `detect_changes_tool` + `get_review_context_tool` instead of reading entire files
- **Finding relationships**: `query_graph_tool` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview_tool` + `list_communities_tool`

### Verify in the source

- Narrow scope with the graph, then read the source. Do not change code from graph output alone.
- For any non-trivial change, read the implementation and the relevant tests before concluding.
- Verify the exact source when touching behavior, database logic, migrations, retries, fallbacks,
  recovery, or compatibility code.
- When the graph and the source disagree, the source wins. The graph may be stale or may not
  model that relationship.
- An empty graph result can mean "not indexed" or "not statically visible", not "does not exist".

### Key Tools

| Tool                             | Use when                                               |
| -------------------------------- | ------------------------------------------------------ |
| `detect_changes_tool`            | Reviewing code changes — gives risk-scored analysis    |
| `get_review_context_tool`        | Need source snippets for review — token-efficient      |
| `get_impact_radius_tool`         | Understanding blast radius of a change                 |
| `get_affected_flows_tool`        | Finding which execution paths are impacted             |
| `query_graph_tool`               | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes_tool`     | Finding functions/classes by name or keyword           |
| `get_architecture_overview_tool` | Understanding high-level codebase structure            |
| `refactor_tool`                  | Planning renames, finding dead code                    |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes_tool` for code review.
3. Use `get_affected_flows_tool` to understand impact.
4. Use `query_graph_tool` pattern="tests_for" to check coverage.
<!-- /code-review-graph MCP tools -->
