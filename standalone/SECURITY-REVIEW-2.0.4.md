# Revisión de la detección de Defender en 2.0.4

Estado: **instalación suspendida para ZIP y JAR 2.0.4 mientras se investiga un nuevo aviso durante la implementación**. Conservar los bloqueos locales y los archivos originales de datos del servidor.

## Nuevo aviso durante la implementación

Tras descargar el JAR independiente, el usuario informa que el antivirus volvió
a activarse cuando Codex intentó implementarlo en el servidor. Todavía no se
dispone del nombre de la nueva detección, sus elementos afectados ni la fase
exacta (copia, comando de instalación o arranque de Paper). No se ha confirmado
que el nuevo recurso afectado sea el JAR.

El usuario aclara que este segundo aviso se mostró a Codex. No se ha confirmado
una nueva entrada en Defender del equipo; falta el mensaje exacto de Codex para
distinguir una detección de antivirus de un bloqueo de una herramienta.

Se retira también la recomendación de instalación del JAR. Los análisis de
archivos registrados abajo siguen siendo evidencia de esas comprobaciones;
no reprodujeron el flujo de instalación de Codex ni el arranque de Paper en
Windows. Identificar la entrada más reciente del historial de protección es
necesario antes de atribuir el aviso al plugin, a Paper o a una herramienta.
No se publica otro binario como sustitución mientras falta ese diagnóstico.

El 17/10/2026 se recibió una captura de Microsoft Defender que identifica
`Trojan:Script/Ulthar.A!ml` en `Armas-Legendarias-2.0.4.zip`, descargado desde
el enlace publicado de esta rama. El estado mostrado es **Quitado**. La captura
identifica el ZIP; no identifica un archivo interior concreto.

La recomendación de descarga del ZIP se ha suspendido en el README. El artefacto
original se conserva para permitir el análisis de sus mismos bytes. Se publica
por separado el JAR recompilado y examinado que se identifica abajo; su instalación
también queda suspendida tras el nuevo aviso. No se ha
determinado que la detección original sea un falso positivo.

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
Defender**. En esa revisión local no se disponía de Microsoft Defender. Las
comprobaciones posteriores en Windows se documentan abajo; no se ha obtenido
un dictamen de Microsoft sobre la detección original.

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
Activa la protección en tiempo real en el runner desechable si estaba apagada y
marca las muestras con zona de Internet. Elimina las exclusiones heredadas de
discos, extensiones y procesos del runner y activa el análisis de archivos,
scripts, descargas y comportamiento. Exige que la protección siga activa al
terminar de examinar el JAR. Activa también
la protección en la nube, el envío de muestras seguras del repositorio público y
Block at First Sight; exige conexión correcta a la nube de Defender.

El JAR solo se sube como artefacto si su análisis personalizado termina con
resultado limpio y el archivo conserva su hash. Si Defender está inactivo,
no puede actualizarse, detecta algo en el JAR nuevo o su análisis falla, no se publica un JAR
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
Esas comprobaciones locales complementan los resultados de Windows indicados abajo.

Primera ejecución: [39080593114](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/39080593114),
correcta, con diez análisis personalizados sin detecciones, Defender
`1.1.26090.9` y firmas `1.461.77.0`. La protección en tiempo real del runner
estaba desactivada por su configuración inicial; no se reprodujo la alerta del
equipo del usuario. La segunda ejecución,
[39080803550](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/39080803550),
también finalizó sin detecciones con la protección en tiempo real activa; se
observó que MAPS y Block at First Sight estaban desactivados en el runner.
La tercera ejecución,
[39080971859](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/39080971859),
completó también la comprobación con nube y Block at First Sight habilitados.
La configuración oficial de las imágenes hospedadas incluye exclusiones para
`C:\` y `D:\` y desactiva otros controles. La última ejecución
elimina esas exclusiones y habilita análisis de archivos, scripts, descargas y
comportamiento. Su resultado se describe a continuación.

## Resultado completo y JAR independiente

La ejecución [39081168660](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/39081168660)
terminó correctamente el **17/10/2026, 21:01 (Argentina)**. Se compiló el código
del commit `c2a1304d358bb6dca9b3a560163baf12a98de6f1` y pasó su tarea de pruebas.
Defender examinó el ZIP original, sus ocho miembros y el JAR recién compilado:
**diez resultados sin amenazas, con salida 0 y sin errores**.

Se registraron Defender `1.1.26090.9`, producto `4.18.26090.9` y firmas
`1.461.77.0` del 17/10/2026. Protección en tiempo real, nube, Block at First
Sight, IOAV, comportamiento, scripts y archivos estaban habilitados. La prueba
de conexión MAPS terminó correctamente. Las listas de exclusiones de rutas,
extensiones y procesos estaban vacías. Las muestras recibieron marca de zona
de Internet. No se ejecutó su contenido para realizar estos análisis.

El JAR que se distribuye ahora es el artefacto de esa ejecución:

- Archivo: [LegendaryWeapons-2.0.4.jar](downloads/LegendaryWeapons-2.0.4.jar).
- SHA-256: `ea0c9201e93f1f6937fdbddef6e7bba59eb5df18a83eba0c29e45e7504c4b0fe`.
- [Hash descargable](downloads/LegendaryWeapons-2.0.4.jar.sha256).
- [Informe íntegro y registros de Defender](docs/defender-verification-2.0.4.json).

Sus 75 clases coinciden byte a byte con las del JAR original probado en Paper.
Las diferencias de bytes corresponden a finales de línea de Windows en los
recursos YML/JSON; las texturas y el contenido de los modelos se conservan.
La publicación no contiene scripts de inicio ni precisa extraer el ZIP bloqueado.

La detección del equipo del usuario **no se reprodujo**, incluso al examinar el
ZIP original en estas condiciones. Eso no permite afirmar que Microsoft haya
confirmado un falso positivo. El ZIP original sigue sin recomendarse. Para usar
el JAR independiente se había recomendado un análisis local previo a la instalación.
El nuevo aviso deja esa recomendación suspendida: no se debe continuar la
implementación ni restaurar archivos bloqueados hasta identificar lo detectado.
