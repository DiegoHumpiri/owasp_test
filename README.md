# Laboratorio OWASP A03:2025 — Software Supply Chain Failures

## 1. Introducción

Este laboratorio tiene como objetivo estudiar los **fallos en la cadena de suministro de software**, correspondientes a la categoría **A03:2025 — Software Supply Chain Failures** del OWASP Top 10.

Se utiliza como caso de estudio la vulnerabilidad **Log4Shell (CVE-2021-44228)**, divulgada en diciembre de 2021, que afectó a determinadas versiones de Apache Log4j Core.

El laboratorio permite identificar componentes vulnerables, analizar su presencia en una aplicación Spring Boot, realizar pruebas dinámicas controladas, comparar versiones y utilizar herramientas de supervisión de dependencias.

Para el análisis se emplean dos herramientas complementarias:

- **CycloneDX:** genera un Software Bill of Materials (SBOM).
- **GitHub Dependabot:** identifica dependencias vulnerables y propone actualizaciones mediante Pull Requests.

## 2. Objetivos

### 2.1. Objetivo general

Analizar los riesgos de seguridad relacionados con dependencias de software mediante el estudio de Log4Shell, utilizando Java, Spring Boot, Gradle, Docker, CycloneDX y GitHub Dependabot.

### 2.2. Objetivos específicos

1. Implementar una aplicación REST utilizando Spring Boot.
2. Ejecutar la aplicación dentro de un contenedor Docker con restricciones de seguridad.
3. Identificar la versión históricamente vulnerable de Apache Log4j Core.
4. Analizar las dependencias directas y transitivas del proyecto.
5. Verificar el comportamiento de expresiones Lookup en Log4j 2.14.1.
6. Comparar dicho comportamiento con Log4j 2.25.5.
7. Generar un inventario SBOM mediante CycloneDX.
8. Configurar GitHub Dependabot para detectar vulnerabilidades y proponer actualizaciones.
9. Documentar los resultados y las evidencias obtenidas.

## 3. Tecnologías utilizadas

| Tecnología | Versión o configuración |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.2 |
| Gradle Wrapper | 9.9.1 |
| Gradle DSL | Kotlin |
| Docker | Docker Engine y Compose |
| Servidor web | Apache Tomcat 11.0.27 |
| Apache Log4j vulnerable | 2.14.1 |
| Apache Log4j de comparación | 2.25.5 |
| CycloneDX Gradle Plugin | 3.6.1 |
| Estándar SBOM | CycloneDX 1.6 |
| GitHub Dependabot | Integrado |
| GitHub Actions | Gradle Dependency Submission |
| Sistema operativo | Ubuntu |

**Nota:** OWASP Dependency-Check fue utilizado durante las pruebas iniciales, pero se decidió sustituirlo por GitHub Dependabot como herramienta de supervisión de dependencias del laboratorio.

## 4. Estructura del repositorio

Estructura relevante:

```text
lab/
├── .git/
├── .github/
│   ├── dependabot.yml
│   └── workflows/
│       └── dependency-submission.yml
├── log4shell/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── gradle/
│   │   └── wrapper/
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── .dockerignore
│   ├── .gitignore
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/edu/lab/a03/
│   │   │   │   ├── A03Log4shellLabApplication.java
│   │   │   │   └── StatusController.java
│   │   │   └── resources/
│   │   └── test/
│   ├── lab-lookup/
│   │   ├── LookupBehavior.java
│   │   ├── log4j-api-2.14.1.jar
│   │   ├── log4j-core-2.14.1.jar
│   │   ├── log4j-api-2.25.5.jar
│   │   └── log4j-core-2.25.5.jar
│   ├── evidence/
│   │   └── dependabot/
│   │       ├── 01-gradle-dependency-submission-workflows.png
│   │       ├── 02-dependabot-alert-log4j.png
│   │       ├── 03-github-dependency-graph.png
│   │       └── 04-dependabot-pull-requests.png
│   ├── build/
│   │   └── reports/
│   └── README.md
└── README.md
```

Las capturas del directorio `evidence/dependabot/` corresponden a las cuatro imágenes proporcionadas para documentar GitHub Dependabot.

## 5. Etapa 1 — Implementación de Spring Boot


Se generó una aplicación Java con Spring Initializr, utilizando Spring Boot 4.1.2 y Gradle Kotlin DSL.

