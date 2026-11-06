# Modelos originales de las cinco armas

Se conserva una sola copia de cada proyecto aportado, sin modificar sus bytes:

| ID | Original en `user-sources/` | Animaciones |
| --- | --- | --- |
| bloodlust | `espada_rubi (3).bbmodel` | `idle_aura`, `ataque_tajo`, `estocada` |
| shadow_blade | `espada_oscura_v3.bbmodel` | `aura_oscura`, `tira_particulas`, `idle_flotar`, `golpe_oscuro` |
| windweaver | `windweaver (1).bbmodel` | `idle_aura`, `golpe_martillo`, `golpe_lateral` |
| earthbreaker | `battle_axe_acostado.bbmodel` | Ninguna incluida |
| thunder_spear | `cetro_cristal_rayo.bbmodel` | `rayo` |

`model-overrides.json` registra las rutas, SHA-256 y transformaciones de exportación.
`../../tools/model_sources.py` valida los originales y los carga directamente.
Para Shadow explicita **en memoria** sus 50 caras ausentes como invisibles,
conservando las caras existentes, su orden, UV, textura, geometría y animaciones.
No se almacena otro proyecto normalizado ni se reescriben los archivos aportados.

[Captura de los cinco proyectos en Blockbench](review-blockbench.png).

## Herramientas

`python3 standalone/tools/model_sources.py` prepara las cinco copias compatibles
para abrir individualmente en Blockbench en **`standalone/build/model-sources/`**.
Conserva sus animaciones; la normalización de Shadow queda fuera de Git.

- `python3 standalone/tools/render_blockbench_review.py --view gallery` crea una
  galería editable y captura en **`standalone/build/model-review/`**, que Git ignora.
  También admite `--view shadow`.
- `python3 standalone/tools/export_user_animations.py` exporta las once animaciones
  a modelos Java 26.2 y copia las texturas incrustadas sin repintarlas. Requiere
  NumPy, Playwright y Blockbench accesible por CDP en el puerto 9224.
- La salida del exportador es la base previa a la optimización. La versión
  optimizada comparte geometría entre poses: ver [PERFORMANCE.md](../../PERFORMANCE.md).

La galería transforma únicamente su composición; los originales siguen siendo
los proyectos editables. Los archivos `.bbmodel` no se instalan en Minecraft.

## Historial

Los borradores, diseños reemplazados, galerías generadas y respaldos completos
se consultan en el [snapshot anterior a la limpieza](https://github.com/benjamingomez-hash/BigC-Paper/tree/e490fb522fbac86636c822ed891c7b7b015383d3/standalone/models).
No forman parte del árbol actual del PR. La prueba de regresión conserva solo
las cinco mallas de reposo y sus texturas necesarias, en
`src/test/resources/legendary-rest-baseline-2.0.15.zip`, relativa a `standalone/`.
