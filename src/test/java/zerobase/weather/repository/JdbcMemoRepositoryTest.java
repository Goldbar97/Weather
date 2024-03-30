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
class JdbcMemoRepositoryTest {
    @Autowired
    JdbcMemoRepository jdbcMemoRepository;
    
    @Test
    void insertMemoTest() {
        // given
        Memo memo = new Memo(1, "this is new memo");
        // when
        jdbcMemoRepository.save(memo);
        Optional<Memo> result = jdbcMemoRepository.findById(1);
        // then
        assertEquals(result.get().getText(), "this is new memo");
    }
    
    @Test
    void findAllMemoTest() {
        // given
        Memo memo1 = new Memo(1, "this is new memo1");
        Memo memo2 = new Memo(2, "this is new memo2");
        Memo memo3 = new Memo(3, "this is new memo3");
        jdbcMemoRepository.save(memo1);
        jdbcMemoRepository.save(memo2);
        jdbcMemoRepository.save(memo3);
        // when
        List<Memo> memoList = jdbcMemoRepository.findAll();
        // then
        assertEquals(memoList.get(0).getText(), memo1.getText());
        assertEquals(memoList.get(1).getText(), memo2.getText());
        assertEquals(memoList.get(2).getText(), memo3.getText());
    }
    
    
}