# 날씨 일기 삭제 API
Repository에서 삭제 메서드를 만들 때 @Transactional을 붙여줘야 정상적으로 삭제가 된다.

---

## 1. Controller 에 Delete Mapping 하기
```java
@DeleteMapping("/delete/diary")
void deleteDiary(
        @RequestParam @DateTimeFormat(iso =
                DateTimeFormat.ISO.DATE) LocalDate date) {
    diaryService.deleteDiary(date);
}
```

---

## 2. Service 에 메서드 만들기
```java
public void deleteDiary(LocalDate date) {
    diaryRepository.deleteAllByDate(date);
}
```

---

## 3. JPA Repository에 쿼리 메서드 만들기
- Delete 쿼리에는 @Transactional이 필요한데 JPA가 트랜잭션을 사용하며 작동하기 때문이다. JPA는 DB 작업을 수행할 때 트랜잭션 컨텍스트 안에서 이루어져야 하므로 Delete 메서드는 트랜잭션에서 실행되도록 해야한다.
- @Transactional이 없다면 delete 메서드는 트랜잭션 컨텍스트 없이 삭제 작업을 시도하므로 이 작업의 결과를 추적할 수 없다. 따라서 삭제가 성공하더라도 JPA는 DB에 작업의 결과를 커밋하지 않는다.
- 다른 메서드가 @Transactional이 없어도 정상 작동하는 것은 이미 JPA가 트랜잭션에서 작동하도록 만들었거나, 단순히 데이터를 가져오는 것은 DB의 상태를 변경하지 않으므로 트랜잭션이 필요없기 때문이다. DB의 상태를 변경하는 메서드를 만들 땐 트랜잭션 내에서 작업해야하므로 @Transactional이 필수다.
```java
@Transactional
void deleteAllByDate(LocalDate date);
```

---