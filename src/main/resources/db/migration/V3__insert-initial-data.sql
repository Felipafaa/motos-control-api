
INSERT INTO localizacao (id, zona, data_hora) VALUES (1, 'Patio A', CURRENT_TIMESTAMP);
INSERT INTO localizacao (id, zona, data_hora) VALUES (2, 'Oficina', CURRENT_TIMESTAMP);

INSERT INTO moto (identificador, modelo, placa, ativa, localizacao_id) VALUES ('MOTO-001', 'Honda Biz', 'ABC1234', true, 1);
INSERT INTO moto (identificador, modelo, placa, ativa, localizacao_id) VALUES ('MOTO-002', 'Yamaha Fazer', 'XYZ5678', true, 2);