Se implementó el siguiente endpoint REST:

```http
GET /api/status
```

Respuesta:

```json
{
  "application": "OWASP A03 Laboratory",
  "status": "OK"
}
```

### 5.1. Compilación

```bash
cd ~/DiegoHumpiri/owasp/lab/log4shell

./gradlew clean test
```

Resultado obtenido:

```text
BUILD SUCCESSFUL in 20s
5 actionable tasks: 5 executed
```

### 5.2. Ejecución

```bash
./gradlew bootRun --args='--server.address=127.0.0.1'
```

Prueba HTTP:

```bash
curl -i http://127.0.0.1:8080/api/status
```

Resultado:

```http
HTTP/1.1 200
Content-Type: application/json
```

Se confirmó el funcionamiento de la aplicación.

## 6. Etapa 2 — Contenerización con Docker

Se implementó un Dockerfile multietapa utilizando:

- `eclipse-temurin:21-jdk-noble` para la compilación.
- `eclipse-temurin:21-jre-noble` para la ejecución.

### 6.1. Construcción y ejecución

```bash
docker compose config
docker compose build
docker compose up -d
docker compose ps
```

Resultado:

```text
Image owasp-a03-springboot:local Built
Container owasp-a03-lab-app-1 Started
```

Se verificó que Spring Boot y Tomcat iniciaran correctamente dentro del contenedor.

### 6.2. Restricciones de seguridad

| Configuración | Valor verificado |
|---|---|
| Usuario | UID 10001 |
| Grupo | GID 10001 |
| Red | none |
| Sistema de archivos | Solo lectura |
| Modo privilegiado | false |
| CPU | 1 |
| Memoria | 768 MiB |
| Procesos máximos | 128 |
| Capacidades | Eliminadas |
| Elevación de privilegios | Deshabilitada |
| Directorio `/tmp` | tmpfs de 64 MiB |

Se comprobó el funcionamiento del endpoint desde el interior del contenedor, obteniendo HTTP 200.

Las restricciones reducen los riesgos del laboratorio, aunque Docker no garantiza aislamiento absoluto del sistema anfitrión.

## 7. Etapa 3 — Identificación de Log4j vulnerable

Se utilizó Apache Log4j Core 2.14.1, versión afectada por CVE-2021-44228.

Durante la primera configuración se declaró:

```kotlin
compileOnly("org.apache.logging.log4j:log4j-core:2.14.1")
```

Para comprobar la dependencia se ejecutó:

```bash
./gradlew dependencyInsight \
  --dependency log4j-core \
  --configuration compileClasspath
```

Resultado obtenido:

```text
org.apache.logging.log4j:log4j-core:2.14.1
```

### 7.1. Inspección del componente

Se localizó el archivo histórico:

```text
log4j-core-2.14.1.jar
```

Se comprobó la presencia de:

```text
org/apache/logging/log4j/core/lookup/JndiLookup.class
```

SHA-256 registrado:

```text
ade7402a70667a727635d5c4c29495f4ff96f061f12539763f6f123973b465b0
```

La presencia de `JndiLookup.class` permite identificar una funcionalidad asociada a la vulnerabilidad histórica, pero no demuestra por sí sola explotación.

## 8. Etapa 4 — Análisis de exposición

Se inspeccionaron las dependencias de compilación y ejecución de Spring Boot.

### 8.1. Dependencias de compilación

Se identificó inicialmente:

```text
org.apache.logging.log4j:log4j-core:2.14.1
```

### 8.2. Dependencias de ejecución

Se ejecutó:

```bash
./gradlew dependencyInsight \
  --dependency log4j-core \
  --configuration runtimeClasspath
```

Resultado:

```text
No dependencies matching given input were found
```

También se identificó:

```text
ch.qos.logback:logback-classic:1.5.38
```

### 8.3. Inspección del JAR ejecutable

Resultado:

```text
BOOT-INF/lib/logback-classic-1.5.38.jar
BOOT-INF/lib/log4j-api-2.25.5.jar
```

**Conclusión:** durante esta prueba, la aplicación Spring Boot utilizaba Logback y no empaquetaba Log4j Core vulnerable.

La identificación de una versión vulnerable en `compileClasspath` no implica automáticamente que la aplicación sea explotable.

## 9. Etapa 5 — Pruebas dinámicas controladas


Se creó una clase Java independiente:

```text
lab-lookup/LookupBehavior.java
```

