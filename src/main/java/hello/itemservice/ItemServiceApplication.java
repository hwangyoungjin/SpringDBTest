package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;


@Import(MemoryConfig.class) //@Import를 사용하면 설정 파일간의 계층을 만들 수 있다.
@SpringBootApplication(scanBasePackages = "hello.itemservice.web") // 해당 패키지만 ComponentScan해서 자동으로 빈 등록
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	/**
	 * local이라는 이름으로 (application.properties)에서 Acive 되었을때
	 * 아래 Bean을 등록
	 */
	@Bean
	@Profile("local")
	public TestDataInit testDataInit(ItemRepository itemRepository) {
		return new TestDataInit(itemRepository);
	}

}
