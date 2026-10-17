-- Asegurar que la PDB quede abierta y guarde su estado para reinicios
ALTER PLUGGABLE DATABASE FREEPDB1 OPEN;
ALTER PLUGGABLE DATABASE FREEPDB1 SAVE STATE;

-- Cambiar el contexto a la Pluggable Database
ALTER SESSION SET CONTAINER=FREEPDB1;

-- Crear los esquemas aislando las contraseñas en minúsculas con comillas dobles
CREATE USER campuslab_bookings IDENTIFIED BY "campuslab";
GRANT ALL PRIVILEGES TO campuslab_bookings;

CREATE USER catalog IDENTIFIED BY "campuslab";
GRANT ALL PRIVILEGES TO catalog;

CREATE USER audit IDENTIFIED BY "campuslab";
GRANT ALL PRIVILEGES TO audit;

CREATE USER report IDENTIFIED BY "campuslab";
GRANT ALL PRIVILEGES TO report;