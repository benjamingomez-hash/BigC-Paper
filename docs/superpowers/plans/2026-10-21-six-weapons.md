# Retirar Void Katana e Hyperion

Partir de 2.0.11 y dejar disponibles Bloodlust, Frost Scythe, Shadow Blade,
Windweaver, Earthbreaker y Thunder Spear. No agregar armas, fechas ni desbloqueos
de la segunda semana. Mantener el plugin independiente y la plataforma existente.

- Un catálogo de seis gobierna recetas, comandos, autocompletado y resolución de
  objetos. Eliminar el código de las habilidades retiradas y sus ajustes nuevos.
- Mantener los nombres históricos del enum para leer el diario existente.
  Separar eventos activos del historial y conservar recuperación/confirmación de
  transacciones anteriores. Los eventos retirados no avanzan ni protegen mesas.
- Conservar los assets originales para representar objetos antiguos, que quedan
  sin habilidad. No borrar inventarios ni registros del usuario.
- Verificar rechazo de recetas sin consumo, reservas, controles y recuperación
  histórica con JUnit. Compilar y comprobar catálogo, recetas, comandos y objetos
  retirados en Paper. Publicar rama y 2.0.12 con los mismos binarios analizados
  por el workflow Windows.

Completado: commit `7ba56f26aed7874f084c00030b9d6d787c8b4cff`, 80 pruebas
correctas y comprobación nativa de seis recetas, seis objetos disponibles,
comandos y rechazo de habilidades retiradas. Los 28 assets originales y las
fuentes de los poderes restantes se conservaron. Actions `39596991130` completó
cinco análisis Defender sin detecciones. Se distribuyen sus archivos exactos y
los hashes; la instancia privada fue detenida y sus datos originales restaurados.
