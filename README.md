# Realty Commission

Realty Commission es el microservicio destinado a gestionar la información de las comisiones asociadas a las operaciones inmobiliarias de RealtyHub. Su función es organizar los registros de las comisiones para facilitar su administración dentro de la plataforma.

## Funcionalidades

- Definición de acuerdos comerciales con las reglas de comisión por tipo de contrato.
- Cálculo automático de la comisión y su reparto entre agente, oficina y central cuando se firma un contrato.
- Aprobación, pago, anulación y reversión de comisiones, con historial de cambios.
- Publica eventos cuando cambia el estado de una comisión.

## Tecnologías

Java 21, Spring Boot, PostgreSQL, Flyway y RabbitMQ.

## Puerto

El servicio se ejecuta en el puerto `8087`.
