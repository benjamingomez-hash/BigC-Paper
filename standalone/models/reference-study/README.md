# Armas legendarias — modelos proporcionados por el usuario

Fuentes de los cinco modelos integrados en la versión 2.0.15.
Los originales se conservan junto a los proyectos de revisión.
Frost Scythe, Void Katana e Hyperion permanecen retiradas.

## Fuentes vigentes

| ID | Archivo original en `user-sources/` | Animaciones |
| --- | --- | --- |
| bloodlust | `espada_rubi (3).bbmodel` | `idle_aura`, `ataque_tajo`, `estocada` |
| shadow_blade | `espada_oscura_v3.bbmodel` | `aura_oscura`, `tira_particulas`, `idle_flotar`, `golpe_oscuro` |
| windweaver | `windweaver (1).bbmodel` | `idle_aura`, `golpe_martillo`, `golpe_lateral` |
| earthbreaker | `battle_axe_acostado.bbmodel` | Ninguna incluida |
| thunder_spear | `cetro_cristal_rayo.bbmodel` | `rayo` |

Los originales se conservan intactos. Los cuatro nuevos proyectos activos son
copias exactas de sus archivos originales, incluidas coordenadas, pivotes, grupos,
rotaciones, UV, textura incrustada y animaciones. Shadow conserva la normalización
anterior: explicita como invisibles sus 50 caras ausentes para que Blockbench 5.2.2
pueda abrir el proyecto; la geometría, caras existentes y animaciones no cambian.
Los proyectos anteriores se guardan en `archive/`; Frost y las fotos anteriores
se mantienen como fuentes históricas.

`model-overrides.json` identifica los cinco proyectos vigentes y los hashes de
los originales. `geometry.json` conserva las mallas generadas anteriores como
referencias históricas y registra esas sustituciones en `sourceOverrides`.
`../../tools/generate_reference_study.py` regenera ese registro sin editar píxeles
ni sustituir los archivos proporcionados por el usuario.

## Vista y comprobación

`review-blockbench.png` muestra los cinco archivos en el visor real de Blockbench.
`review-gallery.bbmodel` es la composición estática editable utilizada para esa
captura; las animaciones se conservan en cada proyecto individual, no en la galería.
La orientación y la escala uniforme utilizadas para encuadrar las armas afectan
únicamente a la composición; los proyectos individuales no se deforman ni giran.
Cada proyecto utiliza su propia textura incrustada, sin repintado ni generación
de imágenes.

Con Blockbench abierto y accesible por CDP, la galería se regenera ejecutando
`python standalone/tools/render_blockbench_review.py --view gallery` desde la raíz
del repositorio. `--view shadow` conserva la vista individual de Shadow.

`user-models-verification.json` registra la reapertura de los cinco proyectos en
Blockbench 5.2.2, las referencias de textura, la conservación de los originales
y la comprobación de las once animaciones en distintos tiempos de reproducción.
Las auras de Bloodlust y Windweaver permanecen ocultas hasta los 4,2 segundos de
sus ciclos de seis segundos; se muestrean también los tramos finales.
El hacha no contiene animaciones y no se inventó ninguna.

Los `.bbmodel` siguen siendo fuentes editables y no se instalan directamente en
Minecraft. `../../tools/export_user_animations.py` abre los proyectos en Blockbench,
muestrea las once animaciones a 20 Hz y verifica las ocho esquinas de cada cubo
exportado contra la matriz original. Conserva los PNG incrustados sin repintarlos.
Los modelos resultantes usan las rotaciones Euler admitidas en Minecraft 26.2;
no añaden una dependencia de BetterModel ni un mod de cliente.

Las auras de Bloodlust y Windweaver y la flotación de Shadow se reproducen al
sostenerlas. Los ataques usan `ataque_tajo`/`estocada`, `golpe_oscuro` y
`golpe_lateral`; el poder usa `estocada`, `aura_oscura` + `tira_particulas` y
`golpe_martillo`, respectivamente. Thunder Spear usa `rayo` en mano al activar sus poderes. Earthbreaker no incluye clips inventados. Los iconos de inventario
usan el reposo para no desplazarse mientras elegís un objeto.

`legendary-animations.properties` contiene las secuencias de fotogramas y el
informe `../../docs/user-models-export-2.0.15.json` registra la conservación de
las fuentes, texturas y geometría. La comprobación real del cliente y los análisis
Windows se documentan en `../../VERIFY.md`.
