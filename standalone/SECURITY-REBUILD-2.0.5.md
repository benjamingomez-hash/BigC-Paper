# Estado de la reconstrucción 2.0.5

El usuario informó dos detecciones locales de Microsoft Defender, `Trojan:Script/Ulthar.A!ml`: el ZIP 2.0.4 y posteriormente `LegendaryWeapons-2.0.4.jar` en cuarentena. El diagnóstico sigue sin confirmar. No se recomienda restaurar esos archivos ni crear exclusiones.

Esta rama parte del código 2.0.3 (`d75fa8bc19002f362df5732a430ec83e7bd84c1f`) y rehace la parálisis de la lanza por solicitud del usuario. La nueva versión es 2.0.5. No se reutiliza el binario 2.0.4. Las otras siete habilidades, los modelos y los contratos persistentes se conservan.

Validación completada: compilación desde fuentes y 66 pruebas sin fallos, cuatro casos en Paper 26.2-133 en Linux con dos clientes oficiales 26.2, y comparación de los 28 archivos de modelos con 2.0.3. El plugin integrado y el cobre permanecen sin cambios. [Resultados funcionales](docs/rebuild-verification-2.0.5.json).

[Windows Actions 39088143178](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/39088143178) compiló y probó el plugin; Microsoft Defender analizó el ZIP, sus tres archivos internos y el JAR independiente. Los cinco análisis terminaron con código 0 y sin amenazas detectadas. Motor 1.1.26090.9, firmas 1.461.79.0; protección en tiempo real, nube, comportamiento, IOAV, scripts y archivos comprimidos activas; sin exclusiones de rutas, procesos ni extensiones. Se marcó el origen Internet y se comprobó la conexión a la nube. [Informe completo](docs/defender-verification-2.0.5.json).

Los archivos publicados proceden de ese runner y coinciden con los SHA-256 del informe: JAR `0aeb7c9a050c87c7e1eca8b9856b43a4b0e326e9aea70f8bf4f6f77ecabed46a`; ZIP `56384b30a77eb6e0c5c89e756752f20ba12814d8637e4649e92ebf5003bcabd5`. Sus 77 clases compiladas coinciden byte por byte con las probadas en Paper/Linux. Las diferencias de recursos son finales de línea CRLF del checkout Windows; imágenes idénticas.

ClamAV 1.4.6, con daily 28156, main 63 y bytecode 339, no detectó amenazas en los artefactos compilados en Linux ni en el JAR y ZIP exactos publicados desde Windows. Son comprobaciones de archivos, no un diagnóstico de la alerta local.

El ZIP se genera directamente desde Gradle e incluye únicamente JAR, pack y LEEME. El workflow comprueba que el JAR dentro del ZIP coincide con el compilado. No se añaden ejecutables auxiliares ni se restaura la copia del usuario en cuarentena.

Un análisis sin detecciones en ese runner no reproduce necesariamente la instalación o ejecución local del usuario. No demuestra que el aviso anterior fuera un falso positivo ni garantiza la aceptación de esta reconstrucción por otra instalación de Defender. No hay un dictamen de Microsoft ni una solicitud de revisión enviada.

Los archivos anteriores y el registro del incidente se conservan en [la rama anterior](https://github.com/benjamingomez-hash/BigC-Paper/blob/feat/legendary-crafting-pvp/standalone/SECURITY-REVIEW-2.0.4.md).
