
CREATE TABLE IF NOT EXISTS localizacao (
  id SERIAL PRIMARY KEY,
  zona VARCHAR(100) NOT NULL,
  descricao VARCHAR(255),
  latitude NUMERIC(9,6),
  longitude NUMERIC(9,6),
  criado_em TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS moto (
  id SERIAL PRIMARY KEY,
  placa VARCHAR(20) UNIQUE NOT NULL,
  modelo VARCHAR(150) NOT NULL,
  fabricante VARCHAR(100),
  ano INT,
  localizacao_id INT REFERENCES localizacao(id),
  criado_em TIMESTAMP DEFAULT now()
);

INSERT INTO localizacao(zona, descricao, latitude, longitude) VALUES
('Zona A', 'Pátio A - entrada', -23.550520, -46.633308),
('Zona B', 'Pátio B - saída', -23.551000, -46.634000);

INSERT INTO moto(placa, modelo, fabricante, ano, localizacao_id) VALUES
('ABC1234', 'YBR 125', 'Yamaha', 2019, 1),
('XYZ5678', 'CG 160', 'Honda', 2021, 2);