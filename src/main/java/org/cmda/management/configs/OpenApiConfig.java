package org.cmda.management.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cmdaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CMDA DEV - API Management")
                        .description("API de gestion CMDA DEV organisee pour Swagger et import Postman.")
                        .version("MVP-R9"))
                .addTagsItem(new Tag().name("01 - AUTH").description("Connexion JWT et recuperation du token."))
                .addTagsItem(new Tag().name("02 - USERS").description("Gestion des comptes utilisateurs par l'ADMIN."))
                .addTagsItem(new Tag().name("03 - SCOPE").description("Perimetre courant de l'utilisateur connecte."))
                .addTagsItem(new Tag().name("04 - MEMBERS - API SECURISEE").description("API membres MVP : listes, detail, creation, modification, cycle de vie, photo et exports."))
                .addTagsItem(new Tag().name("05 - STRUCTURES - PROVINCES").description("CRUD et cycle de vie des provinces."))
                .addTagsItem(new Tag().name("06 - STRUCTURES - REGIONS").description("CRUD et cycle de vie des regions."))
                .addTagsItem(new Tag().name("07 - STRUCTURES - FRATERNITES").description("CRUD et cycle de vie des fraternites."))
                .addTagsItem(new Tag().name("08 - DASHBOARD").description("Indicateurs MVP par role et par perimetre."))
                .addTagsItem(new Tag().name("99 - LEGACY - MEMBERS").description("Ancienne API /members/** conservee temporairement pour compatibilite."));
    }
}
