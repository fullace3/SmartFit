DROP DATABASE IF EXISTS SmartFit;
CREATE DATABASE SmartFit;
USE SmartFit;

-- 1. Tabla de Usuarios
CREATE TABLE USUARIO (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Dietas
CREATE TABLE DIETA (
    id_dieta INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    objetivo_calorico INT NOT NULL,
    proteinas_g DECIMAL(5,2),
    carbohidratos_g DECIMAL(5,2),
    grasas_g DECIMAL(5,2),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- 3. Tabla de Comidas
CREATE TABLE COMIDA (
    id_comida INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    calorias_100g INT NOT NULL,
    proteinas_100g INT NOT NULL,
    carbohidratos_100g INT NOT NULL,
    grasas_100g INT NOT NULL,
    id_usuario INT,
    id_dieta INT,
    imagen VARCHAR(255),
    FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_dieta) REFERENCES DIETA(id_dieta)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- 4. Tabla de Medidas Corporales
CREATE TABLE MEDIDA_CORPORAL (
    id_medida INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    fecha DATE NOT NULL,
    peso_kg DECIMAL(5,2) NOT NULL,
    altura_cm DECIMAL(5,2),
    pecho_cm DECIMAL(5,2),
    pierna_cm DECIMAL(5,2),
    brazo_cm DECIMAL(5,2),
    grasa_corporal_pct DECIMAL(4,2),
    FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- 5. Tabla de Rutinas
CREATE TABLE RUTINA (
    id_rutina INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- 6. Tabla de Ejercicios
CREATE TABLE EJERCICIO (
    id_ejercicio INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    grupo_muscular VARCHAR(100),
    equipamiento VARCHAR(100),
    imagen VARCHAR(255)
);

-- 7. Tabla Intermedia Rutina-Ejercicio
CREATE TABLE RUTINA_EJERCICIO (
    id_rutina INT NOT NULL,
    id_ejercicio INT NOT NULL,
    series INT DEFAULT 3,
    repeticiones INT DEFAULT 10,
    orden INT,
    PRIMARY KEY (id_rutina, id_ejercicio),
    FOREIGN KEY (id_rutina) REFERENCES RUTINA(id_rutina)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_ejercicio) REFERENCES EJERCICIO(id_ejercicio)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- 8. Tabla de Historial de entrenamientos
CREATE TABLE HISTORIAL_ENTRENAMIENTO (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_ejercicio INT NOT NULL,
    peso_kg FLOAT NOT NULL,
    repeticiones INT NOT NULL,
    series INT NOT NULL,
    duracion_minutos INT NOT NULL DEFAULT 0,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_historial_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES EJERCICIO(id_ejercicio) ON DELETE CASCADE
);

INSERT INTO COMIDA (id_comida, nombre, calorias_100g, proteinas_100g, carbohidratos_100g, grasas_100g, imagen) VALUES
  (1, 'Leche semidesnatada (2%)', 50.0, 3.35, 4.91, 1.9, 'leche.jpg'),
  (2, 'Tomates cherry, crudos', 27.0, 0.83, 5.51, 0.63, 'tomate.png'),
  (3, 'Brócoli crudo', 32.0, 2.57, 6.29, 0.34, 'brocoli.jpg'),
  (4, 'Leche desnatada (1%)', 43.0, 3.38, 5.19, 0.95, 'leche.jpg'),
  (5, 'Leche desnatada (0%)', 34.0, 3.43, 4.89, 0.08, 'leche.jpg'),
  (6, 'Leche entera', 60.0, 3.28, 4.67, 3.2, 'leche.jpg'),
  (7, 'Salchicha de ternera', 314.0, 11.7, 2.89, 28.0, 'salchicha.jpg'),
  (8, 'Almendras tostadas', 620.0, 20.4, 16.2, 57.8, 'almendras.jpg'),
  (9, 'Queso ricotta', 157.0, 7.81, 6.86, 11.0, 'queso_ricotta.jpg'),
  (10, 'Salchicha de ternera precocinada', 328.0, 13.3, 3.37, 28.7, 'salchicha.jpg'),
  (11, 'Queso parmesano rallado', 421.0, 29.6, 12.4, 28.0, 'queso_parmesano.jpg'),
  (12, 'Queso americano en lonchas', 366.0, 18.0, 5.27, 30.6, 'queso_lonchas.png'),
  (13, 'Salchicha italiana de cerdo', 322.0, 18.2, 2.15, 26.2, 'salchicha_italiana.jpg'),
  (14, 'Pan blanco', 270.0, 9.43, 49.2, 3.59, 'pan_blanco.jpg'),
  (15, 'Salchicha de pavo', 169.0, 16.7, 0.93, 10.4, 'salchicha_pavo.jpg'),
  (16, 'Queso suizo', 393.0, 27.0, 1.44, 31.0, 'queso_suizo.jpg'),
  (17, 'Naranjas crudas', 47.0, 0.91, 11.8, 0.15, 'naranja.jpg'),
  (18, 'Fresas crudas', 31.0, 0.64, 7.63, 0.22, 'fresas.jpg'),
  (19, 'Lechuga romana cruda', 17.0, 1.24, 3.24, 0.26, 'lechuga.jpg'),
  (20, 'Queso cheddar', 408.0, 23.3, 2.44, 34.0, 'queso_cheddar.png'),
  (21, 'Queso cottage bajo en grasa', 84.0, 11.0, 4.31, 2.3, 'queso_cottage.jpg'),
  (22, 'Queso mozzarella', 298.0, 23.7, 4.44, 20.4, 'queso_mozzarella.jpg'),
  (23, 'Queso fresco seco', 326.0, 24.5, 2.07, 24.3, 'queso_fresco.png'),
  (24, 'Yogur griego natural desnatado', 61.0, 10.3, 3.64, 0.37, 'yogur_griego.jpg'),
  (25, 'Yogur griego de fresa desnatado', 83.0, 8.06, 12.2, 0.15, 'yogur_fresa.png'),
  (26, 'Pavo picado (93% magro)', 220.0, 27.1, 0.0, 11.6, 'pavo_picado.jpg'),
  (27, 'Muslo de pollo cocinado', 156.0, 23.9, 0.0, 5.95, 'pollo_muslo.jpg'),
  (28, 'Pechuga de pollo cocinada', 166.0, 32.1, 0.0, 3.24, 'pechuga_pollo.jpg'),
  (29, 'Salsa marinara para pasta', 45.0, 1.41, 8.05, 1.48, 'salsa_tomate.jpg'),
  (30, 'Chorizo de cerdo', 346.0, 19.3, 2.63, 28.1, 'chorizo.png'),
  (31, 'Galletas de avena con pasas', 430.0, 5.79, 69.6, 14.3, 'galletas_avena.png'),
  (32, 'Cerdo agridulce', 260.0, 8.88, 25.5, 13.6, 'cerdo_agridulce.jpg'),
  (33, 'Arroz frito chino sin carne', 174.0, 3.84, 32.5, 3.19, 'arroz_frito.png'),
  (34, 'Tamal de cerdo', 174.0, 7.38, 15.8, 9.04, 'tamal.jpg'),
  (35, 'Pupusas con alubias', 229.0, 5.59, 31.5, 9.01, 'pupusas.jpg'),
  (36, 'Lomo de ternera crudo', 155.0, 22.8, 0.0, 6.39, 'ternera_lomo.jpg'),
  (37, 'Solomillo de ternera asado', 176.0, 27.7, 0.0, 6.36, 'ternera_solomillo.jpg'),
  (38, 'Redondo de ternera crudo', 122.0, 23.4, 0.0, 2.48, 'ternera_redondo.jpg'),
  (39, 'Tapa de ternera cruda', 123.0, 23.7, 0.0, 2.41, 'ternera_tapa.jpg'),
  (40, 'Chuletón T-bone a la plancha', 219.0, 27.3, 0.0, 11.4, 'chuleton.jpg'),
  (41, 'Porterhouse steak de ternera', 145.0, 22.7, 0.0, 5.32, 'ternera_porterhouse.jpg'),
  (42, 'Pan integral', 254.0, 12.3, 43.1, 3.55, 'pan_integral.jpg'),
  (43, 'Queso americano', 375.0, 17.5, 6.35, 31.1, 'queso_americano.png'),
  (44, 'Clara de huevo', 55.0, 10.7, 2.36, 0.0, 'huevo_clara.png'),
  (45, 'Yema de huevo', 334.0, 16.2, 1.02, 28.8, 'huevo_yema.jpg'),
  (46, 'Huevo entero', 148.0, 12.4, 0.96, 9.96, 'huevo.jpg'),
  (47, 'Bacon de cerdo cocinado', 500.0, 40.9, 2.1, 36.5, 'bacon.jpg'),
  (48, 'Harina de trigo panadera', 363.0, 14.3, 72.8, 1.65, 'harina_trigo.jpg'),
  (49, 'Harina de arroz blanco', 359.0, 6.94, 79.8, 1.3, 'harina_arroz.jpg'),
  (50, 'Plátano maduro', 85.0, 0.73, 20.1, 0.22, 'platano.jpg'),
  (51, 'Plátano en su punto', 97.0, 0.74, 23.0, 0.29, 'platano.jpg'),
  (52, 'Harina de arroz integral', 365.25, 7.19, 75.5, 3.85, 'harina_arroz.jpg'),
  (53, 'Harina de arroz glutinoso', 358.0, 6.69, 80.1, 1.16, 'harina_arroz.jpg'),
  (54, 'Manzana roja con piel', 62.0, 0.19, 14.8, 0.21, 'manzana.jpg'),
  (55, 'Manzana honeycrisp con piel', 60.0, 0.1, 14.7, 0.1, 'manzana.jpg'),
  (56, 'Manzana verde con piel', 59.0, 0.27, 14.2, 0.14, 'manzana_verde.jpg'),
  (57, 'Manzana gala con piel', 61.0, 0.13, 14.8, 0.15, 'manzana.jpg'),
  (58, 'Manzana fuji con piel', 65.0, 0.15, 15.7, 0.16, 'manzana.jpg');

INSERT INTO EJERCICIO (id_ejercicio, nombre, descripcion, grupo_muscular, equipamiento, imagen) VALUES
(1, 'Paralela Flexión', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Pecho', 'Barras paralelas', '2363-O2K9Vb5.jpg'),
(2, 'Barra Press de banca', 'Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.', 'Pecho', 'Barra', '0025-EIeI8Vf.jpg'),
(3, 'Barra Press de banca inclinado', 'Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.', 'Pecho', 'Barra', '0047-3TZduzM.jpg'),
(4, 'Barra Press de banca declinado', 'Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.', 'Pecho', 'Barra', '0033-GrO65fd.jpg'),
(5, 'Peso corporal Flexión', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Pecho', 'Peso corporal', '0662-I4hDWkc.jpg'),
(6, 'Barra Floor Press', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Pecho', 'Barra', '0065-vtusOWT.jpg'),
(7, 'Doble Mancuerna Press de banca', 'Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.', 'Pecho', 'Mancuerna', '0289-SpYC0Kp.jpg'),
(8, 'Doble Mancuerna Press de banca inclinado', 'Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.', 'Pecho', 'Mancuerna', '0314-ns0SIbU.jpg'),
(9, 'Doble Mancuerna Press de banca declinado', 'Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.', 'Pecho', 'Mancuerna', '0301-DwhEmmE.jpg'),
(10, 'Polea Straight Bar Pullover', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Espalda', 'Polea', '0238-x69MAlq.jpg'),
(11, 'Doble Mancuerna Banco inclinado Prono Remo', 'Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.', 'Espalda', 'Mancuerna', '0327-7vG5o25.jpg'),
(12, 'Barra Pendlay Remo', 'Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.', 'Espalda', 'Barra', '3017-r0z6xzQ.jpg'),
(13, 'Barra Bent Over Remo', 'Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.', 'Espalda', 'Barra', '0027-eZyBC3j.jpg'),
(14, 'Doble Mancuerna Prono Remo', 'Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.', 'Espalda', 'Mancuerna', '0293-BJ0Hz5L.jpg'),
(15, 'Barra Conventional Peso muerto', 'Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.', 'Espalda', 'Barra', '0032-ila4NZS.jpg'),
(16, 'Barra Sumo Peso muerto', 'Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.', 'Espalda', 'Barra', '0117-KgI0tqW.jpg'),
(17, 'Barra Good Morning', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Espalda', 'Barra', '0044-XlZ4lAC.jpg'),
(18, 'Barra Inversa Grip Bent Over Remo', 'Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.', 'Espalda', 'Barra', '0118-SzX3uzM.jpg'),
(19, 'Doble Mancuerna Sentado Good Morning', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Espalda', 'Mancuerna', '0090-d960PgE.jpg'),
(20, 'Doble Mancuerna Renegade Remo ', 'Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.', 'Espalda', 'Mancuerna', '0521-b9kqlBy.jpg'),
(21, 'Bar Dominada', 'Cuélgate con los brazos extendidos. Tira de tu cuerpo hacia arriba hasta pasar la barbilla por encima de la barra y baja de forma controlada.', 'Espalda', 'Barra de dominadas', '0652-lBDjFxJ.jpg'),
(22, 'Bar Scapular Dominada', 'Cuélgate con los brazos extendidos. Tira de tu cuerpo hacia arriba hasta pasar la barbilla por encima de la barra y baja de forma controlada.', 'Espalda', 'Barra de dominadas', '0688-uTBt1HV.jpg'),
(23, 'Polea V Grip Sentado Bajo Remo', 'Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.', 'Espalda', 'Polea', '0861-fUBheHs.jpg'),
(24, 'Polea Agarre ancho Lat Pulldown', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Espalda', 'Polea', '2330-LEprlgG.jpg'),
(25, 'Polea Inversa Grip Lat Pulldown', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Espalda', 'Polea', '0245-xBYcQHj.jpg'),
(26, 'Mancuerna Pullover', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Espalda', 'Mancuerna', '0375-9XjtHvS.jpg'),
(27, 'Polea Face Pull', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Polea', '0203-wqNPGCg.jpg'),
(28, 'Polea Sentado Face Pull', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Polea', '0203-wqNPGCg.jpg'),
(29, 'Barra Por encima de la cabeza Press', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Barra', '1456-wdRZISl.jpg'),
(30, 'Barra Push Press', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Barra', '1700-FS63wTN.jpg'),
(31, 'Barra Elevación frontal', 'Eleva el peso hacia adelante con los brazos casi rectos hasta la altura de los ojos y baja de forma controlada.', 'Hombros', 'Barra', '0041-b2Uoz54.jpg'),
(32, 'Doble Mancuerna Push Press', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Mancuerna', '1700-FS63wTN.jpg'),
(33, 'Doble Mancuerna Elevación Lateral', 'Con una ligera flexión de codos, eleva los brazos hacia los lados hasta la altura de los hombros y baja controlando el peso.', 'Hombros', 'Mancuerna', '0334-DsgkuIt.jpg'),
(34, 'Doble Mancuerna Sentado Elevación Lateral', 'Con una ligera flexión de codos, eleva los brazos hacia los lados hasta la altura de los hombros y baja controlando el peso.', 'Hombros', 'Mancuerna', '0396-hxyTtWj.jpg'),
(35, 'Doble Mancuerna Sentado Por encima de la cabeza Press', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Mancuerna', '0405-znQUdHY.jpg'),
(36, 'Doble Mancuerna Por encima de la cabeza Press', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Mancuerna', '0426-A6wtbuL.jpg'),
(37, 'Doble Mancuerna De pie Scaption', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Mancuerna', '0311-AQ0mC4Y.jpg'),
(38, 'Doble Mancuerna Arnold Press', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Hombros', 'Mancuerna', '2137-Xy4jlWA.jpg'),
(39, 'Barra Shrug', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Trapecio', 'Barra', '0095-dG7tG5y.jpg'),
(40, 'Doble Mancuerna Shrug', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Trapecio', 'Mancuerna', '0406-NJzBsGJ.jpg'),
(41, 'Polea Supino Curl de bíceps', 'Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.', 'Bíceps', 'Polea', '1634-otqIxU4.jpg'),
(42, 'Barra Curl de bíceps', 'Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.', 'Bíceps', 'Barra', '0031-25GPyDY.jpg'),
(43, 'Bar Dominada supina', 'Cuélgate con los brazos extendidos. Tira de tu cuerpo hacia arriba hasta pasar la barbilla por encima de la barra y baja de forma controlada.', 'Bíceps', 'Barra de dominadas', '1326-T2mxWqc.jpg'),
(44, 'Alterno Doble Mancuerna Curl de bíceps', 'Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.', 'Bíceps', 'Mancuerna', '0285-BU15nH4.jpg'),
(45, 'Alterno Doble Mancuerna Hammer Curl', 'Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.', 'Bíceps', 'Mancuerna', '1648-6em2Dxj.jpg'),
(46, 'A un brazo Mancuerna Banco inclinado Preacher Curl', 'Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.', 'Bíceps', 'Mancuerna', '1663-4dF3maG.jpg'),
(47, 'Doble Mancuerna Hammer Curl', 'Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.', 'Bíceps', 'Mancuerna', '0313-slDvUAU.jpg'),
(48, 'Peso corporal Diamond Flexión', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Tríceps', 'Peso corporal', '0283-soIB2rj.jpg'),
(49, 'Polea Cuerda Por encima de la cabeza Extensión de tríceps', 'Con los codos fijos, extiende los brazos por completo para contraer los tríceps y vuelve despacio a la posición inicial.', 'Tríceps', 'Polea', '1722-1xHyxys.jpg'),
(50, 'Paralela Agarre estrecho Flexión', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Tríceps', 'Barras paralelas', '0259-x6KpKpq.jpg'),
(51, 'Barra Agarre estrecho Press de banca', 'Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.', 'Tríceps', 'Barra', '0030-J6Dx1Mu.jpg'),
(52, 'Superband Assisted Dips', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Tríceps', 'Superband', '0019-J60bN17.jpg'),
(53, 'Doble Mancuerna Tríceps Kickback', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Tríceps', 'Mancuerna', '0333-W6PxUkg.jpg'),
(54, 'Mancuerna Sentado Por encima de la cabeza Extensión de tríceps', 'Con los codos fijos, extiende los brazos por completo para contraer los tríceps y vuelve despacio a la posición inicial.', 'Tríceps', 'Mancuerna', '2188-kont8Ut.jpg'),
(55, 'Barra Sentado Por encima de la cabeza Extensión de tríceps', 'Con los codos fijos, extiende los brazos por completo para contraer los tríceps y vuelve despacio a la posición inicial.', 'Tríceps', 'Barra', '0092-5uFK1xr.jpg'),
(56, 'Peso corporal Dips', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Tríceps', 'Peso corporal', '0251-9WTm7dq.jpg'),
(57, 'Barra Skull Crusher', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Tríceps', 'Barra', '0060-h8LFzo9.jpg'),
(58, 'Barra Thruster', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Cuádriceps', 'Barra', '3305-f7Y9eDZ.jpg'),
(59, 'Peso corporal Sentadilla', 'Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.', 'Cuádriceps', 'Peso corporal', '3119-75Bgtjy.jpg'),
(60, 'Mancuerna Goblet Sentadilla ', 'Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.', 'Cuádriceps', 'Mancuerna', '1760-yn8yg1r.jpg'),
(61, 'Barra Posición frontal Sentadilla', 'Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.', 'Cuádriceps', 'Barra', '0042-zG0zs85.jpg'),
(62, 'Barra Alto Bar Espalda Sentadilla', 'Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.', 'Cuádriceps', 'Barra', '1436-Gnfo4FM.jpg'),
(63, 'Barra Bajo Bar Espalda Sentadilla', 'Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.', 'Cuádriceps', 'Barra', '1435-bTpEUcm.jpg'),
(64, 'Doble Mancuerna Estilo maleta Sentadilla búlgara', 'Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.', 'Cuádriceps', 'Mancuerna', '0410-qx4fgX7.jpg'),
(65, 'Doble Mancuerna Estilo maleta Subida al cajón', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Cuádriceps', 'Mancuerna', '0431-aXtJhlg.jpg'),
(66, 'Doble Mancuerna Estilo maleta Alterno Hacia adelante Zancada', 'Da un paso y flexiona ambas rodillas hasta que la pierna trasera casi toque el suelo. Empuja para volver a la posición inicial.', 'Cuádriceps', 'Mancuerna', '3635-ecl28tP.jpg'),
(67, 'Doble Mancuerna Estilo maleta Alterno Inversa Zancada', 'Da un paso y flexiona ambas rodillas hasta que la pierna trasera casi toque el suelo. Empuja para volver a la posición inicial.', 'Cuádriceps', 'Mancuerna', '0381-SSsBDwB.jpg'),
(68, 'Barra Posición trasera Caminando Zancada', 'Da un paso y flexiona ambas rodillas hasta que la pierna trasera casi toque el suelo. Empuja para volver a la posición inicial.', 'Cuádriceps', 'Barra', '1460-IZVHb27.jpg'),
(69, 'Barra Posición trasera Subida al cajón', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Cuádriceps', 'Barra', '0114-Kxquu2E.jpg'),
(70, 'Doble Mancuerna Romanian Peso muerto', 'Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.', 'Isquiosurales', 'Mancuerna', '1459-rR0LJzx.jpg'),
(71, 'Barra Romanian Peso muerto', 'Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.', 'Isquiosurales', 'Barra', '0085-wQ2c4XD.jpg'),
(72, 'Barra Stiff Legged Peso muerto', 'Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.', 'Isquiosurales', 'Barra', '0116-hrVQWvE.jpg'),
(73, 'Barra A una pierna Romanian Peso muerto', 'Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.', 'Isquiosurales', 'Barra', '1756-gEyURal.jpg'),
(74, 'Peso corporal Puente de glúteo', 'Tumbado boca arriba con las rodillas flexionadas, empuja con los talones para elevar la cadera apretando los glúteos al máximo al subir.', 'Glúteos', 'Peso corporal', '3523-aWedzZX.jpg'),
(75, 'Mancuerna Puente de glúteo', 'Tumbado boca arriba con las rodillas flexionadas, empuja con los talones para elevar la cadera apretando los glúteos al máximo al subir.', 'Glúteos', 'Mancuerna', '1409-qKBpF7I.jpg'),
(76, 'Barra Cadera Thrust', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Glúteos', 'Barra', '1409-qKBpF7I.jpg'),
(77, 'Barra A una pierna Cadera Thrust', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Glúteos', 'Barra', '3645-rmEukuS.jpg'),
(78, 'Peso corporal Cadera Thrust', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Glúteos', 'Peso corporal', '3236-Pjbc0Kt.jpg'),
(79, 'Mancuerna Cadera Thrust', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Glúteos', 'Mancuerna', '0484-196HJGw.jpg'),
(80, 'Peso corporal Bird Dog', 'Mantén la columna neutra y el abdomen tenso. Extiende las extremidades opuestas de forma lenta y controlada sin perder la postura.', 'Abdominales', 'Peso corporal', '1111-Bj23Fn.png'),
(81, 'Pelota de estabilidad Giro ruso ', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Abdominales', 'Fitball', '0687-XVDdcoj.jpg'),
(82, 'Paralela Escalador', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Abdominales', 'Barras paralelas', '0630-RJgzwny.jpg'),
(83, 'Peso corporal Bicho muerto', 'Mantén la columna neutra y el abdomen tenso. Extiende las extremidades opuestas de forma lenta y controlada sin perder la postura.', 'Abdominales', 'Peso corporal', '0276-iny3m5y.jpg'),
(84, 'Peso corporal Kneeling Forearm Plancha', 'Apóyate en los antebrazos y las puntas de los pies. Mantén el cuerpo en línea recta contrayendo fuerte el abdomen y los glúteos.', 'Abdominales', 'Peso corporal', '3239-h1ezqSu.jpg'),
(85, 'Ab Wheel Kneeling Rollout', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Abdominales', 'Ab Wheel', '0857-NAgVB3t.jpg'),
(86, 'Polea Kneeling Encogimiento', 'Tumbado boca arriba, contrae el abdomen para elevar ligeramente los hombros del suelo sin tirar del cuello, y vuelve a bajar despacio.', 'Abdominales', 'Polea', '0175-WW95auq.jpg'),
(87, 'Paralela L Sit', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Abdominales', 'Barras paralelas', '3419-UpWmA5E.jpg'),
(88, 'Peso corporal De pie Walkout Plancha', 'Apóyate en los antebrazos y las puntas de los pies. Mantén el cuerpo en línea recta contrayendo fuerte el abdomen y los glúteos.', 'Abdominales', 'Peso corporal', '1471-ZgsNQ6d.jpg'),
(89, 'Peso corporal Plancha Lateral', 'Apóyate en los antebrazos y las puntas de los pies. Mantén el cuerpo en línea recta contrayendo fuerte el abdomen y los glúteos.', 'Abdominales', 'Peso corporal', '0705-RKjH6Lt.jpg'),
(90, 'Bar Inverted Hanging Encogimiento', 'Tumbado boca arriba, contrae el abdomen para elevar ligeramente los hombros del suelo sin tirar del cuello, y vuelve a bajar despacio.', 'Abdominales', 'Barra de dominadas', '0499-bZGHsAZ.jpg'),
(91, 'Barra Kneeling Rollout', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Abdominales', 'Barra', '0084-7M66AVi.jpg'),
(92, 'Bar Hanging Rodilla Elevación', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Abdominales', 'Barra de dominadas', '0011-03lzqwk.jpg'),
(93, 'Bar Hanging Rodillas a Codos', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Abdominales', 'Barra de dominadas', '0443-jvp6DiD.jpg'),
(94, 'Bar Hanging Pierna Elevación', 'Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.', 'Abdominales', 'Barra de dominadas', '0472-I3tsCnC.jpg'),
(95, 'Doble Mancuerna Estilo maleta Elevación de gemelos', 'Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.', 'Gemelos', 'Mancuerna', '0417-dPmaUaU.jpg'),
(96, 'Doble Mancuerna Sentado Elevación de gemelos', 'Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.', 'Gemelos', 'Mancuerna', '1379-r29jP7S.jpg'),
(97, 'Barra Sentado Elevación de gemelos', 'Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.', 'Gemelos', 'Barra', '1371-ipvgBnC.jpg'),
(98, 'Peso corporal Mano Assisted Pies elevados Elevación de gemelos', 'Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.', 'Gemelos', 'Peso corporal', '0284-u5ESqzH.jpg'),
(99, 'Barra Posición trasera Elevación de gemelos', 'Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.', 'Gemelos', 'Barra', '1372-8ozhUIZ.jpg'),
(100, 'Barra Inversa Grip Curl de bíceps', 'Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.', 'Antebrazos', 'Barra', '0080-xNrS20v.jpg');