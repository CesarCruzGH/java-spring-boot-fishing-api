package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.infrastructure.csv.EspecieCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.VedaCsvRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para CsvParserService.
 *
 * COBERTURA:
 * - Parsing happy path (datos válidos)
 * - Manejo de UTF-8 BOM
 * - Campos vacíos/null
 * - Campos con comillas y comas
 * - Filas incompletas
 * - Headers inválidos
 * - Valores no numéricos en campos Integer
 *
 * @author Sistema de Ingesta CONAPESCA
 * @since 2026-03-17
 */
@SpringBootTest
@DisplayName("CsvParserService Tests")
class CsvParserServiceTest {

    @Autowired
    private CsvParserService parser;

    @Nested
    @DisplayName("Tests de Especies CSV")
    class EspeciesTests {

        @Test
        @DisplayName("Debe parsear CSV de especies válido correctamente")
        void testParseEspecies_HappyPath() {
            // Arrange
            String csv = """
                ID,NOMBRE COMÚN,ESPECIE,NOMBRE MAYA,TALLA MÍNIMA,HÁBITAT,TÉCNICA RECOMENDADA,ZONA
                5,Mero,Epinephelus morio,Xlavita,40 cm,Arrecifes,Curricán,Golfo de México
                7,Huachinango,Lutjanus campechanus,K'aan xook,25 cm,Rocosos,Línea de mano,Caribe
                """;

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear 2 especies");

            // Primera especie
            EspecieCsvRow mero = result.get(0);
            assertEquals(5, mero.id());
            assertEquals("Mero", mero.nombreComun());
            assertEquals("Epinephelus morio", mero.especieCientifica());
            assertEquals("Xlavita", mero.nombreMaya());
            assertEquals("40 cm", mero.tallaMinima());
            assertEquals("Arrecifes", mero.habitat());
            assertEquals("Curricán", mero.tecnicaRecomendada());
            assertEquals("Golfo de México", mero.zona());

            // Segunda especie
            EspecieCsvRow huachinango = result.get(1);
            assertEquals(7, huachinango.id());
            assertEquals("Huachinango", huachinango.nombreComun());
        }

        @Test
        @DisplayName("Debe manejar campos con comillas y comas")
        void testParseEspecies_ConComillas() {
            // Arrange
            String csv = """
                ID,NOMBRE COMÚN,ESPECIE,NOMBRE MAYA,TALLA MÍNIMA,HÁBITAT,TÉCNICA RECOMENDADA,ZONA
                5,Mero,"Epinephelus morio, variante roja",Xlavita,40 cm,"Arrecifes, fondos rocosos",Curricán,"Golfo de México, Zona Norte"
                """;

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            assertEquals("Epinephelus morio, variante roja", result.get(0).especieCientifica());
            assertEquals("Arrecifes, fondos rocosos", result.get(0).habitat());
            assertEquals("Golfo de México, Zona Norte", result.get(0).zona());
        }

        @Test
        @DisplayName("Debe manejar campos vacíos sin error")
        void testParseEspecies_CamposVacios() {
            // Arrange
            String csv = """
                ID,NOMBRE COMÚN,ESPECIE,NOMBRE MAYA,TALLA MÍNIMA,HÁBITAT,TÉCNICA RECOMENDADA,ZONA
                5,Mero,Epinephelus morio,,40 cm,Arrecifes,,Golfo
                """;

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            assertEquals("", result.get(0).nombreMaya()); // Campo vacío = string vacío
            assertEquals("", result.get(0).tecnicaRecomendada());
        }

        @Test
        @DisplayName("Debe saltar filas incompletas sin romper el proceso")
        void testParseEspecies_FilaIncompleta() {
            // Arrange
            String csv = """
                ID,NOMBRE COMÚN,ESPECIE,NOMBRE MAYA,TALLA MÍNIMA,HÁBITAT,TÉCNICA RECOMENDADA,ZONA
                5,Mero,Epinephelus morio,Xlavita,40 cm,Arrecifes,Curricán,Golfo
                7,Robalo
                9,Huachinango,Lutjanus campechanus,K'aan xook,25 cm,Rocosos,Línea,Caribe
                """;

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear solo las filas completas");
            assertEquals(5, result.get(0).id());
            assertEquals(9, result.get(1).id()); // Saltó la fila 7 incompleta
        }

        @Test
        @DisplayName("Debe retornar lista vacía si CSV solo tiene header")
        void testParseEspecies_SoloHeader() {
            // Arrange
            String csv = "ID,NOMBRE COMÚN,ESPECIE,NOMBRE MAYA,TALLA MÍNIMA,HÁBITAT,TÉCNICA RECOMENDADA,ZONA\n";

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertTrue(result.isEmpty(), "Debe retornar lista vacía si no hay datos");
        }

