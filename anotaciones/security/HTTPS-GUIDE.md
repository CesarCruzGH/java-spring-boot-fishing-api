# HTTPS Configuration Guide - API Pesca Yucatán

## ¿Por qué HTTPS es OBLIGATORIO en Producción?

### 1. **Protección de Credenciales en Tránsito**

```
┌─────────────────────────────────────────────────────────────────┐
│  SIN HTTPS (HTTP plano):                                        │
│  ────────────────────────                                       │
│  Request: POST /api/v1/ingestion/trigger                        │
│  Header:  Authorization: Basic YWRtaW46cGFzc3dvcmQxMjM=         │
│                                                                 │
│  ❌ Un atacante en la misma red puede interceptar:              │
│     - El header Authorization con usuario:password en Base64   │
│     - El password codificado pero RECODIFICABLE a texto plano  │
│                                                                 │
│  ⭐ BCrypt protege el password ALMACENADO, pero NO el          │
│    password en tránsito durante la petición HTTP                │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  CON HTTPS (TLS/SSL):                                           │
│  ──────────────────────                                        │
│  Request: POST /api/v1/ingestion/trigger                        │
│  Header:  Authorization: Basic YWRtaW46cGFNz3dvcmQxMjM=       │
│                                                                 │
│  ✅ El tráfico está CIFRADO con TLS 1.3                        │
│     - El attacker solo ve datos cifrados ilegibles             │
│     - El header Authorization NO es legible                     │
│     - Previene man-in-the-middle (MITM) attacks                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2. **Integridad de Datos**

- **Sin HTTPS**: Un atacante puede modificar las respuestas del servidor
  - Inyectar código malicioso en respuestas JSON
  - Modificar datos de especies de peces
  - Introducir código en páginas web servidas

- **Con HTTPS**: Los datos están firmados criptográficamente
  - El cliente detecta cualquier modificación de la respuesta
  - Garante de que los datos vienen del servidor real

### 3. **Autenticidad del Servidor**

```
┌─────────────────────────────────────────────────────────────────┐
│  CERTIFICADO SSL = Identidad verificada del servidor            │
│                                                                 │
│  ✅ Con certificado válido:                                     │
│     - El cliente verifica que habla con pesca-merida.com REAL  │
│     - Previene ataques de phishing ( DNS spoofing)              │
│     - El navegador muestra el candado verde 🔒                 │
│                                                                 │
│  ❌ Sin HTTPS:                                                  │
│     - Cualquiera puede hacerse pasar por tu API                 │
│     - Usernames y passwords van a servidores fraudulentos      │
└─────────────────────────────────────────────────────────────────┘
```

### 4. **Cumplimiento de Regulaciones**

| Regulación | Requisito HTTPS |
|------------|------------------|
| GDPR (EU) | Obligatorio para datos personales |
| PCI-DSS | Obligatorio para datos de pago |
| SOC 2 | Obligatorio para datos sensibles |
| Best Practices | OWASP Top 10 requiere HTTPS |

### 5. **Modern Browser Requirements**

- **Chrome/Firefox**: Marcan HTTP como "Not Secure"
- **Service Workers**: Solo funcionan en contextos seguros
- **Geolocation API**: Requiere HTTPS
- **HTTP/2**: Requiere HTTPS en la mayoría de navegadores
- **WebRTC**: Solo en contextos seguros

---

## Arquitectura Recomendada

### Opción A: SSL/TLS Directo en Spring Boot (Recomendado para producción)

```
                    ┌─────────────────────────────────────┐
                    │          INTERNET                   │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │         DNS (pesca-merida.com)      │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │           PUERTO 443                │
                    │    ┌──────────────────────────┐     │
                    │    │   Spring Boot App        │     │
                    │    │   (keystore.p12)         │     │
                    │    │   - SSL/TLS terminate    │     │
                    │    └──────────────────────────┘     │
                    └─────────────────────────────────────┘
```

### Opción B: Reverse Proxy (Nginx/Apache) - MAS ESCALABLE

```
                    ┌─────────────────────────────────────┐
                    │          INTERNET                   │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │     Nginx (Puerto 443)              │
                    │     - SSL/TLS terminate             │
                    │     - Certificados aquí             │
                    │     - Load balancing               │
                    └──────────────┬──────────────────────┘
                                   │
                    ┌──────────────▼──────────────────────┐
                    │     Spring Boot (Puerto 8080)       │
                    │     - HTTP interno                 │
                    │     - Sin gestión SSL               │
                    └─────────────────────────────────────┘
