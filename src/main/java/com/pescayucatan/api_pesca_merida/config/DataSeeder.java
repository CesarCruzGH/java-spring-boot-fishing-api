package com.pescayucatan.api_pesca_merida.config;

import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.repository.PezRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(PezRepository repository) {
        return args -> {
            // Mero: Veda del 1 de feb al 31 de mar en el Golfo de México [cite: 443, 445]
            repository.save(new Pez(null, "Mero", "Epinephelus morio", ZonaPesca.GOLFO_DE_MEXICO, "Temporal Fija", "01-02", "31-03", true));

            // Pulpo: Veda del 16 de dic al 31 de jul en Yucatán y Campeche [cite: 571]
            repository.save(new Pez(null, "Pulpo", "Octopus maya",
                    ZonaPesca.YUCATAN, "Temporal Fija", "16-12", "31-07", true));

            // Langosta: Veda del 1 de mar al 30 de jun en el Golfo de México [cite: 339]
            repository.save(new Pez(null, "Langosta", "Panulirus argus",
                    ZonaPesca.GOLFO_DE_MEXICO, "Temporal Fija", "01-03", "30-06", true));

            // Pepino de Mar: Veda Permanente en Yucatán [cite: 480, 482]
            repository.save(new Pez(null, "Pepino de Mar", "Isostichopus badionotus",
                    ZonaPesca.QUINTANA_ROO, "Permanente", "01-01", "31-12", true));

            System.out.println("Base de datos actualizada con datos oficiales de CONAPESCA 2026.");
        };
    }
}