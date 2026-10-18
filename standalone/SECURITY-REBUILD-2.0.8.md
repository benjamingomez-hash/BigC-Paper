# Reconstrucción 2.0.8: distribución pendiente de revisión

El usuario pidió volver a 2.0.5 e incluir todos los cambios de habilidades. La rama nace de `a1ca1655e082173cde24f1493da91152a4b01d92`. Se reintroducen las fuentes revisadas de Earthbreaker, Shadow Blade y Bloodlust y se compila de nuevo; no se reutilizan JAR/ZIP posteriores. La lógica del resto de las armas, modelos, recetas y datos se conserva.

## Aviso confirmado

La captura de Windows muestra `Trojan:Script/Ulthar.A!ml`, estado «Quitado», en `Armas-Legendarias-2.0.7.zip`, el 17/10/2026 a las 23:29 del equipo del usuario. La URL identifica la descarga del commit `f3a7828a88f9d706abb281dbfb2e877b22ab9b3a`. Su referencia pública tiene SHA-256 `cc79aa6e8093361ee00e6a2849215df1a88ca2d889fc11035b045e9922d13ad0`. No se restauró la copia quitada para comprobar su hash local. La captura contiene una ruta personal que no se publica aquí.

## Resultado funcional

Java 25, build completo: **74 pruebas, cero fallos, errores o exclusiones**. Paper 26.2-133/Linux con dos clientes oficiales 26.2: cinco casos representativos confirmaron Bloodlust (dos golpes curan dos puntos en total), hacha (cuatro puntos de salud aun con Resistencia V y absorción, cancelación respetada) y Sombras (seis puntos mágicos tanto con el valor antiguo catorce como sin la clave). [Resultados](docs/rebuild-verification-2.0.8.json).

Las ochenta clases compiladas coinciden byte por byte con 2.0.7. Es el resultado de aplicar los mismos cambios solicitados a la base 2.0.5. **No hay una corrección identificada de la detección; cambiar la base o el número de versión no demuestra resolverla.** El JAR no contiene scripts ni ejecutables nativos adicionales; el pack contiene JSON/PNG/MCMETA. Es una observación de contenido, no un dictamen de seguridad. [Comparación de archivos](docs/archive-comparison-2.0.5-2.0.7.json).

## Comparación Windows

[Actions 39090740658](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/39090740658), fuentes `457a5571ab6c796e0f9c57ff195d38996d58c911`:

- Se descargaron las referencias públicas exactas de 2.0.5 y 2.0.7 y se verificaron sus hashes fijados antes de analizarlas. Se analizaron ambos ZIP y cada uno de sus tres archivos internos: ocho análisis.
- Se analizó la reconstrucción nueva: ZIP, tres archivos internos y JAR independiente: cinco análisis.
- Los trece análisis terminaron con código cero y sin detecciones. **El runner no reprodujo el aviso contra el propio ZIP 2.0.7 que Windows quitó al usuario.**
- Motor 1.1.26090.9 y firmas 1.461.79.0. Protección en tiempo real, nube, comportamiento, IOAV, scripts y comprimidos activas; conexión a nube validada, marca de origen Internet y sin exclusiones de rutas, procesos ni extensiones. No se ejecutaron las referencias analizadas.
- Los manifiestos de hashes de cada entrada confirman las mismas ochenta clases entre el JAR nuevo de Windows, el JAR 2.0.7 descargado y el JAR nuevo validado en Linux. Los recursos difieren por finales de línea del checkout, como en las versiones anteriores.

[Informe completo, referencias y hashes por entrada](docs/defender-comparison-2.0.8.json). ClamAV 1.4.6, daily 28156/main 63/bytecode 339, tampoco detectó amenazas en el JAR/ZIP de reconstrucción Linux. No equivale a una aprobación de Microsoft ni a reproducir la instalación en la PC del usuario.

## Estado de distribución y siguiente paso

Se publican las fuentes y los informes. No se publica otro binario como solución confirmada al aviso, ni se recomienda restaurar muestras quitadas o en cuarentena. Los artefactos nuevos permanecen en el directorio de build para revisión técnica y el workflow conserva únicamente informes. Las versiones anteriores se conservan en sus ramas y commits.

La revisión de clasificación sigue pendiente. Se preparó [el texto para Microsoft](docs/microsoft-review-request-2.0.7.txt), con el archivo, hash, URL y resultados comparativos, **sin enviarlo**. El portal oficial es https://www.microsoft.com/en-us/wdsi/filesubmission y su envío requiere interacción humana; no hay identificador de caso ni veredicto. Antes de nuevas pruebas locales, comprobar las actualizaciones de protección en Seguridad de Windows. Actualizar no requiere restaurar un archivo bloqueado ni crear exclusiones.
