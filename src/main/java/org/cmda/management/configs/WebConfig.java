package org.cmda.management.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        // Enregistrer le module JavaTimeModule pour gérer LocalDate et les autres types Java 8
        objectMapper.registerModule(new JavaTimeModule());
        // Désactiver l'écriture des dates sous forme de tableau
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // Permettre CORS pour les routes d'authentification et autres
        registry.addMapping("/api/**") // Ajoutez cette ligne pour tous les endpoints de l'API
                .allowedOrigins("http://localhost:4200") // Remplacez par l'URL de votre frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                //.allowedHeaders("*")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                .allowCredentials(true);


        //
        registry.addMapping("/members/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                //.allowedHeaders("*")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                .allowCredentials(true);
    }

}