La prueba permitió estudiar el comportamiento de expresiones Lookup utilizando versiones diferentes de Apache Log4j.

### 9.1. Prueba con Log4j 2.14.1

Se utilizó:

```java
logger.error("PRUEBA_LOOKUP: ${java:version}");
```

Salida obtenida:

```text
Inicio de la prueba controlada
PRUEBA_LOOKUP: Java version 21.0.13
Fin de la prueba controlada
```

La expresión fue interpretada por Log4j.

También se comprobó:

```java
logger.error("PRUEBA_LOOKUP: ${env:HOME}");
```

Salida:

```text
PRUEBA_LOOKUP: /
```

Esto demostró que la versión histórica procesaba expresiones Lookup incorporadas en los mensajes registrados.

### 9.2. Prueba con Log4j 2.25.5

Se ejecutó el mismo programa utilizando las bibliotecas de Log4j 2.25.5.

Resultado:

```text
Inicio de la prueba controlada
2026-11-19T20:33:10.520035538Z main ERROR PRUEBA_LOOKUP: ${java:version}
Fin de la prueba controlada
```

La expresión permaneció como texto literal.

### 9.3. Comparación

| Característica | Log4j 2.14.1 | Log4j 2.25.5 |
|---|---|---|
| Ejecución del programa | Correcta | Correcta |
| `${java:version}` | Interpretada | Literal |
| Procesamiento automático de Lookup | Observado | No observado |
| Ejecución remota de código demostrada | No | No |

**Conclusión:** se demostró un cambio real de comportamiento entre ambas versiones.

Esta prueba no constituye explotación JNDI ni ejecución remota de código mediante Log4Shell.

## 10. Etapa 6 — Inventario SBOM con CycloneDX

Se integró el plugin:

```kotlin
id("org.cyclonedx.bom") version "3.6.1"
```

Se configuró el inventario para incluir las dependencias de compilación y ejecución.

### 10.1. Generación del SBOM

Comando:

```bash
./gradlew cyclonedxBom
```

Resultado:

```text
BUILD SUCCESSFUL in 29s
2 actionable tasks: 2 executed
```

Archivos generados:

```text
build/reports/cyclonedx/application.cdx.json
build/reports/cyclonedx-direct/bom.json
build/reports/cyclonedx-direct/bom.xml
```

### 10.2. Validación

Se validó el archivo JSON directo mediante Python.

Resultado:

```text
Formato: CycloneDX
Versión: 1.6
Componentes: 47
```

### 10.3. Componentes Log4j identificados

En el momento de generar el SBOM, el proyecto utilizaba la dependencia corregida.

| Componente | Versión registrada |
|---|---|
| log4j-api | 2.25.5 |
| log4j-core | 2.25.5 |
| log4j-to-slf4j | 2.25.5 |

**Nota:** posteriormente se volvió a declarar Log4j Core 2.14.1 para el escenario de detección con Dependabot. Por tanto, los resultados del SBOM anterior corresponden al estado corregido, no necesariamente al estado actual del proyecto.

El inventario debe regenerarse cuando se necesite reflejar la nueva configuración vulnerable.

## 11. Etapa 7 — GitHub Dependabot

Se decidió utilizar GitHub Dependabot como herramienta de detección y seguimiento de dependencias vulnerables, reemplazando OWASP Dependency-Check.

La integración comprende:

1. Configuración de Dependabot.
2. GitHub Actions para enviar el grafo de dependencias.
3. Detección de vulnerabilidades mediante Dependabot Alerts.
4. Generación de Pull Requests de actualización.

### 11.1. Configuración del repositorio

El proyecto Gradle se encuentra en:

```text
/log4shell
```

El archivo de configuración está ubicado en:

```text
.github/dependabot.yml
```

Configuración propuesta y utilizada como base:

```yaml
version: 2

updates:
  - package-ecosystem: "gradle"
    directory: "/log4shell"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 5

  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 5
```

### 11.2. GitHub Actions — Dependency Submission

Se incorporó el workflow:

```text
.github/workflows/dependency-submission.yml
```

Su finalidad es enviar las dependencias resueltas por Gradle al grafo de dependencias de GitHub.

**Resultados observados:**

Se registraron cuatro ejecuciones exitosas del workflow `Gradle Dependency Submission`.

Entre ellas:

