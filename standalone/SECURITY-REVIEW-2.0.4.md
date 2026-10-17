# Revisión de la detección de Defender en 2.0.4

Estado: **detección pendiente de resolver; instalación desaconsejada**.

El 17/10/2026 se recibió una captura de Microsoft Defender que identifica
`Trojan:Script/Ulthar.A!ml` en `Armas-Legendarias-2.0.4.zip`, descargado desde
el enlace publicado de esta rama. El estado mostrado es **Quitado**. La captura
identifica el ZIP; no identifica un archivo interior concreto.

La recomendación de descarga se ha suspendido en el README. El artefacto original
se conserva para permitir el análisis de sus mismos bytes. No se ha publicado
otra distribución para sustituirlo ni se ha determinado que sea un falso positivo.

## Identificación del artefacto

- Commit de publicación: `28e31ef45529e14ee11985ab91db344c897e895d`.
- ZIP SHA-256: `fa789ababf4a84d0cbcdc5834e1216f0c37fa684956cf9b95d6810e360f62734`.
- JAR SHA-256: `1323fea72cd5ba99a3e8e231da40287ff5ed24c7882b2e4e7a3f82c1c5836f04`.

## Comprobaciones de integridad y revisión estática

La copia obtenida del enlace publicado coincide byte a byte con el ZIP revisado.
Sus ocho entradas son el JAR del plugin, el pack Java, `iniciar.bat`, `iniciar.sh`,
`server.properties`, `LEEME.txt` y dos PNG. Los archivos comprimidos superan sus
comprobaciones de integridad. Las rutas de las entradas no escapan del directorio
de extracción y no contienen enlaces simbólicos.

Los scripts de inicio comprueban si existe `paper.jar` y ejecutan Java para
iniciarlo. El JAR contiene 75 clases bajo `dev/linqfy/`, cada una con un archivo
fuente correspondiente y bytes coincidentes con la salida de compilación local.
No incluye ejecutables o bibliotecas nativas adicionales. La revisión de
constantes del bytecode no encontró referencias a creación de procesos, comandos
de shell ni carga de código remoto. La URL presente es la del servidor local del
pack. El pack anidado solo contiene JSON, PNG y MCMETA.

Estas comprobaciones de integridad y código **no descartan malware ni resuelven
la alerta de Defender**. Las pruebas de funcionamiento de Minecraft tampoco
equivalen a un análisis antivirus.

## Análisis antivirus

El 17/10/2026 a las 20:40 (Argentina), **ClamAV 1.4.6** examinó el ZIP original
y sus ocho archivos extraídos, con análisis recursivo de archivos comprimidos:
**9 archivos examinados, 0 infectados, salida 0**. No hubo errores ni avisos
de límites de análisis en el registro. Se usaron las bases oficiales descargadas
mediante FreshClam: `daily` 28156 (17/10/2026, 06:25 UTC), `main` 63 y
`bytecode` 339. La descarga y verificación de las tres bases terminó correctamente.

El análisis se realizó sobre los mismos hashes identificados arriba. No se
ejecutaron los archivos del paquete durante esta revisión. El registro y los
metadatos están en [antivirus-review-2.0.4.json](docs/antivirus-review-2.0.4.json).
Un resultado negativo en este motor **no invalida la detección de Microsoft
Defender**. No se dispone de Microsoft Defender en este entorno y no se ha
reproducido su resultado ni obtenido un dictamen de Microsoft sobre este archivo.

## Pendiente

Identificar el archivo o patrón exacto que activa Defender y contrastar el caso
con Microsoft. La [descripción oficial de la amenaza](https://www.microsoft.com/en-us/wdsi/threats/malware-encyclopedia-description?Name=Trojan%3AScript%2FUlthar.A!ml)
ofrece información genérica y no publica detalles técnicos que permitan atribuir
el comportamiento de este ZIP a partir del nombre de la detección.

## Comprobación preparada en Windows

El workflow [LegendaryWeapons - Windows Defender verification](../.github/workflows/legendary-defender.yml)
compila y prueba únicamente `standalone/` en un runner Windows con Java 25.
El script [Test-LegendaryDefender.ps1](tools/Test-LegendaryDefender.ps1) actualiza
las firmas y examina el ZIP original, los miembros disponibles y el JAR nuevo.
Registra hashes, versiones del motor, resultados y errores sin ejecutar las muestras.
Mantiene las exclusiones y la protección en tiempo real del runner intactas.

El JAR solo se sube como artefacto si su análisis personalizado termina con
resultado limpio y el archivo conserva su hash. Si Defender está inactivo,
no puede actualizarse, detecta algo o el análisis falla, no se publica un JAR
verificado. El informe de diagnóstico se conserva también cuando falla la prueba.
Los resultados del ZIP original se documentan por separado de los del JAR nuevo.

Los archivos ZIP/JAR compilados usan entradas ordenadas y fechas fijas para
permitir comparar compilaciones con las mismas fuentes y JDK. Esto no elimina
la detección de Defender ni constituye por sí solo una solución de seguridad.

Validación local de esta preparación: dos compilaciones completas con Java 25
y 66 pruebas correctas produjeron hashes idénticos de JAR y pack. Las clases y
recursos del JAR conservan los bytes de la versión publicada; el contenido de
los modelos también coincide. ClamAV 1.4.6 no detectó amenazas en el JAR nuevo.
`actionlint` valida el workflow, PowerShell analiza el script sin errores y las
comprobaciones locales verifican que ausencia de Defender, archivo ausente y
salidas nativas distintas de cero impiden marcar un archivo como validado.
Esas comprobaciones locales no sustituyen la ejecución pendiente en Windows.

Estado de ejecución: **pendiente**. Este repositorio es un fork y la conexión
actual no tiene acceso a la configuración de Actions (HTTP 403). Si Actions está
deshabilitado, el propietario debe abrir la [pestaña Actions del repositorio](https://github.com/benjamingomez-hash/BigC-Paper/actions)
y habilitar los workflows. Después se ejecuta esta comprobación en la rama
`feat/legendary-crafting-pvp`. Solo se recomendará un archivo cuyo resultado
se haya podido verificar; la recomendación del ZIP original sigue suspendida.
