# Rediseño visual de ocho armas legendarias

El usuario aportó una captura de inventario como referencia y pidió inspirarse únicamente para las ocho armas acordadas, excluyendo cobre y otros ítems. Estética: metal oscuro, siluetas escalonadas legibles, bordes vivos, gemas, runas y contrastes luminosos.

Alcance: únicamente los assets del plugin independiente, generador de geometría JSON, pruebas de contratos del pack y documentación/versionado de su distribución. Conservar IDs, materiales, recetas, habilidades, temporizadores y persistencia. Ningún cambio en módulos o assets de cobre.

Textura original generada con image_gen a partir del estilo de la referencia; conservar el PNG sin editar. El atlas fuente no está en el directorio item para impedir su carga directa. El atlas nativo de Minecraft extrae 64 materiales de 128×128 mediante minecraft:unstitch; cada arma usa su fila de ocho materiales. Así se conservan recortes de potencia de dos para mipmaps sin transformar la imagen generada mediante herramientas externas.

Crear ocho modelos tridimensionales originales, con siluetas individuales, decoración y transformaciones de inventario/manos/suelo/mesa. Validar referencias de sprites, UV, bounds de cuboides y límites de emisión. Compilar, probar en cliente oficial 26.2 con mipmaps, capturar galería/inventario y publicar en feat/legendary-crafting-pvp.

## Resultado

- Ocho geometrías nuevas generadas mediante `standalone/tools/generate_models.py`, con un atlas original y extracción nativa de materiales. Sin cambios de identidades ni de código de jugabilidad.
- Distribución independiente actualizada a 2.0.1. Compilación completa y 49 pruebas correctas.
- Cliente oficial 26.2 y Paper 26.2-56: carga del pack sin errores de modelos o sprites, revisión de los ocho iconos y modelos en ItemDisplay y de Thunder Spear en primera persona. Se mantuvieron los mipmaps de bloques en cuatro niveles.
- Capturas reales y proceso de regeneración documentados en `standalone/MODELS.md`; comprobación de archivos de cobre mediante SHA-256 frente a la instantánea previa.
- La galería usa una herramienta auxiliar externa al repositorio; no añade comandos de entrega al plugin de producción.
