# Comprobaciones de distribución 2.0.7

Fuentes `b152deb72d016838e858d0fdd44d07d22664b526`: robo de vida de Bloodlust y Shadow Blade limitada a seis puntos mágicos (tres corazones antes de reducciones), incluso con una configuración anterior de catorce. Modelos y contratos persistentes conservados.

El [workflow Windows 39089836320](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/39089836320) compiló y probó el plugin. Defender completó cinco análisis (ZIP, sus tres miembros y JAR independiente), todos con código cero y sin detecciones. Motor 1.1.26090.9, firmas 1.461.79.0; protección en tiempo real, nube, comportamiento, IOAV, scripts y archivos comprimidos activas. Conexión a nube validada, origen Internet y ninguna exclusión de rutas, procesos o extensiones. No se ejecutan muestras durante el análisis. [Informe](docs/defender-verification-2.0.7.json).

Se publican esos archivos exactos: JAR `a58dc1945b8a398cdc23049b5307374b9822dc0ae3073ff791e4045acb176e9b`; ZIP `cc79aa6e8093361ee00e6a2849215df1a88ca2d889fc11035b045e9922d13ad0`. Sus ochenta clases compiladas coinciden con el JAR validado en Paper/Linux. Las diferencias de recursos son finales de línea del checkout Windows; imágenes idénticas. El JAR dentro del ZIP coincide con el independiente. ClamAV 1.4.6, daily 28156/main 63/bytecode 339, tampoco detectó amenazas en los artefactos Linux ni en los archivos exactos publicados desde Windows.

Java 25: 74 pruebas sin fallos, errores o exclusiones. Paper 26.2-133/Linux con dos clientes oficiales Java 26.2: dieciocho escenarios validados, con veintidós observaciones conservadas de la compilación final. [Resultados y límites](docs/bloodlust-verification-2.0.7.json).

Estos análisis de archivos no reproducen la instalación o ejecución en la PC del usuario. Los avisos locales `Trojan:Script/Ulthar.A!ml` de 2.0.4 siguen sin diagnóstico confirmado; no se afirma que fueran falsos positivos ni que Defender permita esta versión en otro equipo. Los archivos bloqueados deben permanecer en cuarentena, sin exclusiones ni desactivar la protección. Se conservan las versiones y sus registros anteriores.
