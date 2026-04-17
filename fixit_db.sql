-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 17-04-2026 a las 15:59:03
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `fixit_db`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `avisos`
--

CREATE TABLE `avisos` (
  `id` bigint(20) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `firma_cliente` varchar(255) DEFAULT NULL,
  `foto_averia` varchar(255) DEFAULT NULL,
  `categoria_id` bigint(20) DEFAULT NULL,
  `cliente_id` bigint(20) DEFAULT NULL,
  `tecnico_id` bigint(20) DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `prioridad` varchar(20) DEFAULT 'MEDIA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `avisos`
--

INSERT INTO `avisos` (`id`, `descripcion`, `estado`, `firma_cliente`, `foto_averia`, `categoria_id`, `cliente_id`, `tecnico_id`, `fecha_creacion`, `prioridad`) VALUES
(1, 'Rotura de tubería principal en los baños', 'COMPLETADO', NULL, '/fotos/averias/hotel_001.jpg', 1, 1, 2, NULL, 'MEDIA'),
(2, 'Revisión de mantenimiento mensual', 'COMPLETADO', NULL, NULL, 1, 1, 2, '2026-03-04 00:00:00.000000', 'MEDIA'),
(3, 'Revisión de mantenimiento mensual', 'COMPLETADO', NULL, NULL, 2, 1, 2, '2026-03-04 00:00:00.000000', 'MEDIA'),
(5, '', 'EN PROGRESO', NULL, NULL, 2, 1, 2, '2026-04-08 00:00:00.000000', 'MEDIA'),
(6, 'tuberia de los baños pierde agua e inunda.', 'COMPLETADO', NULL, NULL, 1, 3, 4, '2026-04-16 00:00:00.000000', 'MEDIA');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `aviso_materiales`
--

CREATE TABLE `aviso_materiales` (
  `id` bigint(20) NOT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `aviso_id` bigint(20) DEFAULT NULL,
  `material_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `aviso_materiales`
--

INSERT INTO `aviso_materiales` (`id`, `cantidad`, `aviso_id`, `material_id`) VALUES
(1, 5, 1, 1),
(2, 5, 1, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias`
--

CREATE TABLE `categorias` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categorias`
--

INSERT INTO `categorias` (`id`, `nombre`) VALUES
(3, 'Ascensores'),
(2, 'Electricidad'),
(1, 'Fontanería');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clientes`
--

CREATE TABLE `clientes` (
  `id` bigint(20) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `tipo` varchar(255) DEFAULT NULL,
  `notas` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `clientes`
--

INSERT INTO `clientes` (`id`, `direccion`, `nombre`, `telefono`, `email`, `tipo`, `notas`) VALUES
(1, 'Avenida del Mar, 45', 'Hotel Las Palmeras', '600111222', 'info@laspalmerashotel.com', 'EMPRESA', 'Cliente premium, atención prioritaria'),
(3, 'Calle Malaga, 9', 'Restaurante Los billares', '900123654', 'losbillares@gmail.com', 'EMPRESA', 'Solo por la mañana');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `materiales`
--

CREATE TABLE `materiales` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `precio` double DEFAULT NULL,
  `stock` int(11) DEFAULT 0,
  `stock_minimo` int(11) DEFAULT 5,
  `unidad` varchar(255) DEFAULT NULL,
  `categoria_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `materiales`
--

INSERT INTO `materiales` (`id`, `nombre`, `precio`, `stock`, `stock_minimo`, `unidad`, `categoria_id`) VALUES
(1, 'Tubería PVC 50mm', 12.5, 45, 20, 'metros', 1),
(2, 'Rollo Cable Cobre 2.5mm', 45, 12, 30, 'metros', 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tecnicos`
--

CREATE TABLE `tecnicos` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `especialidad` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` varchar(255) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1,
  `calificacion` double DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tecnicos`
--

INSERT INTO `tecnicos` (`id`, `email`, `especialidad`, `nombre`, `password`, `rol`, `telefono`, `activo`, `calificacion`) VALUES
(1, 'jefe@fixit.com', 'Administración', 'David Montoro', '1234password', 'ADMIN', NULL, 1, 0),
(2, 'mario@fixit.com', 'ELECTRICIDAD, FONTANERÍA', 'Mario Bros', 'mypassword123', 'TECNICO', '+34 600 111 222', 1, 4.7),
(4, 'antonio@fixit.com', 'FONTANERÍA', 'Antonio Gutierrez', '1234', 'TECNICO', '651789000', 1, 0);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `avisos`
--
ALTER TABLE `avisos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK9a6d6yulmr5brjotng8cttwax` (`categoria_id`),
  ADD KEY `FK3pc3sydfke1kauj7aiaqfo4pb` (`cliente_id`),
  ADD KEY `FK18d7s8gjho97b2y8m5tg5p09f` (`tecnico_id`);

--
-- Indices de la tabla `aviso_materiales`
--
ALTER TABLE `aviso_materiales`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK2847jrx2amw07he0wp3br3kve` (`aviso_id`),
  ADD KEY `FKj2yv7dg0jgeo74u0tmsqkvdgq` (`material_id`);

--
-- Indices de la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKqcog8b7hps1hioi9onqwjdt6y` (`nombre`);

--
-- Indices de la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `materiales`
--
ALTER TABLE `materiales`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_materiales_categorias` (`categoria_id`);

--
-- Indices de la tabla `tecnicos`
--
ALTER TABLE `tecnicos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKbquon34afupdx4ssovepfeyp6` (`email`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `avisos`
--
ALTER TABLE `avisos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `aviso_materiales`
--
ALTER TABLE `aviso_materiales`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `materiales`
--
ALTER TABLE `materiales`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `tecnicos`
--
ALTER TABLE `tecnicos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `avisos`
--
ALTER TABLE `avisos`
  ADD CONSTRAINT `FK18d7s8gjho97b2y8m5tg5p09f` FOREIGN KEY (`tecnico_id`) REFERENCES `tecnicos` (`id`),
  ADD CONSTRAINT `FK3pc3sydfke1kauj7aiaqfo4pb` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  ADD CONSTRAINT `FK9a6d6yulmr5brjotng8cttwax` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`);

--
-- Filtros para la tabla `aviso_materiales`
--
ALTER TABLE `aviso_materiales`
  ADD CONSTRAINT `FK2847jrx2amw07he0wp3br3kve` FOREIGN KEY (`aviso_id`) REFERENCES `avisos` (`id`),
  ADD CONSTRAINT `FKj2yv7dg0jgeo74u0tmsqkvdgq` FOREIGN KEY (`material_id`) REFERENCES `materiales` (`id`);

--
-- Filtros para la tabla `materiales`
--
ALTER TABLE `materiales`
  ADD CONSTRAINT `FK_materiales_categorias` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
