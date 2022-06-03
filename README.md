# SpringDBTest
SQLMapper vs ORM with Spring Transaction
---
### SQLMapper
1. JDBC Template
2. Mybatis
### ORM
1. Jpa(Hibernate)
2. SpringDataJpa
3. QueryDsl

---
#### 1. Init (InMemory)
1. 도메인
   - Item
2. 기능
   - 상품 조회
   - 상품 등록
   - 상품 수정
3. 환경
   - Spring MVC
   - Thymeleaf
   - Lombok

#### 2. JDBC template (with jdbc feature bransh)
##### [스프링 JdbcTemplate 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#jdbc-JdbcTemplate)
##### 장점
1. 설정편리
   ```properties
   # Spring으로 JDBC 사용시 기본적으로 사용되는 라이브러리
   # jdbctemplate 추가 
   implementation 'org.springframework.boot:spring-boot-starter-jdbc'
   ```
2. 반복 문제 해결
   ```
   1. 커넥션 흭득
   2. statement를 준비하고 실행
   3. 결과 반복하도록 루프 실행
   4. 커넥션 종료
   5. 트랜잭션을 다루기 위한 커넥션 동기화
   6. 예외 발생시 스프링 예외 변환기 실행 
   ```
##### 단점
1. **동적 SQL 해결 어렵**
##### JDBCTemplate 더 쉽게 활용하기
1. ```NamedParameterJdbcTemplate``` 클래스를 통해 파라미터의 이름을 지정할 수 있다.
   ```java
   public Item save(Item item) {
      String sql = "insert into item(item_name, price, quantity) values(:itemName, :price, :quantity)";
      //DB에서 autoincrement 방식을 사용하여 올려주는 PK 값을 저장하기 위해 사용
      KeyHolder keyHolder = new GeneratedKeyHolder();

      //Item Object가 가진 필드로 파라미터 만든다.
      SqlParameterSource param = new BeanPropertySqlParameterSource(item);
      jdbcTemplate.update(sql, param, keyHolder);
      item.setId(keyHolder.getKey().longValue());
      return item;
   }
   ```
2. ```MapSqlParameterSource``` 클래스로 파라미터 지정 가능
   ```java
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
   ```
3. ```Map``` 으로도 파라미터 지정 가능
      ```java
      String sql = "select id, item_name, price, quantity from item where id=:id";
      try {
          //Map 으로도 param 사용 가능하다.
          Map<String, Long> param = Map.of("id", id);
          Item item = jdbcTemplate.queryForObject(sql,param,itemRowMapper());
          return Optional.of(item);
      } catch (EmptyResultDataAccessException e){
          return Optional.empty();
      }
      ```
4. 스프링이 제공하는 ```BeanPropertyRowMapper```을 활용하여 쉽게 Table Row <-> Object 변환 가능
   ```java
      private RowMapper<Item> itemRowMapper() {
          return BeanPropertyRowMapper.newInstance(Item.class);
      }
   ```