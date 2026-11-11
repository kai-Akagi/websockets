package com.demo.websocketserver.model;

/**
 * Entidad de negocio que se envía por el mensajero en formato JSON,
 * por ejemplo: {"matricula":"A001","nombre":"Ana López","carrera":"Sistemas","semestre":4}
 */
public record Alumno(String matricula, String nombre, String carrera, Integer semestre) {
}
