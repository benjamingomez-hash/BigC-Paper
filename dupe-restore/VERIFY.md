# Validación de DupeRestore 1.0.0

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

## Descargas comprobadas en Windows

[La ejecución 41835451441](https://github.com/benjamingomez-hash/BigC-Paper/actions/runs/41835451441)
compiló y comprobó `923204227cbc8e6a8f06f5aa501a94df8b449743`: las 14 pruebas Java
pasaron y los **cinco análisis Defender terminaron sin detecciones**, motor
`1.1.26100.4`, firmas `1.463.122.0`, protección en tiempo real y nube activas,
sin exclusiones. Se publican los binarios de esa ejecución sin reconstruirlos.
El JAR coincide byte por byte con el ensayado en Paper 26.2-133.

[Informe Defender](docs/defender-verification-1.0.0.json) ·
[SHA-256 de descargas](downloads/SHA256SUMS.txt).
