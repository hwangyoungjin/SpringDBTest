package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTemplate
 *
 * @Method: jdbcTemplate.update() -> insert, update, delete등 데이터를 변경할때 사용
 * jdbcTemplate.query(String sql, RowMapper<T> rowMapper, @Nullable Object... args)
 *  -> 조회 결과가 하나 이상일때 사용, 결과가 없는 경우 빈 컬렉션 반환
 *  -> 내부적으로 while문을 통해 RowMapper<T> rowMapper를 수행하여 Row List를 Object List로 변환
 * jdbcTemplate.questForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args)
 *  -> 조회결과가 하나일때 사용
 */
@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price, quantity) values(?, ?, ?)";
        //DB에서 autoincrement 방식을 사용하여 올려주는 PK 값을 저장하기 위해 사용
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            //아래 "id"를 지정하여 insert 쿼리 실행 후 DB에서 생성된 id값을 조회
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);
        item.setId(keyHolder.getKey().longValue());
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";
        jdbcTemplate.update(sql,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id=?";
        try {
            Item item = jdbcTemplate.queryForObject(sql,itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    private RowMapper<Item> itemRowMapper() {
        /**
         * 1. 람다식을 통해 익명클래스 만들어 리턴!!
         */
//        return new RowMapper<Item>() {
//            @Override
//            public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
//                Item item = new Item();
//                item.setId(rs.getLong("id"));
//                item.setItemName(rs.getString("item_name"));
//                item.setPrice(rs.getInt("price"));
//                item.setQuantity(rs.getInt("quantity"));
//                return item;
//            };
//        };
        /**
         * 2. 람다식을 통해 간편하게 함수 리턴
         */
        return (rs, random) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        };
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();
        String sql = "select id, item_name, price, quantity from item";
        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',?,'%')";
            param.add(itemName);
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice);
        }
        log.info("sql={}", sql);
        return jdbcTemplate.query(sql, itemRowMapper(), param.toArray());
    }
}
