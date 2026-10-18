# Comprobaciones de distribución 2.0.6

Esta actualización añade el daño fijo de Earthbreaker y la emboscada de Shadow Blade sobre 2.0.5. Los avisos locales `Trojan:Script/Ulthar.A!ml` de 2.0.4 siguen sin diagnóstico confirmado. El análisis anterior de 2.0.5 es un resultado histórico, no una autorización para la versión nueva.

El [workflow Windows 39088897056](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/39088897056) compiló y probó las fuentes `1fd150c094e4f04b22f63cce271f57e18f0abb67`. Defender completó cinco análisis (ZIP, tres miembros y JAR independiente), todos con código 0 y sin detecciones. Motor 1.1.26090.9 y firmas 1.461.79.0; protección en tiempo real, nube, comportamiento, IOAV, scripts y archivos comprimidos activas, conexión a nube validada, origen Internet y sin exclusiones de rutas, extensiones ni procesos. No se ejecutan muestras durante los análisis. [Informe completo](docs/defender-verification-2.0.6.json).

Se publican esos archivos exactos: JAR SHA-256 `9aa977e00bf2ad86809041283ce56e901a5a201b738c42d545de2bce0db7e0c1`; ZIP `e3d7aed5cb42314f0dce3b087174b65df219523955bd5294b9fffdd3302e2a42`. El JAR dentro del ZIP coincide con el compilado. Las 79 clases compiladas coinciden byte por byte con el JAR probado en Paper/Linux; las diferencias de recursos son finales de línea CRLF del checkout Windows. Los modelos se conservan. Se mantienen las versiones y registros anteriores.

Java 25: 69 pruebas sin fallos, errores o exclusiones. Paper 26.2-133/Linux con dos clientes oficiales Java 26.2: 16 escenarios de ambos poderes y una repetición de escudo con entrada real del cliente. [Resultados funcionales](docs/powers-verification-2.0.6.json). ClamAV 1.4.6 con daily 28156, main 63 y bytecode 339 tampoco detectó amenazas en los JAR/ZIP de Linux ni en los archivos exactos publicados desde Windows.

Una comprobación de archivos sin detecciones en el runner no reproduce la instalación o ejecución en la PC del usuario, ni demuestra que 2.0.4 fuera un falso positivo. Si Defender bloquea un archivo, debe permanecer en cuarentena; no hacen falta exclusiones para esta validación.
