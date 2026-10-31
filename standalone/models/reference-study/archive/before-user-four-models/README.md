# Estudio de las referencias del usuario — revisión del 30 de octubre

Propuesta visual pendiente de aprobación. No sustituye la distribución pública
2.0.14 ni los modelos de producción del plugin.

Quedan cinco armas activas: Bloodlust, Shadow Blade, Windweaver, Earthbreaker
y Thunder Spear. Frost Scythe se retiró por pedido del usuario; su proyecto y
sus fotos anteriores se conservan como archivo y no aparecen en la galería actual.

Los `.bbmodel` son proyectos editables, abiertos en Blockbench 5.2.2.
Shadow usa el archivo `espada_oscura_v3.bbmodel` que proporcionó el usuario;
los otros cuatro proyectos activos conservan sus mallas anteriores.
El original se guarda intacto en `user-sources/espada_oscura_v3.bbmodel`.
`shadow_blade.bbmodel` conserva sus 138 cubos, coordenadas, UV, textura incrustada
y cuatro animaciones. Únicamente explicita como invisibles las caras ausentes
para que el importador de Blockbench 5.2.2 pueda abrirlo. El modelo anterior se
conserva en `archive/shadow-before-user-model/`.
`model-overrides.json` identifica este proyecto como fuente vigente de Shadow;
las mallas generadas de Shadow en `geometry.json` son referencias históricas.
La rotación de ejes y la escala de composición solo afectan a las fotos.
Las animaciones se conservan como fuentes editables; todavía no se han integrado
en Minecraft. Windweaver se rediseñó
libremente a partir del nombre, según el último pedido.
Windweaver tiene un nuevo diseño propio
inspirado en un remolino: cabeza compacta con seis aletas gruesas de acero que
giran alrededor de un núcleo de aire facetado. Los tonos son plata, gris oscuro
y azul muy claro. Las aletas se unen al casquillo inferior y a la corona; una
cinta helicoidal recorre el mango recto. Se mantienen el identificador y las
habilidades del arma.

Earthbreaker usa hojas curvas con sección de cuña: raíz gruesa, nervio elevado
y filo fino, en lugar de las dos placas planas anteriores. El cubo central,
los refuerzos y los bulones tienen profundidad. Bloodlust y Thunder Spear tienen
hojas cerradas con sección romboidal y nervio central; la lanza conserva una
única punta. Los mangos permanecen rectos.

`review-blockbench.png` muestra las cinco armas y `review-head-details.png` muestra
el hacha y el mace en tres cuartos y de perfil. Son capturas del visor de
Blockbench, sin retoque ni generación de imagen. `review-shadow-user-model.png`
muestra el modelo proporcionado en vista frontal y tres cuartos.
`review-shadow-knife.png` conserva la propuesta anterior, ya sustituida.
`review-frost-horizontal.png` conserva la propuesta de Frost ya retirada.
`review-mace-purple.png` conserva la propuesta anterior. `review-windweaver-vortex.png`
muestra el nuevo diseño propio completo y con dos detalles de la cabeza. La
comparación de hacha y mace también se actualizó.
`geometry.json` conserva los componentes y materiales generados;
`../../tools/generate_reference_study.py` los regenera sin editar píxeles y
registra las fuentes externas vigentes. Las capturas se regeneran desde los
cinco proyectos activos con `../../tools/render_blockbench_review.py --view gallery`
y `--view shadow`, con Blockbench abierto y accesible por CDP.
La textura de los otros cuatro modelos activos se pintó con el editor de Blockbench mediante
`../../tools/paint_reference_materials.js` y está incrustada en los proyectos.
Las hojas en cuña, el núcleo y las aletas del mace y las hojas romboidales se verifican
como superficies cerradas, con volumen positivo y dos caras por arista.

Esta propuesta todavía no se ha exportado al formato del resource pack ni se ha
validado en Minecraft. Al aprobarse la apariencia, la exportación deberá usar
las rotaciones de elementos admitidas por Minecraft 26.2 y verificarse contra
estas mallas antes de publicar el plugin. No deben instalarse los `.bbmodel`
directamente como modelos Java del resource pack.
