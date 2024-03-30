# JDBC 연동하기
JDBC는 JPA와 달리 직접 Query문을 작성해야하는 SQL Mapper이다.

## 1. build.gradle에 dependency 추가하기
```gradle
// <MariaDB JDBC>
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
runtimeOnly 'org.mariadb.jdbc:mariadb-java-client:3.3.3'
// </>
```

## 2. application.properties에 속성 추가하기
```properties
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.datasource.url=jdbc:mariadb://localhost:3306/project?serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=mariadb
```

## 3. DB에 테이블이 있다면 똑같이 Java Class로 만들어주기
```java
package zerobase.weather.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Memo {
    private int id;
    private String text;
}
```
## 4. DB와 연동시켜주는 객체인 Repository 만들기
- 스프링에게 이 객체가 Repository 객체임을 알려주기 위해 @Repository 어노테이션을 붙인다.
- @Autowired와 DataSource를 사용해서 application.properties에 작성한 속성들을 받아온다.
- Repository 객체가 sql문을 실행하면 결과를 ResultSet에 받아온다. 이 ResultSet을 Memo 객체로 변환하려면 RowMapper를 사용한다.
- findById는 결과가 없을 수도 있으므로 NullPointerException이 발생할 수 있다. stream().findFirst()를 붙이면 Null Safe한 Optional 객체로 Wrapping 해서 반환해준다.
```java
package zerobase.weather.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMemoRepository {
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public JdbcMemoRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public List<Memo> findAll() {
        String sql = "select * from memo";
        return jdbcTemplate.query(sql, memoRowMapper());
    }
    
    public Optional<Memo> findById(int id) {
        String sql = "select * from memo where id = ?";
        return jdbcTemplate.query(sql, memoRowMapper(), id).stream()
                .findFirst();
    }
    
    private RowMapper<Memo> memoRowMapper() {
        return (rs, rowNum) -> new Memo(rs.getInt("id"), rs.getString("text"));
    }
    
    public Memo save(Memo memo) {
        String sql = "insert into memo values(?,?)";
        jdbcTemplate.update(sql, memo.getId(), memo.getText());
        return memo;
    }
}
```