# Reimplementación de DupeRestore 1.1.0

1. Revisar los bytes y las referencias de la distribución publicada. No se
   ejecuta la 1.0.1 después del aviso del usuario; falta el diagnóstico local.
2. Recuperar las fuentes 1.0.0 y reaplicar origen waterlogged y producción continua.
3. Sustituir la modificación reflectiva de Paper por configuración nativa explícita
   y consulta de solo lectura mediante API pública. Mantener el hilo independiente.
4. Validar la nueva distribución con pruebas unitarias, dos servidores aislados,
   inspección de clases, compilaciones limpias y Windows Defender actualizado.
5. Publicar únicamente los nuevos bytes comprobados y documentar instalación,
   procedencia y límites. Reanalizar la referencia 1.0.1 solo como diagnóstico.

Estado: 14 pruebas unitarias y 40 casos nativos correctos; compilación principal
con 1.014 pruebas correctas. Inspección del JAR correcta. Windows en curso;
los informes y límites se enlazan en `dupe-restore/VERIFY.md`.
