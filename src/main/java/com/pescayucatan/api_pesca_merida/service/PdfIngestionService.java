package com.pescayucatan.api_pesca_merida.service;

import com.pescayucatan.api_pesca_merida.enums.EstadoIngestion;
import com.pescayucatan.api_pesca_merida.enums.TipoVeda;
import com.pescayucatan.api_pesca_merida.enums.ZonaPesca;
import com.pescayucatan.api_pesca_merida.model.EspecieVeda;
import com.pescayucatan.api_pesca_merida.model.IngestionLog;
import com.pescayucatan.api_pesca_merida.model.Pez;
import com.pescayucatan.api_pesca_merida.repository.EspecieVedaRepository;
import com.pescayucatan.api_pesca_merida.repository.PezRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PdfIngestionService {
/*
    private final WebClient pythonClient;
    private final EspecieVedaRepository vedaRepo;
    private final IngestionLogRepository logRepo;
    private final PezRepository pezRepo;

    // WebClient apunta al µService Python en localhost:8090
    public PdfIngestionService(
            @Value("${python.microservice.url:http://localhost:8090}") String url,
            EspecieVedaRepository vedaRepo,
            IngestionLogRepository logRepo,
            PezRepository pezRepo
    ) {
        this.pythonClient = WebClient.builder().baseUrl(url).build();
        this.vedaRepo = vedaRepo;
        this.logRepo = logRepo;
        this.pezRepo = pezRepo;
    }

    public IngestionLog ingestar(MultipartFile pdfFile) throws IOException {
        String hash = calcularSha256(pdfFile.getBytes());

        // Idempotencia: no re-ingestar el mismo PDF
        if (logRepo.existsByHashSha256(hash)) {
            throw new IllegalStateException("Este PDF ya fue procesado (hash: " + hash + ")");
        }

        IngestionLog log = logRepo.save(nuevoLog(pdfFile.getOriginalFilename(), hash));

        // Llamada al µService Python
        List<Map<String, String>> filas = pythonClient.post()
                .uri("/extract")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(buildBody(pdfFile))
                .retrieve()
                .bodyToMono(ExtractionResponse.class)
                .block()
                .getRows();

        int exitosas = 0, errores = 0;
        for (Map<String, String> fila : filas) {
            try {
                EspecieVeda veda = parsearFila(fila, pdfFile.getOriginalFilename());
                vedaRepo.save(veda);
                exitosas++;
            } catch (Exception e) {
                errores++;
                // Loguear fila problemática sin detener la ingesta
            }
        }

        log.setFilasExitosas(exitosas);
        log.setFilasError(errores);
        log.setEstado(errores == 0 ? EstadoIngestion.COMPLETADO : EstadoIngestion.ERROR);
        return logRepo.save(log);
    }

    private EspecieVeda parsearFila(Map<String, String> fila, String fuente) {
        EspecieVeda veda = new EspecieVeda();

        // Buscar o crear el Pez asociado
        String nombreEspecie = fila.get("especie").trim();
        Pez pez = pezRepo.findByNombreIgnoreCase(nombreEspecie)
                .orElseGet(() -> {
                    Pez nuevo = new Pez();
                    nuevo.setNombre(nombreEspecie);
                    return pezRepo.save(nuevo);
                });
        veda.setPez(pez);
        veda.setZona(ZonaPesca.forValue(fila.get("zona")));
        veda.setFuentePdf(fuente);

        String inicio = fila.get("inicio_veda");
        String fin    = fila.get("fin_veda");

        // Detectar tipo de veda por formato de fecha
        if (inicio.matches("\\d{4}-\\d{2}-\\d{2}")) {
            // FIJA: formato YYYY-MM-DD
            veda.setTipoVeda(TipoVeda.FIJA);
            veda.setInicioFijo(LocalDate.parse(inicio));
            veda.setFinFijo(LocalDate.parse(fin));
        } else if (inicio.matches("\\d{2}-\\d{2}")) {
            // CÍCLICA: formato MM-DD
            veda.setTipoVeda(TipoVeda.CICLICA);
            String[] partsI = inicio.split("-");
            String[] partsF = fin.split("-");
            veda.setInicioMes(Integer.parseInt(partsI[0]));
            veda.setInicioDia(Integer.parseInt(partsI[1]));
            veda.setFinMes(Integer.parseInt(partsF[0]));
            veda.setFinDia(Integer.parseInt(partsF[1]));
        }
        // Agregar lógica para PLURIANUAL según formato del PDF real

        return veda;
    }

    private String calcularSha256(byte[] bytes) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    */
}