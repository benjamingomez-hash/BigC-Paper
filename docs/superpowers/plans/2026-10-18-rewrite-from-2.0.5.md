# Reescritura desde 2.0.5

El usuario solicita una reimplementación desde la base 2.0.5 para probarla,
conservando los cambios de jugabilidad posteriores. Base exacta:
`a1ca1655e082173cde24f1493da91152a4b01d92`. Rama nueva; se conserva todo el historial.

La versión 2.0.11 introduce componentes escritos de nuevo para los tres poderes:

- Bloodlust: listener propio de robo de vida, con regla pura de salud recuperable
  y contexto de sesión suministrado por el runtime. Fuerza III y duración de diez
  segundos siguen siendo propiedad de la sesión de la base.
- Earthbreaker: componente dueño del pulso, selección de objetivos y contextos
  de daño por entidad. Usa daño nativo y normaliza sus reducciones únicamente
  durante su propio golpe; respeta cancelaciones e invulnerabilidad.
- Sombras: calcula y conserva un plan de emboscada antes de consumir el cooldown,
  con geometría horizontal y configuración limitada a seis puntos mágicos.
  Mantiene teletransporte seguro y beneficios/perjuicios temporales con propiedad.

Conservar sin reescribir la lanza de 2.0.5, otros poderes, ocho modelos, recetas,
evento, identificadores, diario y cooldowns. No cambiar plataforma ni plugin raíz.
No reutilizar los dos helpers de 2.0.6–2.0.10 ni sus clases compiladas.

Validación: pruebas unitarias de reglas y límites, build completo Java 25,
pruebas nativas de daño/curación/protecciones en Paper con dos clientes,
comparación por entrada del JAR, Defender Windows activo sin exclusiones y
publicación de los mismos binarios analizados con sus hashes.

La reescritura busca una implementación clara y verificable. No cambia el
comportamiento de seguridad del equipo ni demuestra resolver la detección previa.

Completado: `cf25ed115f63e2ba55629a0ad83b912bfd79aeb5` implementa los tres componentes
directamente sobre 2.0.5. Build completo con 77 pruebas correctas y 34 casos nativos
correctos en Paper con dos clientes oficiales. Windows Actions `39093766102`
completó cinco análisis Defender sin detecciones. Las 84 clases distribuidas
coinciden con las probadas en Paper; frente a 2.0.10 hay siete clases nuevas,
tres retiradas y siete compartidas diferentes. ClamAV tampoco detectó amenazas.
Se publican los archivos exactos del runner y sus hashes; la instancia aislada
se detiene y sus datos originales se restauran.
