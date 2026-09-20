-- Crea un usuario/esquema Oracle por microservicio de CampusLab (sección 4 del brief).
-- gvenzl/oracle-free ejecuta este script automáticamente en la PDB (XEPDB1) al
-- primer arranque del contenedor (carpeta /container-entrypoint-initdb.d/).

ALTER SESSION SET CONTAINER = XEPDB1;

CREATE USER campuslab_bookings IDENTIFIED BY campuslab;
GRANT CONNECT, RESOURCE TO campuslab_bookings;
ALTER USER campuslab_bookings QUOTA UNLIMITED ON USERS;

CREATE USER campuslab_catalog IDENTIFIED BY campuslab;
GRANT CONNECT, RESOURCE TO campuslab_catalog;
ALTER USER campuslab_catalog QUOTA UNLIMITED ON USERS;

CREATE USER campuslab_audit IDENTIFIED BY campuslab;
GRANT CONNECT, RESOURCE TO campuslab_audit;
ALTER USER campuslab_audit QUOTA UNLIMITED ON USERS;

CREATE USER campuslab_report IDENTIFIED BY campuslab;
GRANT CONNECT, RESOURCE TO campuslab_report;
ALTER USER campuslab_report QUOTA UNLIMITED ON USERS;
