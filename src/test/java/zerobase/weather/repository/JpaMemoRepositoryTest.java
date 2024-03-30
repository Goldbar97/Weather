package zerobase.weather.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class JpaMemoRepositoryTest {
    @Autowired
    JpaMemoRepository jpaMemoRepository;
    
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
}