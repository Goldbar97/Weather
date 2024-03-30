# JPA 연동하기
JPA는 JDBC와 달리 직접 Query문을 작성하지 않는 ORM이다. DB 테이블과 Java 객체를 매핑해준다.

## 1. build.gradle에 dependency 추가하기
```gradle
// <JPA>
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client:3.3.3'
// </>
```

## 2. application.properties에 속성 추가하기
```properties
spring.jpa.show-sql=true
spring.jpa.database=mysql
```

## 3. 테이블 Class 에 @Entity 붙이기
    - 테이블의 세부 Entity인 경우 세부 Entity Class에 @Table(name = "[테이블 이름]") 어노테이션을 붙인다.
    - @Id 어노테이션을 붙여 Primary Key 값을 지정하고 Primary Key 값에 @GeneratedValue 를 붙여 자동으로 값을 설정하게 한다.
    - @GeneratedValue 에 (strategy) 를 지정할 수 있다. strategy를 지정하지 않으면 AUTO로 지정된다.
        - GenerationType.AUTO: 자동으로 strategy 지정
        - GenerationType.IDENTITY: DB에 의한 키값 지정
        - GenerationType.SEQUENCE: DB가 지원하는 SEQUENCE 생성기를 정의해서 사용
        - GenerationType.TABLE: DB가 자동 증가와 SEQUENCE를 지원하지 않을 때 Table 기반 생성기를 정의해서 사용
```java
package zerobase.weather.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Entity
@Getter
@NoArgsConstructor
@Setter
public class Memo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String text;
}
```

## 4. DB와 연동시켜주는 Repository 만들기
- JDBC 와 달리 interface 로 생성한다.
- 스프링에게 이 인터페이스가 Repository 임을 알려주기 위해 @Repository 어노테이션을 붙인다.
- ORM 개념이 정의된 JpaRepository 인터페이스를 상속받는다.
- 상속받을 때 JpaRepository 제네릭에 Entity 클래스와 Entity PK의 자료형을 넣어준다.
- 아무 내용이 없어보이더라도 웬만한 CRUD 기능을 바로 사용할 수 있다. (ex: save, findById, deleteById ...)
```java
package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

@Repository
public interface JpaMemoRepository extends JpaRepository<Memo, Integer> {
}
```

## 5. Test 해보기
- 특히 findByIdTest()에서 save할 때 입력한 Id값과 무관하게 저장되는 것을 알 수 있다. @GeneratedValue에 의한 결과이다. 따라서 save할 때 반환되는 객체의 id값을 통해서 찾아야 한다.
```java
package zerobase.weather.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JpaMemoRepositoryTest {
    @Autowired
    JpaMemoRepository jpaMemoRepository;
    
    @Test
    void insertMemoTest() {
        // given
        Memo memo = new Memo(10, "this is jpa memo");
        
        // when
        jpaMemoRepository.save(memo);
        
        // then
        List<Memo> memoList = jpaMemoRepository.findAll();
        assertTrue(!memoList.isEmpty());
    }
    
    @Test
    void findByIdTest() {
        // given
        Memo memo = new Memo(11, "jpa");
        
        // when
        Memo save = jpaMemoRepository.save(memo);
        Optional<Memo> optionalMemo = jpaMemoRepository.findById(save.getId());
        
        // then
        assertEquals(memo.getText(), optionalMemo.get().getText());
    }
}
```