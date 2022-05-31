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
- 장점
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
- 단점
  1. 동적 SQL 해결 어렵