        @Test
        @DisplayName("Debe manejar UTF-8 BOM correctamente")
        void testParseEspecies_ConBOM() {
            // Arrange - BOM UTF-8 = EF BB BF
            byte[] csvWithBom = new byte[] {
                    (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, // BOM
                    'I', 'D', ',', 'N', 'O', 'M', 'B', 'R', 'E', ' ', 'C', 'O', 'M', 'N', ',',
                    'E', 'S', 'P', 'E', 'C', 'I', 'E', ',', 'N', 'O', 'M', 'B', 'R', 'E', ' ', 'M', 'A', 'Y', 'A', ',',
                    'T', 'A', 'L', 'L', 'A', ' ', 'M', 'N', 'I', 'M', 'A', ',',
                    'H',  'B', 'I', 'T', 'A', 'T', ',',
                    'T', 'C', 'N', 'I', 'C', 'A', ' ', 'R', 'E', 'C', 'O', 'M', 'E', 'N', 'D', 'A', 'D', 'A', ',',
                    'Z', 'O', 'N', 'A', '\n',
                    '5', ',', 'M', 'e', 'r', 'o', ',', 'E', 'p', 'i', 'n', 'e', 'p', 'h', 'e', 'l', 'u', 's', ',',
                    ',', ',', ',', ',', '\n'
            };

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csvWithBom);

            // Assert - No debe fallar por BOM
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(5, result.get(0).id());
        }
    }

    @Nested
    @DisplayName("Tests de Vedas CSV")
    class VedasTests {

        @Test
        @DisplayName("Debe parsear CSV de vedas válido correctamente")
        void testParseVedas_HappyPath() {
            // Arrange
            String csv = """
                Pez ID,Nombre Común,Especie Científica,Zona,Tipo de Veda,Inicio mes,Inicio día,Fin mes,Fin día,Inicio fijo,Fin fijo,Fuente DOF
                5,Mero,Epinephelus morio,Golfo,TEMPORAL FIJA,5,1,8,31,,,DOF-2024-001
                7,Huachinango,Lutjanus campechanus,Caribe,TEMPORAL VARIABLE,3,15,5,30,,,DOF-2024-002
                """;

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear 2 vedas");

            // Primera veda
            VedaCsvRow meroVeda = result.get(0);
            assertEquals(5, meroVeda.pezId());
            assertEquals("Mero", meroVeda.nombreComun());
            assertEquals("Epinephelus morio", meroVeda.especieCientifica());
            assertEquals("Golfo", meroVeda.zona());
            assertEquals("TEMPORAL FIJA", meroVeda.tipoVeda());
            assertEquals(5, meroVeda.inicioMes());
            assertEquals(1, meroVeda.inicioDia());
            assertEquals(8, meroVeda.finMes());
            assertEquals(31, meroVeda.finDia());
            assertEquals("DOF-2024-001", meroVeda.fuenteDof());
        }

        @Test
        @DisplayName("Debe manejar vedas PERMANENTES con fechas null")
        void testParseVedas_VedaPermanente() {
            // Arrange
            String csv = """
                Pez ID,Nombre Común,Especie Científica,Zona,Tipo de Veda,Inicio mes,Inicio día,Fin mes,Fin día,Inicio fijo,Fin fijo,Fuente DOF
                5,Mero,Epinephelus morio,Golfo,PERMANENTE,,,,,,,DOF-2024-003
                """;

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            VedaCsvRow veda = result.get(0);
            assertEquals("PERMANENTE", veda.tipoVeda());
            assertNull(veda.inicioMes(), "Inicio mes debe ser null en veda PERMANENTE");
            assertNull(veda.inicioDia(), "Inicio día debe ser null en veda PERMANENTE");
            assertNull(veda.finMes(), "Fin mes debe ser null en veda PERMANENTE");
            assertNull(veda.finDia(), "Fin día debe ser null en veda PERMANENTE");
        }

        @Test
        @DisplayName("Debe saltar filas sin Pez ID válido")
        void testParseVedas_SinPezId() {
            // Arrange
            String csv = """
                Pez ID,Nombre Común,Especie Científica,Zona,Tipo de Veda,Inicio mes,Inicio día,Fin mes,Fin día,Inicio fijo,Fin fijo,Fuente DOF
                5,Mero,Epinephelus morio,Golfo,TEMPORAL FIJA,5,1,8,31,,,DOF-2024-001
                ,Robalo,Centropomus,Golfo,TEMPORAL FIJA,3,1,5,31,,,DOF-2024-002
                INVALID,Huachinango,Lutjanus,Caribe,TEMPORAL FIJA,3,1,5,31,,,DOF-2024-003
                7,Corvina,Cynoscion,Golfo,TEMPORAL FIJA,4,1,6,30,,,DOF-2024-004
                """;

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear solo filas con Pez ID válido");
            assertEquals(5, result.get(0).pezId());
            assertEquals(7, result.get(1).pezId());
        }

