ALTER TABLE movimiento
MODIFY tiempoSalida timestamp NULL DEFAULT NULL;

DROP VIEW IF EXISTS movimientofullinfo;

CREATE VIEW movimientofullinfo AS
SELECT m.idMovimiento,
       m.idVehiculo,
       m.tiempoEntrada,
       m.tiempoSalida,
       m.minutosEstacionado,
       m.horasCobradas,
       m.costoTotal,
       m.tarifaHora,
       m.tiempoCreacion,
       m.tiempoActualizacion,
       m.idEspacio,
       ee.claveEspacio,
       ee.tipo AS tipoEspacio
FROM movimiento m
JOIN espacioestacionamiento ee ON ee.idEspacio = m.idEspacio;
