package ch.psi.wica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class WicaApplication2 extends SpringBootServletInitializer
{

   @Override
   protected SpringApplicationBuilder configure( SpringApplicationBuilder application) {
      return application.sources(WicaApplication2.class);
   }

   public static void main(String[] args) {
      SpringApplication.run( WicaApplication2.class, args);
   }

}