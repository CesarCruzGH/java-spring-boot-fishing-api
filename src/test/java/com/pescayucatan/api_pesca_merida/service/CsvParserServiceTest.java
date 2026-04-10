package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.infrastructure.csv.EspecieCsvRow;
import com.pescayucatan.api_pesca_merida.infrastructure.csv.PeriodoVedaCsvRow;
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
                ID,NOMBRE COMÚN,NOMBRE CIENTÍFICO,NOMBRE MAYA,DESCRIPCIÓN,RIESGO CIGUATERA,ES INVASIVA,ES PROTEGIDA,TIPO AGUA,MIGRATORIO
                5,Mero,Epinephelus morio,Xlavita,Pez de arrecife importante,No,FALSE,TRUE,Salada,TRUE
                7,Huachinango,Lutjanus campechanus,K'aan xook,Pez demersal comercial,No,FALSE,FALSE,Salada,FALSE
                """;

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear 2 especies");

            // Primera especie
            EspecieCsvRow mero = result.get(0);
            assertEquals(5, mero.id());
            assertEquals("Mero", mero.nombreComun());
            assertEquals("Epinephelus morio", mero.nombreCientifico());
            assertEquals("Xlavita", mero.nombreMaya());
            assertEquals("Pez de arrecife importante", mero.descripcion());
            assertEquals("No", mero.riesgoCiguatera());
            assertEquals(false, mero.esInvasiva());
            assertEquals(true, mero.esProtegida());
            assertEquals("Salada", mero.tipoAgua());
            assertEquals(true, mero.migratorio());

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
                ID,NOMBRE COMÚN,NOMBRE CIENTÍFICO,NOMBRE MAYA,DESCRIPCIÓN,RIESGO CIGUATERA,ES INVASIVA,ES PROTEGIDA,TIPO AGUA,MIGRATORIO
                5,Mero,"Epinephelus morio, variante roja",Xlavita,"Pez de arrecife, muy apreciado",No,FALSE,FALSE,"Salada, con influencia dulce",FALSE
                """;

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            assertEquals("Epinephelus morio, variante roja", result.get(0).nombreCientifico());
            assertEquals("Pez de arrecife, muy apreciado", result.get(0).descripcion());
            assertEquals("Salada, con influencia dulce", result.get(0).tipoAgua());
        }

        @Test
        @DisplayName("Debe manejar campos vacíos sin error")
        void testParseEspecies_CamposVacios() {
            // Arrange
            String csv = """
                ID,NOMBRE COMÚN,NOMBRE CIENTÍFICO,NOMBRE MAYA,DESCRIPCIÓN,RIESGO CIGUATERA,ES INVASIVA,ES PROTEGIDA,TIPO AGUA,MIGRATORIO
                5,Mero,Epinephelus morio,,,No,,FALSE,,TRUE
                """;

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            assertEquals("", result.get(0).nombreMaya()); // Campo vacío = string vacío
            assertEquals("", result.get(0).descripcion());
        }

        @Test
        @DisplayName("Debe saltar filas incompletas sin romper el proceso")
        void testParseEspecies_FilaIncompleta() {
            // Arrange
            String csv = """
                ID,NOMBRE COMÚN,NOMBRE CIENTÍFICO,NOMBRE MAYA,DESCRIPCIÓN,RIESGO CIGUATERA,ES INVASIVA,ES PROTEGIDA,TIPO AGUA,MIGRATORIO
                5,Mero,Epinephelus morio,Xlavita,Pez importante,No,FALSE,FALSE,Salada,TRUE
                7,Robalo
                9,Huachinango,Lutjanus campechanus,K'aan xook,Pez comercial,No,FALSE,FALSE,Salada,FALSE
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
            String csv = "ID,NOMBRE COMÚN,NOMBRE CIENTÍFICO,NOMBRE MAYA,DESCRIPCIÓN,RIESGO CIGUATERA,ES INVASIVA,ES PROTEGIDA,TIPO AGUA,MIGRATORIO\n";

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
                    'N', 'O', 'M', 'B', 'R', 'E', ' ', 'C', 'I', 'E', 'N', 'T', 'I', 'F', 'I', 'C', 'O', ',',
                    'N', 'O', 'M', 'B', 'R', 'E', ' ', 'M', 'A', 'Y', 'A', ',',
                    'D', 'E', 'S', 'C', 'R', 'I', 'P', 'C', 'I', 'O', 'N', ',',
                    'R', 'I', 'E', 'S', 'G', 'O', ' ', 'C', 'I', 'G', 'U', 'A', ',',
                    'E', 'S', ' ', 'I', 'N', 'V', 'A', 'S', 'I', 'V', 'A', ',',
                    'E', 'S', ' ', 'P', 'R', 'O', 'T', 'E', 'G', 'I', 'D', 'A', ',',
                    'T', 'I', 'P', 'O', ' ', 'A', 'G', 'U', 'A', ',',
                    'M', 'I', 'G', 'R', 'A', 'T', 'O', 'R', 'I', 'O', '\n',
                    '5', ',', 'M', 'e', 'r', 'o', ',', 'E', 'p', 'i', 'n', 'e', 'p', 'h', 'e', 'l', 'u', 's', ',',
                    ',', ',', ',', ',', ',', ',', ',', ',', '\n'
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
        @DisplayName("Debe parsear CSV de periodos veda valido correctamente")
        void testParsePeriodoVedas_HappyPath() {
            // Arrange - Formato real: ID | Regulacion ID | Tipo Veda | Mes Inicio | Dia Inicio | Mes Fin | Dia Fin | Fuente DOF
            String csv = """
                ID,Regulacion ID,Tipo Veda,Mes Inicio,Dia Inicio,Mes Fin,Dia Fin,Fuente DOF
                1,5,TEMPORAL FIJA,5,1,8,31,DOF-2024-001
                2,7,TEMPORAL VARIABLE,3,15,5,30,DOF-2024-002
                """;

            // Act
            List<PeriodoVedaCsvRow> result = parser.parsePeriodoVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear 2 periodos veda");

            // Primera veda
            PeriodoVedaCsvRow meroVeda = result.get(0);
            assertEquals(1L, meroVeda.id());
            assertEquals(5L, meroVeda.regulacionId());
            assertEquals("TEMPORAL FIJA", meroVeda.tipoVeda());
            assertEquals(5, meroVeda.mesInicio());
            assertEquals(1, meroVeda.diaInicio());
            assertEquals(8, meroVeda.mesFin());
            assertEquals(31, meroVeda.diaFin());
            assertEquals("DOF-2024-001", meroVeda.fuenteDof());
        }

        @Test
        @DisplayName("Debe manejar vedas PERMANENTES con fechas null")
        void testParsePeriodoVedas_VedaPermanente() {
            // Arrange
            String csv = """
                ID,Regulacion ID,Tipo Veda,Mes Inicio,Dia Inicio,Mes Fin,Dia Fin,Fuente DOF
                1,5,PERMANENTE,,,,,,DOF-2024-003
                """;

            // Act
            List<PeriodoVedaCsvRow> result = parser.parsePeriodoVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            PeriodoVedaCsvRow veda = result.get(0);
            assertEquals("PERMANENTE", veda.tipoVeda());
            assertNull(veda.mesInicio(), "Mes inicio debe ser null en veda PERMANENTE");
            assertNull(veda.diaInicio(), "Dia inicio debe ser null en veda PERMANENTE");
            assertNull(veda.mesFin(), "Mes fin debe ser null en veda PERMANENTE");
            assertNull(veda.diaFin(), "Dia fin debe ser null en veda PERMANENTE");
        }

        @Test
        @DisplayName("Debe saltar filas sin ID valido")
        void testParsePeriodoVedas_SinIdValido() {
            // Arrange
            String csv = """
                ID,Regulacion ID,Tipo Veda,Mes Inicio,Dia Inicio,Mes Fin,Dia Fin,Fuente DOF
                1,5,TEMPORAL FIJA,5,1,8,31,DOF-2024-001
                ,7,TEMPORAL FIJA,3,1,5,31,DOF-2024-002
                INVALID,9,TEMPORAL FIJA,4,1,6,30,DOF-2024-003
                2,10,TEMPORAL FIJA,2,1,4,30,DOF-2024-004
                """;

            // Act
            List<PeriodoVedaCsvRow> result = parser.parsePeriodoVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear solo filas con ID valido");
            assertEquals(1L, result.get(0).id());
            assertEquals(2L, result.get(1).id());
        }

        @Test
        @DisplayName("Debe manejar valores no numericos en campos de fecha")
        void testParsePeriodoVedas_FechasInvalidas() {
            // Arrange
            String csv = """
                ID,Regulacion ID,Tipo Veda,Mes Inicio,Dia Inicio,Mes Fin,Dia Fin,Fuente DOF
                1,5,TEMPORAL FIJA,N/A,TBD,8,31,DOF-2024-001
                """;

            // Act
            List<PeriodoVedaCsvRow> result = parser.parsePeriodoVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            PeriodoVedaCsvRow veda = result.get(0);
            assertNull(veda.mesInicio(), "Valor 'N/A' debe convertirse a null");
            assertNull(veda.diaInicio(), "Valor 'TBD' debe convertirse a null");
            assertEquals(8, veda.mesFin());
            assertEquals(31, veda.diaFin());
        }

        @Test
        @DisplayName("Debe saltar filas incompletas sin romper el proceso")
        void testParsePeriodoVedas_FilaIncompleta() {
            // Arrange
            String csv = """
                ID,Regulacion ID,Tipo Veda,Mes Inicio,Dia Inicio,Mes Fin,Dia Fin,Fuente DOF
                1,5,TEMPORAL FIJA,5,1,8,31,DOF-2024-001
                2,7,TEMPORAL
                3,9,TEMPORAL FIJA,3,1,5,31,DOF-2024-002
                """;

            // Act
            List<PeriodoVedaCsvRow> result = parser.parsePeriodoVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe parsear solo las filas completas");
            assertEquals(1L, result.get(0).id());
            assertEquals(3L, result.get(1).id());
        }

        @Test
        @DisplayName("Debe manejar comillas escapadas en fuente DOF")
        void testParsePeriodoVedas_ComillasEscapadas() {
            // Arrange
            String csv = """
                ID,Regulacion ID,Tipo Veda,Mes Inicio,Dia Inicio,Mes Fin,Dia Fin,Fuente DOF
                1,5,TEMPORAL FIJA,5,1,8,31,DOF 2024 ""Actualizado""
                """;

            // Act
            List<PeriodoVedaCsvRow> result = parser.parsePeriodoVedas(csv.getBytes(StandardCharsets.UTF_8));

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
            List<PeriodoVedaCsvRow> result = parser.parsePeriodoVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertTrue(result.isEmpty(), "CSV vacío debe retornar lista vacía");
        }

        @Test
        @DisplayName("Debe manejar lineas con espacios en blanco")
        void testParse_LineasConEspacios() {
            // Arrange
            String csv = """
                ID,Regulacion ID,Tipo Veda,Mes Inicio,Dia Inicio,Mes Fin,Dia Fin,Fuente DOF
                1,  5  ,  TEMPORAL FIJA  ,  5  ,  1  ,  8  ,  31  ,  DOF-2024-001
                """;

            // Act
            List<PeriodoVedaCsvRow> result = parser.parsePeriodoVedas(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(1, result.size());
            assertEquals(5L, result.get(0).regulacionId());
        }

        @Test
        @DisplayName("Debe manejar diferentes tipos de saltos de línea")
        void testParse_SaltosDeLinea() {
            // Arrange - Mezcla de \n y \r\n
            String csv = "ID,NOMBRE COMÚN,NOMBRE CIENTÍFICO,NOMBRE MAYA,DESCRIPCIÓN,RIESGO CIGUATERA,ES INVASIVA,ES PROTEGIDA,TIPO AGUA,MIGRATORIO\r\n" +
                    "5,Mero,Epinephelus morio,Xlavita,Pez importante,No,FALSE,FALSE,Salada,TRUE\n" +
                    "7,Huachinango,Lutjanus campechanus,K'aan xook,Pez comercial,No,FALSE,FALSE,Salada,FALSE\r\n";

            // Act
            List<EspecieCsvRow> result = parser.parsePeces(csv.getBytes(StandardCharsets.UTF_8));

            // Assert
            assertEquals(2, result.size(), "Debe manejar ambos tipos de salto de línea");
        }
    }
}
