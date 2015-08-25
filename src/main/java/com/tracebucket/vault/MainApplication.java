package com.tracebucket.vault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class MainApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

/*    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer(){
        return new VaultFileUploadHandler();
    }

    private static class VaultFileUploadHandler implements EmbeddedServletContainerCustomizer {

        @Override
        public void customize(ConfigurableEmbeddedServletContainer container) {
            container.addErrorPages(new ErrorPage(MultipartException.class, "/uploadError"));
        }
    }*/
}
