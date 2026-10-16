# Crafting PvP — plugin independiente

El usuario sustituyó los altares por eventos de mesa vanilla y confirmó explícitamente Paper 26.2 y distribución independiente, aunque el documento original solicitaba integración. Implementar únicamente en standalone/, conservando el resto del repositorio y la rama anterior.

Reservar una sola fabricación por arma con diario atómico; duración fija de 1.200 segundos reales. Persistir zonas anidadas de 1.000 y 500 bloques con desplazamiento aleatorio inclusivo y coordenadas originales. Revelar a 5/10/15 minutos; arma visual y reclamo exclusivo a los 20. El fabricante no tiene propiedad ni bonificaciones. Anuncio de reclamo anónimo.

Consumir ingredientes en una transacción recuperable de inventario; recuperar consumo/entrega antes de admitir nuevas interacciones. La reserva, transacción y estado pertenecen al mismo registro persistido. Registrar avisos antes de emitirlos para no repetirlos después de reinicios. Una caída entre confirmar un aviso y enviarlo puede omitirlo; no se promete entrega exactamente una vez a clientes externos.

No cargar chunks para los indicadores: persistir el estado y reconstruir al cargar. Proteger únicamente mesa y bloque superior; no proteger jugadores durante el evento. Aislar toda entidad visual y ciclo de vida. Mantener los ataques y cooldowns anteriores.

Validación prevista: pruebas de reloj/zonas/estados/reinicio/concurrencia/consumo y reclamo; build completo; servidor Paper 26.2 real. Publicar rama feat/legendary-crafting-pvp y PR si la autenticación lo permite.