        @Test
        @DisplayName("Debe manejar zonas con comillas y comas")
        void testParseVedas_ZonaConComillas() {
            // Arrange
            String csv = """
                Pez ID,Nombre Común,Especie Científica,Zona,Tipo de Veda,Inicio mes,Inicio día,Fin mes,Fin día,Inicio fijo,Fin fijo,Fuente DOF
                5,Mero,Epinephelus morio,"Golfo de México, Zona Norte y Centro",TEMPORAL FIJA,5,1,8,31,,,DOF-2024-001
                """;

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            assertEquals("Golfo de México, Zona Norte y Centro", result.get(0).zona());
        }

        @Test
        @DisplayName("Debe manejar valores no numéricos en campos de fecha")
        void testParseVedas_FechasInvalidas() {
            // Arrange
            String csv = """
                Pez ID,Nombre Común,Especie Científica,Zona,Tipo de Veda,Inicio mes,Inicio día,Fin mes,Fin día,Inicio fijo,Fin fijo,Fuente DOF
                5,Mero,Epinephelus morio,Golfo,TEMPORAL FIJA,N/A,TBD,8,31,,,DOF-2024-001
                """;

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            VedaCsvRow veda = result.get(0);
            assertNull(veda.inicioMes(), "Valor 'N/A' debe convertirse a null");
            assertNull(veda.inicioDia(), "Valor 'TBD' debe convertirse a null");
            assertEquals(8, veda.finMes());
            assertEquals(31, veda.finDia());
        }

        @Test
        @DisplayName("Debe saltar filas incompletas sin romper el proceso")
        void testParseVedas_FilaIncompleta() {
            // Arrange
            String csv = """
                Pez ID,Nombre Común,Especie Científica,Zona,Tipo de Veda,Inicio mes,Inicio día,Fin mes,Fin día,Inicio fijo,Fin fijo,Fuente DOF
                5,Mero,Epinephelus morio,Golfo,TEMPORAL FIJA,5,1,8,31,,,DOF-2024-001
                7,Robalo,Centropomus
                9,Huachinango,Lutjanus campechanus,Caribe,TEMPORAL FIJA,3,1,5,31,,,DOF-2024-002
                """;

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear solo las filas completas");
            assertEquals(5, result.get(0).pezId());
            assertEquals(9, result.get(1).pezId()); // Saltó la fila 7 incompleta
        }

        @Test
        @DisplayName("Debe manejar comillas escapadas en campos de texto")
        void testParseVedas_ComillasEscapadas() {
            // Arrange
            String csv = """
                Pez ID,Nombre Común,Especie Científica,Zona,Tipo de Veda,Inicio mes,Inicio día,Fin mes,Fin día,Inicio fijo,Fin fijo,Fuente DOF
                5,Mero,Epinephelus morio,Golfo,TEMPORAL FIJA,5,1,8,31,,,DOF 2024 ""Actualizado""
                """;

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            assertEquals("DOF 2024 Actualizado", result.get(0).fuenteDof());
        }
    }

    @Nested
    @DisplayName("Tests de Casos Edge")
    class EdgeCasesTests {

        @Test
        @DisplayName("Debe manejar CSV completamente vacío")
        void testParse_CsvVacio() {
            // Arrange
            String csv = "";

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertTrue(result.isEmpty(), "CSV vacío debe retornar lista vacía");
        }

        @Test
        @DisplayName("Debe manejar líneas con solo espacios")
        void testParse_LineasConEspacios() {
            // Arrange
            String csv = """
                Pez ID,Nombre Común,Especie Científica,Zona,Tipo de Veda,Inicio mes,Inicio día,Fin mes,Fin día,Inicio fijo,Fin fijo,Fuente DOF
                5,  Mero  ,  Epinephelus morio  ,Golfo,TEMPORAL FIJA,5,1,8,31,,,DOF-2024-001
                """;

            // Act
            List<VedaCsvRow> result = parser.parseVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            assertEquals("Mero", result.get(0).nombreComun().trim());
        }

        @Test
        @DisplayName("Debe manejar diferentes tipos de saltos de línea")
        void testParse_SaltosDeLinea() {
            // Arrange - Mezcla de \n y \r\n
            String csv = "ID,NOMBRE COMÚN,ESPECIE,NOMBRE MAYA,TALLA MÍNIMA,HÁBITAT,TÉCNICA RECOMENDADA,ZONA\r\n" +
                    "5,Mero,Epinephelus morio,Xlavita,40 cm,Arrecifes,Curricán,Golfo\n" +
                    "7,Huachinango,Lutjanus campechanus,K'aan xook,25 cm,Rocosos,Línea,Caribe\r\n";

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe manejar ambos tipos de salto de línea");
        }
    }
}
