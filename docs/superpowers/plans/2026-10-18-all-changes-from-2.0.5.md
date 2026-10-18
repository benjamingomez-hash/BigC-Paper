# Todos los cambios juntos desde 2.0.5

El usuario aclara que quiere partir de 2.0.5 y agregar todos los cambios juntos,
no volver a la versión original sin cambios. La rama nueva nace directamente de
`a1ca1655e082173cde24f1493da91152a4b01d92`, sin los commits de 2.0.6/2.0.7/2.0.8.

- Aplicar en un único cambio las fuentes revisadas de Earthbreaker (4 puntos de
  salud una vez), Sombras (máximo 6 puntos mágicos y efectos mejorados) y
  Bloodlust (Fuerza III 10 s y curación de 1 punto por golpe directo válido).
- Conservar la parálisis de la lanza de 2.0.5, las otras armas, modelos, recetas,
  persistencia y cooldowns. No modificar el plugin raíz ni sus dependencias.
- Compilar de cero como 2.0.9, ejecutar la suite completa y comprobar el contenido
  del JAR/ZIP. Reutilizar las herramientas privadas de Paper para las habilidades.
- Ejecutar Defender en Windows con protección activa y sin exclusiones, y
  publicar los archivos exactos analizados, sus hashes y el informe.
- Mantener explícito que esta operación no identifica la causa del aviso local
  ni confirma una solución al antivirus. No alterar código para eludir detecciones.

Las versiones y ramas anteriores permanecen conservadas.
