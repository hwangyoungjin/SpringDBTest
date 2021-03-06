package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SimpleJdbcInsert
 * JdbcTemplate은 INSERT SQL를 직접 작성하지 않아도 되도록 SimpleJdbcInsert 라는 편리한  기능을 제공
 */
@Slf4j
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("item")
                .usingGeneratedKeyColumns("id");
//                .usingColumns("item_name","price","quantity"); //생략가능
    }

    @Override
    public Item save(Item item) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        Number key = jdbcInsert.executeAndReturnKey(param);
        item.setId(key.longValue());
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=:itemName, price=:price, quantity=:quantity where id=:id";
        //Object가 아닌 필드명에 직접 넣어주는 방법, -> Key 값이 sql의 :key에 맵핑된다.
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);
        jdbcTemplate.update(sql, param);

    }

    @Override
    public Optional<Item> findById(Long id) {
        /**
         * BeanPropertyRowMapper 를 사용하므로 item_name의 결과 별칭 사용 필요할것
         * -> 관례상 자바는 카멜(itemName)표기법 사용, RDM는 언더스코어(item_name)사용
         * @But
         * BeanPropertyRowMapper는 이를 인식하고 있어 언더스코어를 카멜로 자동 변환 해준다.
         * -> 완전 다른 이름의 객체 필드명과 테이플 컬럼명이라면 별칭(as name) 사용 필요
         */
        String sql = "select id, item_name, price, quantity from item where id=:id";
        try {
            //Map 으로도 param 사용 가능하다.
            Map<String, Long> param = Map.of("id", id);
            Item item = jdbcTemplate.queryForObject(sql,param,itemRowMapper());
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }


    private RowMapper<Item> itemRowMapper() {
        /**
         * 스프링이 제공하는 ObjectMapper
         * 내부적으로 자바 리플렉션을 활용
         */
        return BeanPropertyRowMapper.newInstance(Item.class);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(cond);
        /**
         * BeanPropertyRowMapper 를 사용하므로 item_name의 결과 별칭 사용 필요할것
         * -> 관례상 자바는 카멜(itemName)표기법 사용, RDM는 언더스코어(item_name)사용
         * @But
         * BeanPropertyRowMapper는 이를 인식하고 있어 언더스코어를 카멜로 자동 변환 해준다.
         * -> 완전 다른 이름의 객체 필드명과 테이플 컬럼명이라면 별칭(as name) 사용 필요
         */String sql = "select id, item_name, price, quantity from item";
        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',:itemName,'%')";
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
        return jdbcTemplate.query(sql, sqlParameterSource, itemRowMapper());
    }
}