```

**Recomendación**: Usar **Opción B (Reverse Proxy)** por:
- Gestión centralizada de certificados (Let's Encrypt)
- Rendimiento mejorado (Nginx maneja SSL handshake)
- Flexibilidad para añadir CDN, WAF, cache
- Los certificados se renovan automáticamente

---

## Generación de Certificado SSL

### Paso 1: Generar Keystore PKCS12

```bash
# Generar keystore con keytool (incluido en JDK)
keytool -genkeypair \
    -alias api-pesca \
    -keyalg RSA \
    -keysize 2048 \
    -keystore keystore.p12 \
    -storepass ${SSL_KEY_PASSWORD} \
    -validity 3650 \
    -storetype PKCS12 \
    -dname "CN=pesca-merida.com, OU=API, O=Pesca Yucatán, L=Mérida, ST=Yucatán, C=MX"
```

### Paso 2: Generar CSR para Certificate Authority (CA)

```bash
# Crear CSR (Certificate Signing Request)
keytool -certreq \
    -alias api-pesca \
    -file csr.pem \
    -keystore keystore.p12 \
    -storepass ${SSL_KEY_PASSWORD}
```

### Paso 3: Obtener Certificado de CA

**Opción A: Let's Encrypt (Gratuito, recomendado)**
```bash
# Usar certbot para obtener certificado
certbot certonly --manual --preferred-challenges=dns \
    -d pesca-merida.com \
    -d www.pesca-merida.com
```

**Opción B: CA Comercial** (GoDaddy, DigiCert, etc.)
- Enviar CSR.pem al CA
- Recibir certificado firmado

### Paso 4: Importar Certificado al Keystore

```bash
# Importar certificado CA intermedia (si hay)
keytool -import \
    -alias intermediate \
    -file intermediate-ca.crt \
    -keystore keystore.p12 \
    -storepass ${SSL_KEY_PASSWORD}

# Importar certificado del servidor
keytool -import \
    -alias api-pesca \
    -file server.crt \
    -keystore keystore.p12 \
    -storepass ${SSL_KEY_PASSWORD}
```

### Paso 5: Colocar keystore.p12 en recursos

```bash
# Copiar keystore al classpath de la aplicación
cp keystore.p12 src/main/resources/

# Agregar al .gitignore (NUNCA commitear keystores!)
echo "**/keystore.p12" >> .gitignore
```

### Paso 6: Configurar Variable de Entorno

```bash
# En producción (no hardcodear!)
export SSL_KEY_PASSWORD="tu-password-secreto-para-keystore"
```

---

## Configuración de application-prod.properties

```properties
# Puerto 443 (requiere root en Linux)
server.port=443

# Habilitar SSL
server.ssl.enabled=true

# Ubicación del keystore (en classpath)
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEY_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=api-pesca
```

---

## Verificación

### Test Local con HTTPS

```bash
# Arrancar con perfil prod
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Probar endpoint
curl -k https://localhost:443/peces
# -k = aceptar certificado autofirmado (solo para testing)
```

### Verificar Configuración SSL

```bash
# Usar OpenSSL para verificar handshake SSL
openssl s_client -connect pesca-merida.com:443 -showcerts

# Verificar protocolo y cipher
openssl s_client -connect pesca-merida.com:443 -tls1_3
```

---

## Configuración Nginx (Reverse Proxy)

```nginx
server {
    listen 443 ssl http2;
    server_name pesca-merida.com;

    ssl_certificate /etc/letsencrypt/live/pesca-merida.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/pesca-merida.com/privkey.pem;
    
    # SSL hardening
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
    ssl_prefer_server_ciphers off;
    
    # HSTS (HTTP Strict Transport Security)
    add_header Strict-Transport-Security "max-age=63072000" always;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}

# Redirect HTTP → HTTPS
server {
    listen 80;
    server_name pesca-merida.com;
    return 301 https://$server_name$request_uri;
}
```

---

## Resumen de Seguridad HTTPS

| Aspecto | Sin HTTPS | Con HTTPS |
|---------|-----------|-----------|
| Credenciales | Expuestas en texto plano | Cifradas (TLS 1.3) |
| MITM Attack | Vulnerable | Protegido |
| Integridad datos | Modificable | Firmado criptográficamente |
| Phishing | Fácil de falsificar | Autenticado por CA |
| SEO | Penalizado por Google | Beneficio SEO |
| Browser warning | "No es seguro" | Candado verde |