- Ejecución inicial en `main`.
- Ejecución de actualización de Log4j Core.
- Ejecución de actualización de `actions/setup-java`.
- Ejecución de actualización de `actions/checkout`.

![Evidencia 1: Ejecuciones satisfactorias de Gradle Dependency Submission](evidence/dependabot/01-gradle-dependency-submission-workflows.png)

**Figura 1.** Ejecuciones registradas por GitHub Actions, incluidas las activadas desde ramas de Dependabot.

### 11.3. GitHub Dependency Graph

Se verificó que GitHub había procesado el grafo de dependencias del repositorio.

**Resultado observado:**

```text
237 dependencias registradas
```

Entre los componentes identificados aparecen:

| Componente | Versión observada | Tipo |
|---|---|---|
| log4j-core | 2.14.1 | Directa |
| guava | 33.7.1-jre | Transitiva |
| commons-lang3 | 3.16.0 | Transitiva |

La interfaz de GitHub mostró alertas asociadas a componentes directos y transitivos.

![Evidencia 2: Grafo de dependencias identificado por GitHub](evidence/dependabot/03-github-dependency-graph.png)

**Figura 2.** GitHub Dependency Graph con 237 dependencias y detección de Log4j Core 2.14.1.

La diferencia entre los 47 componentes del SBOM directo anterior y los 237 del grafo de GitHub puede relacionarse con el alcance, las configuraciones y las dependencias consideradas por cada mecanismo.

No deben interpretarse como inventarios idénticos.

### 11.4. Detección de vulnerabilidades en Log4j

GitHub Dependabot identificó una alerta de seguridad relacionada con:

```text
org.apache.logging.log4j:log4j-core:2.14.1
```

**Información observada en la alerta:**

| Campo | Resultado |
|---|---|
| Componente | Apache Log4j Core |
| Versión detectada | 2.14.1 |
| Estado | Open |
| Severidad | Critical |
| Puntuación CVSS 3 | 9.0 |
| Rango afectado mostrado | >= 2.13.0, < 2.16.0 |
| Versión corregida para ese aviso | 2.16.0 |
| Versión propuesta por Dependabot | 2.26.1 |

La alerta indica que la corrección es incompleta para el aviso detectado.

Además, GitHub relacionó la propuesta de actualización con la corrección de ocho alertas de Dependabot sobre Log4j Core.

![Evidencia 3: Alerta crítica de seguridad en Apache Log4j](evidence/dependabot/02-dependabot-alert-log4j.png)

**Figura 3.** Alerta crítica detectada por GitHub Dependabot sobre Apache Log4j Core 2.14.1.

La alerta corresponde al estado vulnerable registrado en el repositorio. No demuestra por sí sola que el componente pueda explotarse en la aplicación Spring Boot durante su ejecución.

### 11.5. Pull Requests generados por Dependabot

GitHub Dependabot creó tres propuestas de actualización.

| PR | Componente | Actualización | Estado observado |
|---|---|---|---|
| #1 | actions/checkout | 6 → 7 | Open |
| #2 | actions/setup-java | 5 → 6 | Open |
| #3 | log4j-core | 2.14.1 → 2.26.1 | Open |

Las tres propuestas mostraban sus comprobaciones ejecutadas satisfactoriamente en las capturas.

![Evidencia 4: Pull Requests generados automáticamente por Dependabot](evidence/dependabot/04-dependabot-pull-requests.png)

**Figura 4.** Pull Requests de actualización de bibliotecas Java y acciones de GitHub.

Estos resultados demuestran que Dependabot no solamente identifica versiones disponibles, sino que también propone modificaciones del proyecto para actualizar sus dependencias.

**Importante:** que una Pull Request tenga comprobaciones satisfactorias no significa que ya haya sido integrada.

## 12. Etapa 8 — Análisis de mitigación

Durante las pruebas se actualizó temporalmente la dependencia de Log4j Core:

```kotlin
compileOnly("org.apache.logging.log4j:log4j-core:2.25.5")
```

Gradle confirmó:

```text
org.apache.logging.log4j:log4j-core:2.25.5
```

Resultado:

```text
BUILD SUCCESSFUL
```

También se verificó la diferencia de comportamiento entre las versiones 2.14.1 y 2.25.5.

Posteriormente, para documentar la detección automatizada de vulnerabilidades mediante Dependabot, se volvió a utilizar la versión histórica:

```text
log4j-core:2.14.1
```

