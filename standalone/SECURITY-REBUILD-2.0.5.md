# Estado de la reconstrucción 2.0.5

El usuario informó dos detecciones locales de Microsoft Defender, `Trojan:Script/Ulthar.A!ml`: el ZIP 2.0.4 y posteriormente `LegendaryWeapons-2.0.4.jar` en cuarentena. El diagnóstico sigue sin confirmar. No se recomienda restaurar esos archivos ni crear exclusiones.

Esta rama parte del código 2.0.3 (`d75fa8bc19002f362df5732a430ec83e7bd84c1f`) y rehace la parálisis de la lanza por solicitud del usuario. La nueva versión es 2.0.5. No se reutiliza el binario 2.0.4. Las otras siete habilidades, los modelos y los contratos persistentes se conservan.

Validación en curso: compilación y pruebas desde fuentes, prueba de Paper en Linux con dos clientes, comparación de recursos, análisis ClamAV y análisis de archivos con Microsoft Defender en un runner Windows con protección activa y sin exclusiones. El ZIP se genera directamente desde Gradle e incluye únicamente JAR, pack y LEEME. El workflow comprueba que el JAR dentro del ZIP coincide con el compilado.

Un análisis sin detecciones en ese runner no reproduce necesariamente la instalación o ejecución local del usuario. No demuestra que el aviso anterior fuera un falso positivo ni garantiza la aceptación de esta reconstrucción por otra instalación de Defender. No hay un dictamen de Microsoft ni una solicitud de revisión enviada.

Los archivos anteriores y el registro del incidente se conservan en [la rama anterior](https://github.com/benjamingomez-hash/BigC-Paper/blob/feat/legendary-crafting-pvp/standalone/SECURITY-REVIEW-2.0.4.md).
