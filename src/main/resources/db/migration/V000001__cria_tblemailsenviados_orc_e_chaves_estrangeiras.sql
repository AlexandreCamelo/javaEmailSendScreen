CREATE TABLE tblemailsenviados(
	cod SERIAL PRIMARY KEY,
	remetente TEXT,
	para TEXT,
	cc TEXT,
	cco TEXT,
	assunto VARCHAR(500),
	corpo TEXT,
	listaDeAnexos TEXT,
	data_hora TIMESTAMP WITH TIME ZONE
);