**Estado actual declarado:** el laboratorio utiliza nuevamente Log4j Core 2.14.1 para la demostración de Dependabot.

La Pull Request #3 propone actualizarla a Log4j Core 2.26.1.

La mitigación final del repositorio aún no está verificada. Para completarla se deberá revisar la propuesta, ejecutar las pruebas, incorporar la corrección y comprobar que las alertas correspondientes quedan resueltas.

## 13. Problemas encontrados y soluciones

| Problema | Solución o resultado |
|---|---|
| Docker no encontró inicialmente el Dockerfile | Se corrigió la ubicación y la construcción terminó correctamente |
| Log4j vulnerable no estaba en ejecución | Se identificó su alcance y se preparó una prueba Java independiente |
| Falló la prueba inicial con Log4j 2.25.5 | Se descargaron los JAR correspondientes y se repitió la prueba |
| Se buscó el SBOM en una ruta incorrecta | Se localizó en `build/reports/cyclonedx-direct/` |
| Dependency-Check falló durante la actualización de NVD | Se revisó la configuración de acceso a NVD |
| Dependency-Check agotó el heap de Gradle | Se aumentó el heap máximo a 2 GiB |
| Se decidió cambiar de herramienta de supervisión | Se adoptó GitHub Dependabot |
| Se necesitaba identificar dependencias transitivas en GitHub | Se configuró Gradle Dependency Submission |
| GitHub detectó Log4j 2.14.1 | Dependabot generó una alerta crítica y una propuesta de actualización |

## 14. Resultados generales

| Actividad | Resultado |
|---|---|
| Aplicación Spring Boot | Verificada |
| Endpoint REST | HTTP 200 |
| Docker | Construcción y ejecución correctas |
| Restricciones de aislamiento | Verificadas |
| Identificación de Log4j 2.14.1 | Completada |
| Inspección de JndiLookup | Completada |
| Prueba dinámica con Log4j 2.14.1 | Completada |
| Comparación con Log4j 2.25.5 | Completada |
| SBOM CycloneDX | 47 componentes en el inventario generado |
| GitHub Dependency Graph | 237 dependencias observadas |
| GitHub Actions | 4 ejecuciones satisfactorias |
| Dependabot Alerts | Alerta crítica de Log4j identificada |
| Dependabot Pull Requests | 3 propuestas de actualización |
| Corrección final del repositorio | Pendiente |

## 15. Conclusiones

1. La dependencia de bibliotecas externas puede introducir riesgos de seguridad que no se originan directamente en el código de la aplicación.

2. La presencia de una versión vulnerable en un proyecto no implica necesariamente que esté cargada durante la ejecución. Es importante distinguir dependencias de compilación, dependencias de ejecución y artefactos empaquetados.

3. Las pruebas dinámicas demostraron que Log4j 2.14.1 procesaba automáticamente determinadas expresiones Lookup, mientras que Log4j 2.25.5 conservó la expresión probada como texto literal.

4. CycloneDX permitió generar un inventario de los componentes utilizados por el proyecto, facilitando su identificación y seguimiento.

5. GitHub Dependabot detectó la versión histórica vulnerable de Log4j Core, mostró una alerta crítica y generó una propuesta de actualización.

6. GitHub Actions permitió registrar dependencias directas y transitivas mediante el envío del grafo Gradle a GitHub.

7. La seguridad de la cadena de suministro requiere identificar los componentes, supervisar las vulnerabilidades, revisar las actualizaciones y verificar las correcciones.

## 16. Referencias

- [OWASP Top 10:2025 — A03 Software Supply Chain Failures](https://top10.owasp.org/2025/es/A03_2025-Software_Supply_Chain_Failures/)
- [Apache Log4j Security](https://logging.apache.org/security.html)
- [NIST — CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/reference/)
- [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)
- [Docker Documentation](https://docs.docker.com/)
- [CycloneDX](https://cyclonedx.org/)
- [GitHub Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [GitHub Dependency Graph Documentation](https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/about-the-dependency-graph)
- [Gradle GitHub Actions](https://github.com/gradle/actions)

---

**Estado del laboratorio:** integración de Dependabot verificada mediante capturas. La aplicación Spring Boot, Docker, las pruebas dinámicas y la generación del SBOM cuentan con resultados documentados. Queda pendiente la aplicación y verificación de la mitigación final en el repositorio GitHub.