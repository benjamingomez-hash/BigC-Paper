# Validación de DupeRestore

## 1.1.0 — API pública y opciones nativas explícitas

Reconstrucción desde las fuentes 1.0.0, reaplicando el filtro waterlogged. Se
eliminaron el puente de reflexión y los cambios internos de Paper. Las opciones
nativas se activan manualmente en Paper; el plugin solo consulta una instantánea
pública y administra el hilo. No se ejecutó la versión reportada al investigar.

- Compilación limpia: **14 pruebas Java correctas** del plugin y **1.014** del
  proyecto principal.
- **40 casos correctos en dos servidores Paper 26.2-133**, con opciones nativas
  false y true. Los valores y el archivo de Paper se conservaron al recargar y
  desactivar el plugin. Las granjas de trampilla/escalera waterlogged siguieron
  produciendo; agua común, corrientes intermedias y origen vaciado no repusieron hilo.
- Inspección del JAR: **11 clases de producción**, sin probe ni archivos inesperados
  y sin referencias directas a reflexión, ClassLoader, procesos, red o clases
  internas del servidor. Esta inspección no equivale a un análisis antimalware.

[Prueba nativa](docs/native-verification-1.1.0.json) ·
[Inspección de clases](docs/jar-audit-1.1.0.json).

La detección local de 1.0.1 no tiene identificador ni archivo confirmados; no se
ha determinado su causa. Los resultados Windows registrados abajo
no garantizan resultados en otro equipo.

### Comprobación Windows y alerta local

[La ejecución 42130799179](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/42130799179)
compiló `c2ceeb5c30d2f3271263843d5d4d74dc06718c93` y pasó 14 pruebas, la inspección
de clases y **cinco análisis Defender sin detecciones** del JAR y ZIP nuevos.
Motor `1.1.26100.4`, firmas `1.463.147.0`, protección y nube activas,
sin exclusiones. El JAR es idéntico al ensayado en los dos servidores Paper.
Se distribuyen sus bytes exactos, sin recompilarlos.

La referencia 1.0.1 tuvo cuatro análisis adicionales sin detecciones en ese
runner; solo se descargó, extrajo y analizó, sin ejecutarse. No se reprodujo allí
la alerta del usuario y su causa local permanece sin identificar. Esto no prueba
que la alerta local sea falsa ni que otro equipo vaya a aceptar el archivo.

[Informe Defender](docs/defender-verification-1.1.0.json) ·
[Revisión y límites](docs/security-review-1.1.0.json) ·
[SHA-256 de descargas](downloads/SHA256SUMS.txt).

## 1.0.1 — origen directo waterlogged (histórico; detección local informada)

La fuente de la rotura debe ser un bloque con `Waterlogged.isWaterlogged()`;
se vuelve a comprobar antes de reponer el hilo. El usuario confirmó producción
continua mientras salga agua del bloque anegado.

- **14 pruebas Java correctas** del plugin y **1.014** del proyecto principal,
  con compilaciones limpias.
- **19 casos correctos en Paper 26.2-133**: tanto una trampilla anegada como una
  escalera anegada produjeron 20 ítems en 100 ticks desde una pieza de hilo.
- Agua común y una corriente intermedia produjeron solo el drop original, sin
  reponer el hilo. Si el origen se vacía entre la rotura y la tarea siguiente,
  tampoco se repone. Pasaron los controles de flujos bloqueados, drops vacíos,
  líneas desconectadas, rotura manual, recarga y desactivación.
- Se reprodujo el error con el JAR publicado 1.0.0 usando el mismo mecanismo:
  producía continuamente con agua común y no reponía desde los bloques anegados.

[Prueba nativa](docs/native-verification-1.0.1.json) ·
[Comparación antes/después](docs/waterlogged-regression-1.0.1.json).

### Descargas 1.0.1 comprobadas en Windows

[La ejecución 42128056854](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/42128056854)
compiló y probó `2283de9bc0da97f9b6c9b710d38abb18f20a762c`: 14 pruebas correctas
y cinco análisis Defender sin detecciones, motor `1.1.26100.4`, firmas
`1.463.147.0`, protección en tiempo real y nube activas, sin exclusiones.
Se distribuyeron sus archivos exactos, sin reconstruirlos. El JAR coincide byte
por byte con el probado en Paper para los 19 casos.

[Informe Defender](docs/defender-verification-1.0.1.json) ·
[SHA-256 originales](https://github.com/benjamingomez-hash/BigC-Paper/blob/fe2abff21b4219f0018a85f18c1a4ed809c42530/dupe-restore/downloads/SHA256SUMS.txt).

## 1.0.0 — registro histórico

- Compilación limpia del plugin: **14 pruebas Java correctas**. Validan opciones
  reversibles, rollback ante errores, valores originales, geometría y orientación
  de ganchos, distancia máxima, chunks descargados y reposiciones simultáneas.
- Compilación limpia del proyecto principal: **1.014 pruebas correctas**.
- Servidor aislado **Paper 26.2-133**: **15 comprobaciones correctas**. Una granja
  iniciada con un hilo produjo **20 ítems de hilo en 100 ticks** y siguió activa.
  Después de deshabilitar la reposición, una granja nueva produjo un solo hilo;
  tras reactivarla volvió a producir repetidamente.
- Se comprobaron flujo cancelado, drops vacíos, hilo desconectado y rotura
  manual; no generaron reposición. Recargar/desactivar restauró los dos flags de
  Paper y retiró las tareas pendientes.

[Resultado nativo completo](docs/native-verification-1.0.0.json).

TNT/alfombras y ganchos usan las opciones reales de Paper, cuyo valor activo y
restauración se comprobaron en el servidor. No se ensayaron todos los diseños
posibles de máquinas ni protecciones de terceros. La compatibilidad de una granja
antigua con la física vanilla 26.2 debe probarse con ese diseño.

El servidor de prueba tuvo una advertencia de conexión a las claves públicas de
Mojang; la prueba no usa autenticación ni jugadores y completó sus 15 casos.
No se modificaron mundos, configuraciones ni datos de ningún servidor existente.

### Descargas 1.0.0 comprobadas en Windows

[La ejecución 41835451441](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/41835451441)
compiló y comprobó `923204227cbc8e6a8f06f5aa501a94df8b449743`: las 14 pruebas Java
pasaron y los **cinco análisis Defender terminaron sin detecciones**, motor
`1.1.26100.4`, firmas `1.463.122.0`, protección en tiempo real y nube activas,
sin exclusiones. Se publicaron los binarios de esa ejecución sin reconstruirlos.
El JAR coincide byte por byte con el ensayado en Paper 26.2-133.

[Informe Defender](docs/defender-verification-1.0.0.json) ·
[SHA-256 originales](https://github.com/benjamingomez-hash/BigC-Paper/blob/6de5a558da62d88dbfa39468ba5f5e2d28ad25b8/dupe-restore/downloads/SHA256SUMS.txt).
