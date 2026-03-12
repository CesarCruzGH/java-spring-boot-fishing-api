package com.pescayucatan.api_pesca_merida.config;

import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.repository.PezRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class DataSeeder {

    @Bean
    @ConditionalOnProperty(
            name = "app.seeder.enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    CommandLineRunner initDatabase(PezRepository repository) {
        return args -> {
            // Definimos el formato que estás usando (día-mes-año)
            // Nota: Para fechas sin año (como "01-02"), LocalDate requiere un año para ser un objeto válido.
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            int anioActual = 2026;

            // Mero
            repository.save(new Pez(null, "Mero", "Epinephelus morio", ZonaPesca.GOLFO_DE_MEXICO,
                    "Temporal Fija",
                    LocalDate.parse("01-02-" + anioActual, fmt),
                    LocalDate.parse("31-03-" + anioActual, fmt),
                    true));

            // Pulpo (Corregí el formato de "2026-02-21" a "21-02-2026" para que sea consistente con el resto)
            repository.save(new Pez(null, "Pulpo", "Octopus maya", ZonaPesca.YUCATAN,
                    "Temporal Fija",
                    LocalDate.parse("16-12-" + (anioActual - 1), fmt),
                    LocalDate.parse("31-07-" + anioActual, fmt),
                    true));

            // Langosta
            repository.save(new Pez(null, "Langosta", "Panulirus argus", ZonaPesca.GOLFO_DE_MEXICO,
                    "Temporal Fija",
                    LocalDate.parse("01-03-" + anioActual, fmt),
                    LocalDate.parse("30-06-" + anioActual, fmt),
                    true));

            // Pepino de Mar
            repository.save(new Pez(null, "Pepino de Mar", "Isostichopus badionotus", ZonaPesca.QUINTANA_ROO,
                    "Permanente",
                    LocalDate.parse("01-01-" + anioActual, fmt),
                    LocalDate.parse("31-12-" + anioActual, fmt),
                    true));

            System.out.println("Base de datos actualizada con objetos LocalDate para CONAPESCA 2026.");
        };
    }
}