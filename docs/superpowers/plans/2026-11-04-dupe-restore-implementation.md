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
