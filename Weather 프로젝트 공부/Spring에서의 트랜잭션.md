# Spring 트랜잭션
@Transactional 어노테이션을 추가하면 트랜잭션 기능이 적용된 프록시 객체 (PlatformTransaction Manage) 를 생성할 수 있다.

---

## Spring 트랜잭션 세부 설정
### 1. Isolation (격리수준)
트랜잭션에서 일관성 없는 데이터를 허용하는 수준을 결정한다.

일반적으로 DEFAULT를 사용하며 돈 계산과 같이 중요한 작업에 해당하는 부분만 높은 격리수준을 적용한다.

@Transaction(isolation = Isolation.DEFAULT) 와 같이 적용한다.
- DEFAULT
- READ-UNCOMMITTED (Dirty Read 발생)
- READ_COMMITTED (Dirty Read 방지)
- REPEATABLE_READ (Non-Repeatable Read 방지)
    - 수정하는 동안 해당 데이터에 다른 트랜잭션 접근 불가
- SERIALIZABLE (Phantom Read 방지)
    - 수정하는 동안 모든 데이터에 다른 트랜잭션 접근 불가

---

### 2. Propagation (전파수준)
트랜잭션 동작중 다른 트랜잭션을 호출하는 상황에서 다른 트랜잭션은 어떻게 동작할 지를 결정한다.
- REQUIRED
    - 현재 실행 중인 트랜잭션이 있으면 해당 트랜잭션에 참여하고, 그렇지 않으면 새로운 트랜잭션으로 시작한다.
- SUPPORTS
    - 현재 실행 중인 트랜잭션이 있으면 해당 트랜잭션에 참여하고, 그렇지 않으면 트랜잭션 없이 바로 실행한다. 트랜잭션이 없어도 되는 메서드에 사용하며 트랜잭션이 있다면 해당 트랜잭션 안에서 실행되기를 원할 때 사용한다.
- MANDATORY
    - 실행중인 트랜잭션이 반드시 있어야한다. MANDATORY로 설정하면 이미 실행 중인 트랜잭션 내에서 호출되어야 한다. 반드시 실행되어야 하는 메서드에 설정한다.
- REQUIRES_NEW
    - 호출되면 항상 새로운 트랜잭션을 시작한다. 현재 트랜잭션과는 독립적으로 실행되므로 트랜잭션 분리가 발생한다.
- NESTED
    - 부모 트랜잭션 내에서 중첩된 트랜잭션을 시작한다. 부모가 롤백되면 전부 롤백된다. 중첩된 트랜잭션은 부모와는 별도의 트랜잭션을 제공하므로 부모의 결과와 독립적으로 커밋이나 롤백을 할 수 있다.

---

### 3. ReadOnly 속성
읽기 외(저장, 수정, 삭제) 작업이 발생 시 예외가 발생하도록 지정한다.

@Transactional(readOnly = true) 로 지정하며 성능 향상에 도움을 줄 수 있다.

---

### 4. 트랜잭션 롤백 예외
특정 예외가 발생했을 때 롤백을 진행할 지 결정하는 설정이다. 다음은 모든 Exception에 대한 설정을 지정한 것이다.

기본값은 RuntimeException, Error이다.

- @Transactional(rollbackFor=Exception.class)
- @Transactional(noRollbackFor=Exception.class)

---

### 5. timeout 속성
일정 시간내에 트랜잭션을 끝내지 못하면 롤백시키는 설정이다. 단위는 '초'이다.

- @Transactional(timeout=10)

---
