# Implementación de DupeRestore

1. Escribir pruebas de opciones nativas y granjas de hilo antes del código.
2. Implementar opciones reversibles y reposición de hilo validada por tick.
3. Añadir entrada del plugin, configuración y comando de administración.
4. Compilar y probar en un servidor aislado, incluyendo casos negativos.
5. Documentar instalación, alcance, validación y distribuir el JAR separado.

Estado: implementación completada. Pasaron 14 pruebas unitarias del plugin,
1.014 del proyecto principal y 15 casos reales en Paper 26.2-133. El hilo
produjo 20 ítems en 100 ticks partiendo de un bloque. Windows completó las mismas 14 pruebas y cinco análisis
Defender sin detecciones en la ejecución 41835451441. Se distribuyen exactamente
sus archivos; el JAR coincide con el probado en Paper. Informes y hashes en
`dupe-restore/VERIFY.md`.

Corrección 1.0.1: validar origen directo waterlogged y su estado al reponer.
El usuario confirmó producción continua; no se introduce modo de un solo pulso.
La prueba nativa añade controles de agua común, corriente intermedia y origen
vaciado. Compilación limpia completada; Defender se verificó para el nuevo binario.

Validación de la corrección: 14 pruebas Java y 19 casos reales en Paper correctos;
la regresión se reprodujo con el JAR publicado 1.0.0 y se corrigió en 1.0.1.
La compilación limpia principal pasó nuevamente sus 1.014 pruebas.

Publicación 1.0.1: ejecución Windows 42128056854 correcta, cinco análisis sin
detecciones. Se publicaron sus archivos y el JAR coincide con la prueba nativa.
